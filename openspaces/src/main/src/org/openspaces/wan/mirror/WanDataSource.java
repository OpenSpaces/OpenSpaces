package org.openspaces.wan.mirror;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jini.space.JavaSpace;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.space.Space;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.core.transaction.manager.DistributedJiniTxManagerConfigurer;
import org.openspaces.core.transaction.manager.LocalJiniTxManagerConfigurer;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.gigaspaces.async.AsyncFutureListener;
import com.gigaspaces.async.AsyncResult;
import com.gigaspaces.datasource.BulkDataPersister;
import com.gigaspaces.datasource.BulkItem;
import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.DataProvider;
import com.gigaspaces.datasource.DataSourceException;
import com.gigaspaces.internal.client.spaceproxy.SpaceProxyImpl;
import com.gigaspaces.internal.transport.EntryPacket;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.OperationID;
import com.j_spaces.core.client.SQLQuery;
import com.j_spaces.core.client.SpaceURL;

/****************************************
 * A BulkDataPersister implementation used to synchronize data across different 
 * GigaSpaces clusters. 
 * Each bulk that arrives at this data source will be written to an embedded space.
 * This space is part of a replication group along with the embedded spaces in other
 * mirrors. The mirrors in the other sites will pick up the updates and write them
 * to their local clusters.
 * 
 * 
 * @author barakme
 *
 */
@SuppressWarnings( { "unchecked", "deprecation" })
public class WanDataSource implements BulkDataPersister, DataProvider {

    // logger
    private static final Logger logger = Logger.getLogger(WanDataSource.class.getName());

    // Space access timeouts
    private static final int LOCAL_SPACE_LOOKUP_INTERVAL_SECONDS = 15;
    public static final int WAN_SPACE_ACCESS_TIMEOUT = 5000;
    public static final int LOCAL_SPACE_ACCESS_TIMEOUT = 5000;

    // String template for the URL of the wan cluster
    private static final String WAN_SPACE_URL_TEMPLATE =
            "/./wanSpace?cluster_schema=sync_replicated&total_members=<TOTAL>&id=<INDEX>";
    // ///////////////////////
    // Injected properties //
    // ///////////////////////

    // Locations of embedded LUS per each site, in format host:port[;name]
    // should be set from static config file during startup
    private List<String> locationsConfiguration = null;

    // URL for lookup of the local cluster (that this mirror belongs to)
    private String localClusterSpaceUrl;

    // 1-Based index of this site in the locations list
    // should be set in config file, or will be calculated during startup
    private int mySiteId = -1;

    // Interval between executions of the cleanup tasks
    private long cleanupTaskInterval = 30000;

    // Number of partitions in the local cluster. If 0, information is loaded
    // via the admin API, using the injected local cluster URL.
    // TODO: Check if initialized in property file, otherwise load via Admin API
    private int numberOfPartitions = 0;

    // The wan-wide clustered space
    private GigaSpace wanGigaSpace;
    // proxy back to the space cluster that this mirror belongs to.
    private GigaSpace localClusterSpace;

    // Transaction manager for the wan cluster operations
    private PlatformTransactionManager wanTransactionManager = null;

    // Transaction manager for the local cluster operations
    // TODO: Chance local cluster txn manager to embedded mahalo
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

