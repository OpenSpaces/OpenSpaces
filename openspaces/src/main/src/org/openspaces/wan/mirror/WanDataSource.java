package org.openspaces.wan.mirror;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jini.admin.JoinAdmin;
import net.jini.space.JavaSpace;

import org.openspaces.admin.Admin;
import org.openspaces.admin.space.Space;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.core.transaction.manager.DistributedJiniTxManagerConfigurer;
import org.openspaces.core.transaction.manager.LocalJiniTxManagerConfigurer;
import org.openspaces.pu.service.PlainServiceMonitors;
import org.openspaces.pu.service.ServiceMonitors;
import org.openspaces.pu.service.ServiceMonitorsProvider;
import org.openspaces.wan.mirror.WanEntry.TxnData;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.gigaspaces.async.AsyncFutureListener;
import com.gigaspaces.async.AsyncResult;
import com.gigaspaces.datasource.BulkDataPersister;
import com.gigaspaces.datasource.BulkItem;
import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.DataProvider;
import com.gigaspaces.datasource.DataSourceException;
import com.gigaspaces.internal.backport.java.util.Arrays;
import com.gigaspaces.internal.client.spaceproxy.SpaceProxyImpl;
import com.gigaspaces.internal.transport.EntryPacket;
import com.gigaspaces.lrmi.ProtocolAdapter;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.OperationID;
import com.j_spaces.core.admin.StatisticsAdmin;
import com.j_spaces.core.client.SQLQuery;
import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.core.filters.ReplicationStatistics;
import com.j_spaces.core.filters.ReplicationStatistics.OutgoingChannel;
import com.j_spaces.core.filters.ReplicationStatistics.OutgoingReplication;
import com.j_spaces.kernel.ResourceLoader;
import com.sun.jini.reggie.GigaRegistrar;

/****************************************
 * A BulkDataPersister implementation used to synchronize data across different GigaSpaces clusters.
 * Each bulk that arrives at this data source will be written to an embedded space. This space is
 * part of a replication group along with the embedded spaces in other mirrors. The mirrors in the
 * other sites will pick up the updates and write them to their local clusters.
 * 
 * 
 * @author barakme
 * 
 */