    private WanEntry createWANEntry(final List<BulkItem> bulkItems) {

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
                packets, operationTypes);
        return entry;
    }

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

        final WanEntry entry = createWANEntry(bulkItems);

        logger.fine("********** EXECUTE BULK ON MIRROR ********");
        wanGigaSpace.write(entry);

        this.writeIndicesPerPartition.incrementAndGet(
            entry.getPartitionIndex());

    }

    public long getCleanupTaskInterval() {
        return cleanupTaskInterval;
    }

    public String getLocalClusterSpaceUrl() {
        return localClusterSpaceUrl;
    }

    private WanLocation getLocationByIndex(final int index) {
        return this.allLocations.get(index - 1);
    }

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

    public List<String> getLocations() {
        return locationsConfiguration;
    }

    public int getMySiteId() {
        return mySiteId;
    }

    public int getNumberOfPartitions() {
        return numberOfPartitions;
    }

    private String getURLString(final Object[] params) {
        final StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (final Object param : params) {

            if (first) {
                builder.append(param.toString());
                first = false;
            } else {
                builder.append(",");
                builder.append(param.toString());
            }
        }

        final String res = builder.toString();
        return res;
    }

    private String getWanSpaceUrl() {
        final String res = WAN_SPACE_URL_TEMPLATE.replace("<TOTAL>", "" + this.allLocations.size())
            .replace("<INDEX>", "" + this.mySiteId);
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
        
        final WanLocation location =this.getLocationByIndex(targetSite); 
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

    public void initialize() throws Exception {

        initLocations();

        initLocalClusterSpaceProxy();

        initNumberOfPartitions();

        initWanSpace();

        initClusterJoin();

        initIndices();

        initReadQueries();

        logger.info("******** WAN MIRROR GATEWAY WAS INITIALIZED CORRECTLY ********");

    }

    public DataIterator initialLoad() throws DataSourceException {
        // ignore
        return null;
    }

    private void initIndices() {
        initPerPartitionWriteIndices();

        initReadIndices();

    }

    private void initLocalClusterSpaceProxy() {
        if (this.localClusterSpaceUrl == null) {
            throw new IllegalArgumentException("The 'localClusterSpaceUrl' property must " +
                    "be set with the URL of the local space");
        }

        IJSpace space = null;
        boolean gotSpace = false;
        while (!gotSpace) {
            try {
                final UrlSpaceConfigurer spaceConfigurer =
                        new UrlSpaceConfigurer(this.localClusterSpaceUrl);

                space = spaceConfigurer.space();
                gotSpace = true;
            } catch (final Exception e) {
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

        if (locationsConfiguration == null) {
            throw new Exception("Locations configuration is missing in Wan Mirror configuration");
        }

        if (locationsConfiguration.size() < 2) {
            throw new Exception("Wan Mirror Locations requires at least two locations");
        }

        final Set<String> localAddresses = createLocalAddresses();

        final boolean isSiteIdConfigured = (this.mySiteId != -1);

        allLocations = new ArrayList<WanLocation>(locationsConfiguration.size());

        int index = 1;

        if (isSiteIdConfigured) {
            for (final String location : locationsConfiguration) {
                final WanLocation wanLocation = new WanLocation(index, location, null);
                wanLocation.setMe(index == this.mySiteId);
                this.allLocations.add(wanLocation);
                ++index;
            }
        } else {
            for (final String location : locationsConfiguration) {
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

        final AdminFactory factory = new AdminFactory();
        if (url.getLookupLocators().length > 0) {
            factory.addLocators(getURLString(url.getLookupLocators()));
        }
        if (url.getLookupGroups().length > 0) {
            factory.addGroups(getURLString(url.getLookupGroups()));
        }

        // .addLocator("192.168.10.49");//

        final String spaceName = url.getSpaceName();
        final Admin admin = factory.createAdmin();

        // GridServiceContainer[] gscs = admin.getGridServiceContainers().getContainers();
        // Space[] spaces = admin.getSpaces().getSpaces();
        // LookupService[] luses = admin.getLookupServices().getLookupServices();
        final Space tempSpace = admin.getSpaces().waitFor(spaceName, 10, TimeUnit.SECONDS);

        if (tempSpace == null) {
            throw new IllegalStateException("Failed to find space " + spaceName
                    + " in Admin API while checking number of partitions in local cluster. Verify that the Space URL: "
                    + url + " is accurate");
        }
        this.numberOfPartitions = tempSpace.getNumberOfInstances();

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

    // /////////////
    // Accessors //
    // /////////////

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

    private void startQueriesForLocation(final WanLocation location) {
        // need the impl to create new operation IDs in the listener
        // when sending entries to the local space
        SpaceProxyImpl impl = (SpaceProxyImpl) localClusterSpace.getSpace();
        if (!location.isMe()) {
            final int numOfPartitions = location.getNumberOfPartitions();
            for (int i = 0; i < numOfPartitions; ++i) {
                // get the current index - next value is the next index
                final long index = location.getReadIndexForPartition(i) + 1;
                // create a template that will be used with this partition
                final WanEntry template = new WanEntry(location.getSiteIndex(), i, index, null, null);
                // Create the listener for this partition's query
                final AsyncFutureListener<WanEntry> listener = new UpdateListener(template,
                        this.wanGigaSpace, this.localClusterSpace, impl, location, this.mySiteId,
                        this.localClusterTransactionTemplate);
                // send the first query. When the listener returns, it will fire the next query
                logger.info("Waiting for update from site: " + location.getSiteIndex() + ", partition: " + i + " index: " + index);
                this.wanGigaSpace.asyncRead(template, Long.MAX_VALUE, listener);

            }

        }
    }
    
    

    private void initWanSpace() {
        final StringBuilder builder = new StringBuilder();
        for (final WanLocation location : this.allLocations) {
            builder.append(location.toLocatorString() + ",");
        }

        String locators = builder.toString();
        if (locators.endsWith(",")) {
            locators = locators.substring(0, locators.length() - 1);
        }

        // TODO: Make this a debug feature, off by default
        locators += ",localhost:4164";

        final String url = getWanSpaceUrl();
        final UrlSpaceConfigurer spaceConfigurer =
                new UrlSpaceConfigurer(url).lookupLocators(locators).lookupGroups("WAN")
            .addProperty(com.j_spaces.kernel.SystemProperties.START_EMBEDDED_LOOKUP,
                Boolean.TRUE.toString())
            .addProperty(com.j_spaces.core.Constants.Container.CONTAINER_EMBEDDED_HTTPD_EXPLICIT_BINDING_PORT_PROP,
                "" + getLocationByIndex(this.mySiteId).getPort());

        // This does not work because the constant was already initialized when
        // the mirror
        // started up

        /*
         * .addProperty(net.jini.discovery.Constants.MULTICAST_ENABLED_PROPERTY,
         * Boolean.FALSE.toString());
         */

        final IJSpace space = spaceConfigurer.space();

        try {
            wanTransactionManager = new LocalJiniTxManagerConfigurer(space).transactionManager();

        } catch (final Exception e) {
            logger.log(Level.SEVERE, "Failed to create transaction manager for multi-cluster space", e);
            throw new IllegalStateException(e);
        }

        wanGigaSpace = new GigaSpaceConfigurer(space).transactionManager(wanTransactionManager).gigaSpace();
    }

    public boolean isEmbeddedLusRunning() {
        final String sysProp = System.getProperty(com.j_spaces.kernel.SystemProperties.START_EMBEDDED_LOOKUP);
        if ((sysProp != null) && sysProp.equals("true")) {
            return true;
        }

        return false;
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
                    myLocation.getName(), myLocation.getHost(), myLocation.getPort()));
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

    public void setLocalClusterSpaceUrl(final String localClusterSpaceUrl) {
        this.localClusterSpaceUrl = localClusterSpaceUrl;
    }

    public void setLocations(final List<String> locations) {
        this.locationsConfiguration = locations;
    }

    public void setMySiteId(final int mySiteId) {
        this.mySiteId = mySiteId;
    }

    public void setNumberOfPartitions(final int numberOfPartitions) {
        this.numberOfPartitions = numberOfPartitions;
    }

    public void setWaitForUdatesEnabled(final boolean isWaitForUpdatesEnabled) {
        this.isWaitForUpdatesEnabled = isWaitForUpdatesEnabled;
    }

    public void shutdown() throws DataSourceException {
        // ignore
    }

}