@SuppressWarnings({ "deprecation" })
public class WanDataSource implements DisposableBean, BulkDataPersister, DataProvider,
        ClusterInfoAware, ServiceMonitorsProvider {

    private static final int ADMIN_LOOKUP_TIMEOUT_SECONDS = 10;

    // logger
    static final Logger logger = Logger.getLogger(WanDataSource.class.getName());

    private static final String LOCAL_LUS_GROUP_DEFAULT = "WAN_LOCAL";

    // Space access timeouts
    public static final int WAN_SPACE_ACCESS_TIMEOUT = 5000;
    private static final int LOCAL_SPACE_LOOKUP_INTERVAL_SECONDS = 15;
    private static final int LOCAL_SPACE_LOOKUP_RETRIES = 4;
    public static final int LOCAL_SPACE_ACCESS_TIMEOUT = 5000;

    // String template for the URL of the wan cluster
    private static final String DEFAULT_WAN_SPACE_URL_TEMPLATE =
            "/./wanSpace?cluster_schema=sync_replicated";

    

    // ///////////////////////
    // Injected properties //
    // ///////////////////////

    // Locations of embedded LUS per each site, in format host:port[;name]
    // should be set from static config file during startup
    private List<LocationConfiguration> locationsConfiguration = null;

    // URL of the wan space
    private String wanSpaceUrl = DEFAULT_WAN_SPACE_URL_TEMPLATE;

    // URL for lookup of the local cluster (that this mirror belongs to)
    private String localClusterSpaceUrl;

    // 1-Based index of this site in the locations list
    // should be set in config file, or will be calculated during startup
    private int mySiteId = -1;

    // Interval between executions of the cleanup tasks
    // NOTE: Not used! TODO: Finish this.
    private long cleanupTaskInterval = 30000;

    // Number of partitions in the local cluster. If 0, information is loaded
    // via the admin API, using the injected local cluster URL.
    private int numberOfPartitions = 0;

    // The wan-wide clustered space
    private GigaSpace wanGigaSpace;
    // proxy back to the space cluster that this mirror belongs to.
    private GigaSpace localClusterSpace;

    // Transaction manager for the wan cluster operations
    private PlatformTransactionManager wanTransactionManager = null;

    // Transaction manager for the local cluster operations
    private PlatformTransactionManager localClusterTransactionManager;

    private TransactionTemplate localClusterTransactionTemplate;

    // parsed location objects, based on the injected locations list
    private List<WanLocation> allLocations = null;

    // The client ID of the local cluster space proxy
    private long localClusterProxyClientId;

    // 0-Based Array of write indices - 1 per active partition in the local cluster
    private AtomicLongArray writeIndicesPerPartition;

    // flag indicating whether updates from remote sites should be processed
    private boolean isWaitForUpdatesEnabled = true;

    // The IP address for the machine running the mirror. May be set by NIC_ADDR
    // environment variable, or defaults to InetAddress.getLocalHost().getHostAddress()
    private String myNICAddress;

    // The lookup group used by the local LUS
    private String localLUSLookupGroups = LOCAL_LUS_GROUP_DEFAULT;

    // The name of this PU
    private String puName;

    // The LRMI port that this mirror should be bound to
    private int gscPort;
    // The unicast discovery port used by the embedded LUS
    private int discoveryPort;

    // Statistics counter
    private final AtomicLong totalProcessedBulks = new AtomicLong();

    // Handler for optimistic locking collisions across clusters
    @Autowired
    private CollisionHandler collisionHandler;

    // list of listeners, used in shutdown.
    private List<UpdateListener> wanListeners = new LinkedList<UpdateListener>();

    /*
     * private void initCleanUpTaskExecutor() {
     * 
     * cleanupTaskExecutor = Executors.newScheduledThreadPool(1); final Runnable cleanUpTask = new
     * LogCleanupRunnable(wanGigaSpace, wanTransactionTemplate, locationsConfiguration.size(),
     * deleteLogQuery);
     * 
     * cleanupTaskExecutor.scheduleWithFixedDelay(cleanUpTask, 10, cleanupTaskInterval,
     * TimeUnit.MILLISECONDS);
     * 
     * }
     */

    // TODO: Is this required?
    private String wanLookupGroup = "WAN_CLUSTER";

    // A debug feature - injectable locators string that will be added to the wan
    // space locators.
    private String wanAdditionalLocators;

    private GigaRegistrar reggieImpl;

    // Used to shut down the embedded space
    private UrlSpaceConfigurer wanSpaceConfigurer;

    /*******
     * Returns a set of the possible IPs and host names that this host may be named. Used when
     * mySiteId is not set in the configuration.
     * 
     * @return A set of IPs for this host.
     * @throws SocketException .
     * @throws UnknownHostException .
     */
    private Set<String> createLocalAddresses() throws SocketException, UnknownHostException {
        final Set<String> localAddresses = new HashSet<String>();

        // TODO: Uncomment this when done with testing - It can be very slow.
        /*
         * Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
         * while(nics.hasMoreElements()){ NetworkInterface nic = nics.nextElement();
         * Enumeration<InetAddress> addressess = nic.getInetAddresses();
         * while(addressess.hasMoreElements()) { InetAddress address = addressess.nextElement();
         * localAddresses.add(address.getCanonicalHostName());
         * localAddresses.add(address.getHostAddress()); localAddresses.add(address.getHostName());
         * } }
         */

        localAddresses.add(InetAddress.getLocalHost().getHostAddress());
        localAddresses.add(InetAddress.getLocalHost().getHostName());
        return localAddresses;
    }

    private WanEntry createWANEntry(final List<BulkItem> bulkItems, final TxnData txnData) {

        final EntryPacket[] packets = new EntryPacket[bulkItems.size()];
        final short[] operationTypes = new short[bulkItems.size()];
        int i = 0;

        int partitionId = 0;
        boolean first = true;
        for (final BulkItem bulkItem : bulkItems) {
            // logger.info(bulkItem.toString());

            final Object item = bulkItem.getItem();
            final EntryPacket packet = (EntryPacket) item;

            if (first) {
                first = false;

                final Object routingField = packet.getRoutingFieldValue();
                if (routingField != null) {
                    partitionId = (routingField.hashCode() % this.numberOfPartitions);
                }
            }
            packets[i] = packet;
            operationTypes[i] = bulkItem.getOperation();
            ++i;
        }

        final long nextWriteIndex = this.writeIndicesPerPartition.get(partitionId) + 1;
        final WanEntry entry =
                new WanEntry(this.mySiteId, partitionId, nextWriteIndex,
                        packets, operationTypes, txnData);
        return entry;
    }

    /***********
     * The main entry point for the WAN Gateway. This is the method that sends changes from the
     * local cluster to the remote ones.
     */
    public void executeBulk(final List<BulkItem> bulkItems)
            throws DataSourceException {

        if (bulkItems.size() == 0) {
            logger.warning("Recieved a bulk update with no items");
            return;
        }

        if (isResonantBulk(bulkItems)) {
            if (logger.isLoggable(Level.FINE)) {
                logger.finer("Detected a resonant bulk update - ignoring");
            }
            return;
        }

        // Distributed transaction support has been canceled
        // It looks like there is no way to implement this without
        // risking deadlocks, operation reordering or data loss
        // So txnData is always null, and every bulk will run as an
        // Independent transaction
        // final TxnData txnData = createTxnData();

        final TxnData txnData = null;

        final WanEntry entry = createWANEntry(bulkItems, txnData);

        logger.fine("********** EXECUTE BULK ON MIRROR ********");
        wanGigaSpace.write(entry);

        // Note: if the thread dies before this method, there
        // could be a problem
        this.writeIndicesPerPartition.incrementAndGet(
            entry.getPartitionIndex());

    }

    public long getCleanupTaskInterval() {
        return cleanupTaskInterval;
    }

    public CollisionHandler getCollisionHandler() {
        return collisionHandler;
    }

    public String getLocalClusterSpaceUrl() {
        return localClusterSpaceUrl;
    }

    public String getLocalLUSLookupGroups() {
        return localLUSLookupGroups;
    }

    // ///////////////////////
    // Initializers
    // //////////////////////

    private WanLocation getLocationByIndex(final int index) {
        return this.allLocations.get(index - 1);
    }

    public List<LocationConfiguration> getLocations() {
        return locationsConfiguration;
    }

    public String getMyNICAddress() {
        return myNICAddress;
    }

    public int getMySiteId() {
        return mySiteId;
    }

    public int getNumberOfPartitions() {
        return numberOfPartitions;
    }

    public String getPuName() {
        return puName;
    }

    /********
     * Service monitors implementation.
     * 
     */
    public ServiceMonitors[] getServicesMonitors() {
        final PlainServiceMonitors psm = new PlainServiceMonitors("WAN_MIRROR");

        final IJSpace ijSpace = this.wanGigaSpace.getSpace();

        final Map<String, Object> map = psm.getMonitors();
        map.put("Site Name", getLocationByIndex(this.mySiteId).getName());
        map.put("Total Processed Bulks", this.totalProcessedBulks.longValue());
        map.put("Collision Detection", (collisionHandler == null ? "Off" : "On"));

        try {
            final ReplicationStatistics stats = ((StatisticsAdmin) ijSpace.getAdmin()).getHolder()
                .getReplicationStatistics();
            final OutgoingReplication outgoingReplication = stats.getOutgoingReplication();
            map.put("Redo Log Size", outgoingReplication.getRedoLogSize());
            map.put("Redo Log External Storage Packet Count",
                    outgoingReplication.getRedoLogExternalStoragePacketCount());
            map.put("Redo Log External Storate Space Used", outgoingReplication.getRedoLogExternalStorageSpaceUsed());
            map.put("Redo Log Memory Packet Count",
                    outgoingReplication.getRedoLogMemoryPacketCount());

            final List<OutgoingChannel> channels = stats.getOutgoingReplication().getChannels();
            for (final OutgoingChannel channel : channels) {
                final HashMap<String, Object> channelMap =
                        new HashMap<String, Object>();

                final String targetName = getTargetNameForChannel(channel);
                channelMap.put("Name", targetName);
                channelMap.put("State", channel.getState().toString());
                channelMap.put("Sent Bytes", channel.getSentBytes());
                channelMap.put("Received Bytes", channel.getReceivedBytes());
                channelMap.put("Sent Bytes per Second", channel.getSendBytesPerSecond());
                channelMap.put("Received Bytes per Second", channel.getReceiveBytesPerSecond());
                channelMap.put("Inconsistent", channel.getInconsistencyReason() != null);

                map.put("Channel " + targetName, channelMap);

            }
        } catch (final RemoteException e) {
            e.printStackTrace();
        }

        return new ServiceMonitors[] { psm };

    }

    private String getTargetNameForChannel(final OutgoingChannel channel) {
        // TODO - map this to site location name
        return channel.getTargetMemberName();
        // for(WanLocation loc : this.allLocations) {
        // if(channel.getTargetMemberName())
        // }
    }

    public String getWanAdditionalLocators() {
        return wanAdditionalLocators;
    }

    public String getWanLookupGroup() {
        return wanLookupGroup;
    }

    private String getWanSpaceUrl() {

        final String res = this.wanSpaceUrl
                + "&total_members=" + this.allLocations.size()
                + "&id=" + this.mySiteId;

        return res;

    }

    private void handleSiteInfoQueryResult(final AsyncResult<WanSiteInfo> result) {
        if (result.getException() != null) {
            logger.severe("Failed to read WanSiteInfo from wan space. Number of partition for target site cannot be determined!");
            return;
        }

        final WanSiteInfo info = result.getResult();
        if (info == null) {
            logger.severe("Got a null when reading WanSiteInfo from wan space. Number of partition for target site cannot be determined!");
        }

        final int numOfPartitions = info.getNumberOfPartitions();
        final int targetSite = info.getSiteId();

        if (logger.isLoggable(Level.INFO)) {
            logger.info("Setting number of partitions in site: " + targetSite + " to: " + numOfPartitions);
        }

        final WanLocation location = this.getLocationByIndex(targetSite);
        location.setNumberOfPartitions(numOfPartitions);
        startQueriesForLocation(location);

    }

    // ///////////////////////////
    // Managed Data Source API //
    // ///////////////////////////
    public void init(final Properties arg0) throws DataSourceException {
        // ignore
    }

    private void initClusterJoin() {

        readWanSiteInfos();

        // now register listener to handle wan site notifications
        // for the sites that are not up yet
        registerSiteInfoQueriesForNewSites();

    }

    private void initEmbeddedLUS() {

        try {
            final URL reggieConfig = ResourceLoader.getServicesConfigUrl();
            // final Class reggieClass = Thread.currentThread()
            // .getContextClassLoader()
            // .loadClass(SystemConfig.getReggieClassName());
            // final Constructor constructor = reggieClass.getDeclaredConstructor(String[].class,
            // LifeCycle.class);
            // constructor.setAccessible(true);

            final String config = reggieConfig.toExternalForm();
            this.reggieImpl = new GigaRegistrar(new String[] { config }, null);

            // final Object reggieImpl = constructor.newInstance(new String[] {
            // reggieConfig.toExternalForm() }, null);
            final JoinAdmin joiner = reggieImpl;

            joiner.setLookupGroups(new String[] { "WAN_CLUSTER" });
            final Object reggieProxy = ((com.sun.jini.start.ServiceProxyAccessor) reggieImpl).getServiceProxy();
            new com.sun.jini.start.NonActivatableServiceDescriptor.Created(reggieImpl, reggieProxy);
        } catch (final Exception e) {
            throw new IllegalStateException("Could not start embedded LUS: " + e.getMessage(), e);
        }

    }

    /*******
     * Initialization of the WAN mirror starts here.
     */
    public void initialize() throws Exception {

        logger.info("Initializing WanDataSource with site ID: " + this.mySiteId + ", Local Cluster URL: "
                + this.localClusterSpaceUrl);

        initLocalAddress();

        initLocations();

        new WanLicenseVerifier().verifyLicense();

        // check if mirror is running on the required GSC.
        final boolean isCorrectGSC = initWanLUS();
        if (isCorrectGSC) {

            initLocalClusterSpaceProxy();

            initNumberOfPartitions();

            initWanSpace();

            initClusterJoin();

            initIndices();

            initReadQueries();

            logger.info("******** WAN MIRROR GATEWAY WAS INITIALIZED CORRECTLY ********");
        } else {
            logger.info("******** FORKING NEW CONTAINER FOR MIRROR GATEWAY ********");
            final Admin admin = WanUtils.getAdminForLocalCluster(this.localClusterSpaceUrl);
            admin.getProcessingUnits()
                .getProcessingUnitInstanceAdded()
                .add(new PostInitPUIListener(admin, this.gscPort, this.discoveryPort, this.myNICAddress, this.puName));

        }

    }

    public DataIterator initialLoad() throws DataSourceException {
        // ignore
        return null;
    }

    private void initIndices() {
        initPerPartitionWriteIndices();

        initReadIndices();

    }

    // /////////////
    // Accessors //
    // /////////////

    private void initLocalAddress() {
        if (this.myNICAddress == null) {
            this.myNICAddress = System.getenv().get("NIC_ADDR");
        }

        if (this.myNICAddress == null) {
            try {
                this.myNICAddress = InetAddress.getLocalHost().getHostAddress();
            } catch (final UnknownHostException e) {
                throw new IllegalStateException("Unable to configure local host address", e);
            }
        }

        logger.info("Setting local Address to: " + this.myNICAddress);

    }

    private void initLocalClusterSpaceProxy() {
        if (this.localClusterSpace != null) {
            return; // just in case this is called twice
        }
        if (this.localClusterSpaceUrl == null) {
            throw new IllegalArgumentException("The 'localClusterSpaceUrl' property must " +
                    "be set with the URL of the local space");
        }

        logger.info("Connecting to local space cluster at: " + this.localClusterSpaceUrl);

        IJSpace space = null;
        int retryCount = 0;
        boolean gotSpace = false;
        while (!gotSpace) {
            try {
                final UrlSpaceConfigurer spaceConfigurer =
                        new UrlSpaceConfigurer(this.localClusterSpaceUrl);

                space = spaceConfigurer.space();

                if (space.isOptimisticLockingEnabled() && (this.collisionHandler == null)) {
                    logger.warning("Optimistic Locking is enabled for local cluster, but no collision handler has been assigned. Using DefaultCollisionHandler");
                    this.collisionHandler = new DefaultCollisionHandler();
                }

                gotSpace = true;
            } catch (final Exception e) {
                ++retryCount;
                if (retryCount > LOCAL_SPACE_LOOKUP_RETRIES) {
                    throw new IllegalStateException("Could not connect to space at URL: "
                            + localClusterSpaceUrl + " after " + LOCAL_SPACE_LOOKUP_RETRIES + " retries. Giving up.");
                }
                
                logger.log(Level.WARNING, "Could not find space at URL: "
                        + localClusterSpaceUrl + ". Waiting for " + LOCAL_SPACE_LOOKUP_INTERVAL_SECONDS
                        + " seconds before trying again.");
                try {
                    Thread.sleep(LOCAL_SPACE_LOOKUP_INTERVAL_SECONDS * 1000);
                } catch (final InterruptedException e1) {
                    // ignore
                }

            }
        }

        try {
            localClusterTransactionManager = new DistributedJiniTxManagerConfigurer().transactionManager();
            localClusterTransactionTemplate = new TransactionTemplate(localClusterTransactionManager);

        } catch (final Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize transaction manager for local space", e);
            throw new IllegalStateException(e);
        }

        this.localClusterSpace = new GigaSpaceConfigurer(space)
            .transactionManager(localClusterTransactionManager).gigaSpace();

        final SpaceProxyImpl impl = (SpaceProxyImpl) space;
        localClusterProxyClientId = impl.getClientID();

    }

    private void initLocations() throws Exception {

        if (locationsConfiguration.size() == 0) {
            throw new IllegalArgumentException("Locations configuration is missing in Wan Mirror configuration");
        }

        if (locationsConfiguration.size() == 1) {
            throw new IllegalArgumentException("Wan Mirror Locations requires at least two locations");
        }

        final Set<String> localAddresses = createLocalAddresses();

        final boolean isSiteIdConfigured = (this.mySiteId != -1);

        allLocations = new ArrayList<WanLocation>(locationsConfiguration.size());

        int index = 1;

        if (isSiteIdConfigured) {
            for (final LocationConfiguration location : locationsConfiguration) {
                final WanLocation wanLocation = new WanLocation(index, location, null);
                wanLocation.setMe(index == this.mySiteId);
                this.allLocations.add(wanLocation);
                ++index;
            }
        } else {
            for (final LocationConfiguration location : locationsConfiguration) {
                final WanLocation wanLocation = new WanLocation(index, location, localAddresses);
                this.allLocations.add(wanLocation);
                ++index;
            }
        }

        if (this.mySiteId == -1) {
            throw new IllegalArgumentException(
                    "Could not find my location in the locations list, " +
                            "and local site ID not available in properties");
        }

        logger.info("Loaded locations configuration: " + Arrays.toString(this.allLocations.toArray()));
    }

    private void initNumberOfPartitions() {
        if (this.numberOfPartitions > 0) {
            // this is really a debugging feature, useful when using the
            // IntegratedProcessingUnitContainer
            logger.info("Number of partitions is set in configuration file to: " + this.numberOfPartitions);
            return;
        }

        final IJSpace space = this.localClusterSpace.getSpace();
        final SpaceURL url = space.getFinderURL();

        final String spaceName = url.getSpaceName();

        final Admin admin = WanUtils.getAdminForLocalCluster(this.localClusterSpaceUrl);
        final Space tempSpace = admin.getSpaces().waitFor(spaceName,
                ADMIN_LOOKUP_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        if (tempSpace == null) {
            throw new IllegalStateException("Failed to find space " + spaceName
                    + " in Admin API while checking number of partitions in local cluster. Verify that the Space URL: "
                    + url + " is accurate");
        }
        this.numberOfPartitions = tempSpace.getNumberOfInstances();

        admin.close();
        logger.info("Number of partitions in the local space cluster has been detected as: " + this.numberOfPartitions);

    }

    private void initPerPartitionWriteIndices() {
        final SQLQuery<WanEntry> query = new SQLQuery<WanEntry>(WanEntry.class,
                "siteIndex = " + this.mySiteId + " AND partitionIndex = ? ORDER BY writeIndex DESC");
        this.writeIndicesPerPartition = new AtomicLongArray(this.numberOfPartitions);
        for (int i = 0; i < this.numberOfPartitions; ++i) {
            query.setParameter(1, i);
            final WanEntry res = this.wanGigaSpace.read(query, JavaSpace.NO_WAIT);
            if (res == null) {
                this.writeIndicesPerPartition.set(i, 0);
            } else {
                this.writeIndicesPerPartition.set(i, res.getWriteIndex());
            }
        }

        if (logger.isLoggable(Level.INFO)) {
            logger.info("Set initial write indices per partition to " +
                    "the following values: "
                    + this.writeIndicesPerPartition.toString());
        }

    }

    private void initReadIndices() {
        final ReadIndex template = new ReadIndex(this.mySiteId);

        final ReadIndex[] readIndices = wanGigaSpace.readMultiple(template, Integer.MAX_VALUE);
        logger.info("Found " + readIndices.length + " existing read indices");

        for (final ReadIndex readIndex : readIndices) {
            final WanLocation location = this.allLocations.get(readIndex.getTargetSiteId());
            location.setReadIndexForPartition(readIndex.getTargetPartitionId(), readIndex.getLogIndex());
        }
    }

    private void initReadQueries() {

        for (final WanLocation location : this.allLocations) {
            startQueriesForLocation(location);
        }
    }

    private boolean initWanLUS() {

        final WanLocation loc = getLocationByIndex(this.mySiteId);

        gscPort = loc.getReplicationPort();
        discoveryPort = loc.getDiscoveryPort();

        int lrmiPort = 0;
        final ProtocolAdapter<?> prot = com.gigaspaces.lrmi.LRMIRuntime.getRuntime().getProtocolRegistry().get("NIO");
        if (prot != null) {
            lrmiPort = prot.getPort();
        }
        logger.info("LRMI port: " + lrmiPort + ", target GSC port: " + gscPort);

        if (lrmiPort == 0) {
            // LRMI layer not initialized yet - probably means that we are running in the
            // IntegratedProcessingUnitContainer
            // So nothing to do here
            logger.info("Could not find the NIO protocol adapter. " +
                    "This is normal if running in an IntegratedProcessingUnitContainer");
        } else {
            if (gscPort != lrmiPort) {
                logger.info("This GSC is not running on the required port. Creating new GSC!");
                // Must create new GSC and move this PU there.
                if (!WanUtils.checkPortAvailable(gscPort)) {
                    throw new IllegalArgumentException("The required Replication port for the new GSC(" + gscPort
                            + ") is not available!");
                }
                return false;
            }
        }

        final int currentDiscoPort = Integer.parseInt(
                System.getProperty("com.sun.jini.reggie.initialUnicastDiscoveryPort"));

        logger.info("Discovery port: " + currentDiscoPort + ", target Discovery port: " + discoveryPort);
        if (currentDiscoPort != this.discoveryPort) {
            logger.info("This GSC is not running with the required Unicast Discovery port. Creating new GSC!");
            if (!WanUtils.checkPortAvailable(this.discoveryPort)) {
                throw new IllegalArgumentException("The required discovery port for the new GSC(" + discoveryPort
                        + ") is not available!");
            }
            return false;

        }
        return true;

    }

    private void initWanSpace() {
        initEmbeddedLUS();

        final StringBuilder builder = new StringBuilder();
        for (final WanLocation location : this.allLocations) {
            builder.append(location.toLocatorString() + ",");
        }

        String locators = builder.toString();
        if (locators.endsWith(",")) {
            locators = locators.substring(0, locators.length() - 1);
        }

        // This is a debug feature, useful for enabling wan space discovery
        // via an additional LUS and available via a UI
        if (this.wanAdditionalLocators != null) {
            this.wanAdditionalLocators = this.wanAdditionalLocators.trim();
            if (this.wanAdditionalLocators.length() > 0) {

                if (!locators.endsWith(",")) {
                    locators += ",";
                }
                locators += this.wanAdditionalLocators;
            }
        }

        final String url = getWanSpaceUrl();

        logger.info("Starting embedded replicatad space with URL: " + url);
        logger.info("Starting embedded replicatad space with Locators: " + locators);
        logger.info("Starting embedded replicatad space with Groups: " + this.wanLookupGroup);
        logger.info("Starting embedded replicatad space with LUS port: "
                + getLocationByIndex(this.mySiteId).getDiscoveryPort());

        this.wanSpaceConfigurer =
                new UrlSpaceConfigurer(url).lookupLocators(locators)
        // .addProperty(com.j_spaces.kernel.SystemProperties.START_EMBEDDED_LOOKUP,
        // Boolean.TRUE.toString())
        // .addProperty(
        // com.j_spaces.core.Constants.Container.CONTAINER_EMBEDDED_HTTPD_EXPLICIT_BINDING_PORT_PROP,
        // "" + getLocationByIndex(this.mySiteId).getPort())
        ;

        if ((this.wanLookupGroup != null) && (this.wanLookupGroup.length() > 0)) {
            wanSpaceConfigurer.lookupGroups(this.wanLookupGroup);
        }

        final IJSpace space = wanSpaceConfigurer.space();

        try {
            wanTransactionManager = new LocalJiniTxManagerConfigurer(space).transactionManager();

        } catch (final Exception e) {
            logger.log(Level.SEVERE, "Failed to create transaction manager for multi-cluster space", e);
            throw new IllegalStateException(e);
        }

        wanGigaSpace = new GigaSpaceConfigurer(space).transactionManager(wanTransactionManager).gigaSpace();

    }

    private boolean isResonantBulk(final List<BulkItem> bulkItems) {
        final BulkItem firstItem = bulkItems.get(0);
        final EntryPacket packet = (EntryPacket) firstItem.getItem();
        OperationID opid = packet.getOperationID();

        // TODO: Remove this
        if (opid == null) {
            logger.severe("OPERATION ID IS NULL IN BULK!!!");
            opid = new OperationID(1, 1);
        }
        // Resonance - ignore this update
        // If they are equal, this cluster update originated from my
        // Space proxy, so this update must have originally come
        // from another site - no need to send it back out.
        // If we do, we would have
        // an endless loop of messages going back and forth.
        return (opid.getClientID() == this.localClusterProxyClientId);
    }

    public boolean isWaitForUpdatesEnabled() {
        return isWaitForUpdatesEnabled;
    }

    public DataIterator iterator(final Object arg0) throws DataSourceException {
        // ignore
        return null;
    }

    public Object read(final Object arg0) throws DataSourceException {
        // ignore
        return null;
    }

    private void readWanSiteInfos() {
        final WanSiteInfo template = new WanSiteInfo();
        final WanSiteInfo[] siteInfos = this.wanGigaSpace.readMultiple(template, allLocations.size());

        boolean myEntryExists = false;
        for (final WanSiteInfo wanSiteInfo : siteInfos) {
            final WanLocation location = getLocationByIndex(wanSiteInfo.getSiteId());
            if (location.isMe()) {
                if (wanSiteInfo.getNumberOfPartitions() != this.numberOfPartitions) {
                    logger.severe("Number of partitions for this site is " + this.numberOfPartitions
                            + " but the number of partitions listed in the Wan Space for this site is: "
                            + wanSiteInfo.getNumberOfPartitions());
                    throw new IllegalStateException("Number of partitions for this site is " + this.numberOfPartitions
                            + " but the number of partitions listed in the Wan Space for this site is: "
                            + wanSiteInfo.getNumberOfPartitions());

                }
                myEntryExists = true;
            } else {
                logger.info("Setting number of partitions in site: " + location.getSiteIndex() + " to: "
                        + wanSiteInfo.getNumberOfPartitions());
                location.setNumberOfPartitions(wanSiteInfo.getNumberOfPartitions());
            }
        }

        if (!myEntryExists) {
            final WanLocation myLocation = this.getLocationByIndex(this.mySiteId);
            this.wanGigaSpace.write(
                new WanSiteInfo(
                        this.mySiteId, this.numberOfPartitions,
                        myLocation.getName(), myLocation.getHost(),
                        myLocation.getDiscoveryPort(), myLocation.getReplicationPort()));
        }
    }

    private void registerSiteInfoQueriesForNewSites() {
        for (final WanLocation location : this.allLocations) {
            if (!location.isMe()) {
                // if number of partitions is zero then we did not find a wan site info
                // for this item
                if (location.getNumberOfPartitions() == 0) {
                    final WanSiteInfo siteInfoTemplate = new WanSiteInfo(location.getSiteIndex());
                    this.wanGigaSpace.asyncRead(siteInfoTemplate, Long.MAX_VALUE,
                            new AsyncFutureListener<WanSiteInfo>() {

                                public void onResult(final AsyncResult<WanSiteInfo> result) {
                                    handleSiteInfoQueryResult(result);

                                }

                            });
                }
            }
        }
    }

    public void setCleanupTaskInterval(final long cleanupTaskInterval) {
        this.cleanupTaskInterval = cleanupTaskInterval;
    }

    /*********
     * ClusterInfoAware implementation, used to get the PU name. If running in the Integrated PU
     * container, the received name is null, so you should set the puName property explictly in the
     * properties file.
     * 
     */
    public void setClusterInfo(final ClusterInfo clusterInfo) {
        logger.info("Wan Mirror Cluster Info: name= " + clusterInfo.getName());
        if (this.puName == null) {
            this.puName = clusterInfo.getName();
        }

    }

    public void setCollisionHandler(final CollisionHandler collisionHandler) {
        this.collisionHandler = collisionHandler;
    }

    public void setLocalClusterSpaceUrl(final String localClusterSpaceUrl) {
        this.localClusterSpaceUrl = localClusterSpaceUrl;
    }

    public void setLocalLUSLookupGroups(final String localLUSLookupGroups) {
        this.localLUSLookupGroups = localLUSLookupGroups;
    }

    public void setLocations(final List<LocationConfiguration> locations) {
        this.locationsConfiguration = locations;
    }

    public void setMyNICAddress(final String myNICAddress) {
        this.myNICAddress = myNICAddress;
    }

    public void setMySiteId(final int mySiteId) {
        this.mySiteId = mySiteId;
    }

    public void setNumberOfPartitions(final int numberOfPartitions) {
        this.numberOfPartitions = numberOfPartitions;
    }

    public void setPuName(final String puName) {
        this.puName = puName;
    }

    public void setWaitForUdatesEnabled(final boolean isWaitForUpdatesEnabled) {
        this.isWaitForUpdatesEnabled = isWaitForUpdatesEnabled;
    }

    public void setWanAdditionalLocators(final String wanAdditionalLocators) {
        this.wanAdditionalLocators = wanAdditionalLocators;
    }

    public void setWanLookupGroup(final String wanLookupGroup) {
        this.wanLookupGroup = wanLookupGroup;
    }

    public void setWanSpaceUrl(final String wanSpaceUrl) {
        this.wanSpaceUrl = wanSpaceUrl;
    }

    public void shutdown() throws DataSourceException {
        // ignore
    }

    private void startQueriesForLocation(final WanLocation location) {

        if (!location.isMe()) {

            // need the impl to create new operation IDs in the listener
            // when sending entries to the local space
            final SpaceProxyImpl impl = (SpaceProxyImpl) localClusterSpace.getSpace();

            // these are used to handle distributed transactions across multiple partitions
            // and listeners

            final int numOfPartitions = location.getNumberOfPartitions();
            for (int i = 0; i < numOfPartitions; ++i) {
                // get the current index - next value is the next index
                final long index = location.getReadIndexForPartition(i) + 1;
                // create a template that will be used with this partition
                final WanEntry template = new WanEntry(location.getSiteIndex(), i, index, null, null, null);
                // Create the listener for this partition's query
                final UpdateListener listener = new UpdateListener(template,
                        this.wanGigaSpace, this.localClusterSpace, impl, location, this.mySiteId,
                        this.localClusterTransactionTemplate, this.totalProcessedBulks,
                        this.collisionHandler);

                // Add new listener to list so it can be shut down in the future
                wanListeners.add(listener);

                // send the first query. When the listener returns, it will fire the next query
                logger.info("Waiting for update from site: " + location.getSiteIndex() + ", partition: " + i
                        + " index: " + index);
                this.wanGigaSpace.asyncRead(template, Long.MAX_VALUE, listener);

            }

        }
    }

    public void destroy() throws Exception {
        // shut down the LUS
        if (this.reggieImpl != null) {
            logger.fine("Shutting down embedded reggie");
            this.reggieImpl.destroy();
        }

        // Stop all the queries
        if (this.wanListeners != null) {

            logger.fine("Disabling per partition update listeners");
            for (UpdateListener updateListener : wanListeners) {
                // this will prevent any future blocking reads to the space
                updateListener.setEnabled(false);
            }
        }

        // Shut down the embedded space.
        if (this.wanSpaceConfigurer != null) {
            this.wanSpaceConfigurer.destroy();
        }

    }

}
