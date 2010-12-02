package org.openspaces.wan.mirror;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.core.transaction.manager.LocalJiniTxManagerConfigurer;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.gigaspaces.datasource.BulkDataPersister;
import com.gigaspaces.datasource.BulkItem;
import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.DataProvider;
import com.gigaspaces.datasource.DataSourceException;
import com.gigaspaces.internal.client.spaceproxy.SpaceProxyImpl;
import com.gigaspaces.internal.transport.EntryPacket;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.OperationID;
import com.j_spaces.core.client.ReadModifiers;
import com.j_spaces.core.client.SQLQuery;
import com.j_spaces.core.client.UpdateModifiers;

public class WanDataSource implements BulkDataPersister, DataProvider {

	private static final int LOCAL_SPACE_LOOKUP_INTERVAL_SECONDS = 15;

	private static final int SPACE_ACCESS_TIMEOUT = 5000;

	private static final Logger logger = Logger.getLogger(WanDataSource.class.getName());

	private List<String> locationsConfiguration = null;

	private List<WanLocation> allLocations = null;
	// Modify this appropriately
	private final long WRITE_TIMEOUT = 10000l;

	private GigaSpace wanGigaSpace;
	private GigaSpace localClusterSpace;

	private int mySiteId = -1;
	private String localClusterSpaceUrl;

	private long writeIndex;

	private SQLQuery<WanEntry> nextUpdateQuery;

	private ExecutorService queryExecutors;

	private long localClusterProxyClientId;

	private boolean isWaitForUpdatesEnabled = true;

	private SQLQuery<WanEntry> deleteLogQuery;

	private ScheduledExecutorService cleanupTaskExecutor;

	private long cleanupTaskInterval = 30000;

	private PlatformTransactionManager wanTransactionManager = null;

	private PlatformTransactionManager localClusterTransactionManager;

	private TransactionTemplate wanTransactionTemplate;

	private TransactionTemplate localClusterTransactionTemplate;

	private Set<String> createLocalAddresses() throws SocketException, UnknownHostException {
		final Set<String> localAddresses = new HashSet<String>();

		/*
		 * Enumeration<NetworkInterface> nics =
		 * NetworkInterface.getNetworkInterfaces();
		 * while(nics.hasMoreElements()){ NetworkInterface nic =
		 * nics.nextElement(); Enumeration<InetAddress> addressess =
		 * nic.getInetAddresses(); while(addressess.hasMoreElements()) {
		 * InetAddress address = addressess.nextElement();
		 * localAddresses.add(address.getCanonicalHostName());
		 * localAddresses.add(address.getHostAddress());
		 * localAddresses.add(address.getHostName()); } }
		 */

		localAddresses.add(InetAddress.getLocalHost().getHostAddress());
		localAddresses.add(InetAddress.getLocalHost().getHostName());
		return localAddresses;
	}

	public void executeBulk(final List<BulkItem> bulkItems)
			throws DataSourceException {

		if (bulkItems.size() == 0) {
			logger.warning("Recieved a bulk update with no items");
			return;
		}

		if (isResonantBulk(bulkItems)) {
			logger.fine("Detected a resonant bulk update - ignoring");
			return;
		}

		EntryPacket[] packets = new EntryPacket[bulkItems.size()];
		short[] operationTypes = new short[bulkItems.size()];
		int i = 0;
		logger.info("********** EXECUTE BULK ON MIRROR ********");
		for (final BulkItem bulkItem : bulkItems) {
			// logger.info(bulkItem.toString());

			final Object item = bulkItem.getItem();
			final EntryPacket packet = (EntryPacket) item;

			packets[i] = packet;
			operationTypes[i] = bulkItem.getOperation();
			++i;
		}

		// TODO: Should write index be cached here, or loaded from the
		// space?
		final WriteIndex index = new WriteIndex(this.mySiteId, this.writeIndex + 1);
		final WanEntry entry =
				new WanEntry(packets, operationTypes, this.writeIndex + 1, this.mySiteId);

		localClusterTransactionTemplate.execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(final TransactionStatus arg0) {
				wanGigaSpace.write(entry);
				wanGigaSpace.write(index);
			}
		});

		// TODO: Is this good enough? What if there is a thread failure here?
		// TODO: THIS IS NOT NEEDED - I SHOULD BE LOCKING THE WRITE INDEX ON THE
		// SPACE!
		++this.writeIndex;

	}

	private boolean isResonantBulk(final List<BulkItem> bulkItems) {
		BulkItem firstItem = bulkItems.get(0);
		final EntryPacket packet = (EntryPacket) firstItem.getItem();
		OperationID opid = packet.getOperationID();
		System.out.println("Op ID is: " + opid);

		// TODO: remove this!
		if (opid == null) {
			opid = new OperationID(1, 1);
		}
		// Resonance - ignore this update
		// This means that this cluster update originated from my
		// Space proxy, so this update must have originally come
		// from another
		// site, so no need to send it back out. If we do, we would
		// have
		// an endless loop of messages going back and forth.

		return (opid.getClientID() == this.localClusterProxyClientId);
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

	public List<String> getLocations() {
		return locationsConfiguration;
	}

	public void init(final Properties arg0) throws DataSourceException {
		// ignore
	}

	private void initCleanUpTaskExecutor() {

		cleanupTaskExecutor = Executors.newScheduledThreadPool(1);
		final Runnable cleanUpTask = new Runnable() {

			public void run() {
				runLogCleanUpTaskInTransaction();
			}
		};

		cleanupTaskExecutor.scheduleWithFixedDelay(cleanUpTask,
				10, cleanupTaskInterval, TimeUnit.MILLISECONDS);

	}

	public boolean isEmbeddedLusRunning() {
		String sysProp = System.getProperty(com.j_spaces.kernel.SystemProperties.START_EMBEDDED_LOOKUP);
		if ((sysProp != null) && sysProp.equals("true")) {
			return true;
		}

		return false;
	}

	public void initialize() throws Exception {

		initLocations();

		initLocalClusterSpaceProxy();

		initWanSpace();

		initIndices();

		initSQLQueryForRead();

		initSQLQueryForTake();

		initQueryExecutors();

		initCleanUpTaskExecutor();

	}

	public void stopQueryExecutors() {
		for (WanLocation location : this.allLocations) {
			if (!location.isMe()) {
				location.getQueryExecutor().setWaitForUpdatesEnabled(false);
				location.setQueryExecutor(null);
			}
		}
	}

	private void initQueryExecutors() {
		queryExecutors = Executors.newFixedThreadPool(this.allLocations.size() - 1);
		for (WanLocation location : this.allLocations) {
			if (!location.isMe()) {
				WanQueryExecutor exec = new WanQueryExecutor(this.wanGigaSpace,
						this.localClusterSpace, location);
				location.setQueryExecutor(exec);
				this.queryExecutors.execute(exec);

			}
		}
	}

	public DataIterator initialLoad() throws DataSourceException {
		// ignore
		return null;
	}

	private void initIndices() {
		initWriteIndex();

		initReadIndices();

	}

	private void initReadIndices() {
		final ReadIndex template = new ReadIndex(this.mySiteId, 0, 0);

		final ReadIndex[] readIndices = wanGigaSpace.readMultiple(template, this.allLocations.size() + 1);

		logger.info("Found " + readIndices.length + " existing read indices");

		for (final WanLocation location : this.allLocations) {
			boolean found = false;
			for (final ReadIndex readIndex : readIndices) {
				if (location.getSiteIndex() == readIndex.getTargetSiteId()) {
					found = true;
					location.setReadIndex(readIndex.getLogIndex());
				}
			}

			if ((!location.isMe()) && (!found)) {
				logger.info("Creating a ReadIndex for site "
						+ this.mySiteId + ", target: " + location.getSiteIndex());
				final ReadIndex readIndex = new ReadIndex(this.mySiteId, location.getSiteIndex(), 1);
				wanGigaSpace.write(readIndex);
				location.setReadIndex(readIndex.getLogIndex());
			}
		}
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
			} catch (Exception e) {
				logger.log(Level.WARNING, "Could not find space at URL: "
						+ localClusterSpaceUrl + ". Waiting for " + LOCAL_SPACE_LOOKUP_INTERVAL_SECONDS
						+ " seconds before trying again.");
				try {
					Thread.sleep(LOCAL_SPACE_LOOKUP_INTERVAL_SECONDS * 1000);
				} catch (InterruptedException e1) {
					// ignore
				}

			}
		}

		try {
			localClusterTransactionManager = new LocalJiniTxManagerConfigurer(space).transactionManager();
			localClusterTransactionTemplate = new TransactionTemplate(localClusterTransactionManager);

		} catch (final Exception e) {
			logger.log(Level.SEVERE, "Failed to initialize transaction manager for local space", e);
			throw new IllegalStateException(e);
		}

		this.localClusterSpace = new GigaSpaceConfigurer(space).gigaSpace();

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

	private String sqlQueryRead;

	private void initSQLQueryForRead() {
		final StringBuilder queryBuilder = new StringBuilder();
		boolean first = true;
		for (final WanLocation location : this.allLocations) {
			if (!location.isMe()) {
				if (first) {
					first = false;
				} else {
					queryBuilder.append(" OR ");
				}
				queryBuilder.append("(siteIndex = " + 1 // location.getSiteIndex()
						// TODO: Uncomment this
						+ " AND writeIndex = ?)");
			}
		}

		final String queryString = queryBuilder.toString();
		sqlQueryRead = queryString; // TODO: remove
		nextUpdateQuery = new SQLQuery<WanEntry>(WanEntry.class, queryString);
	}

	private void initSQLQueryForTake() {
		final StringBuilder queryBuilder = new StringBuilder();
		boolean first = true;
		final int howmany = this.allLocations.size();
		for (int i = 0; i < howmany; ++i) {

			if (first) {
				first = false;
			} else {
				queryBuilder.append(" OR ");
			}
			queryBuilder.append("(siteIndex = ? AND writeIndex = ?)");

		}

		final String queryString = queryBuilder.toString();
		deleteLogQuery = new SQLQuery<WanEntry>(WanEntry.class, queryString);
	}

	private static final String WAN_SPACE_URL_TEMPLATE =
			"/./wanSpace?cluster_schema=sync_replicated&total_members=<TOTAL>&id=<INDEX>";

	private String getWanSpaceUrl() {
		String res = WAN_SPACE_URL_TEMPLATE.replace("<TOTAL>", "" + this.allLocations.size())
				.replace("<INDEX>", "" + this.mySiteId);
		return res;

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
				new UrlSpaceConfigurer(url).lookupLocators(locators)
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
			wanTransactionTemplate = new TransactionTemplate(wanTransactionManager);

		} catch (final Exception e) {
			logger.log(Level.SEVERE, "Failed to create transaction manager for multi-cluster space", e);
			throw new IllegalStateException(e);
		}

		wanGigaSpace = new GigaSpaceConfigurer(space).gigaSpace();
	}

	private void initWriteIndex() {
		final WriteIndex writeTemplate = new WriteIndex(this.mySiteId, 0);

		WriteIndex result = wanGigaSpace.read(writeTemplate, JavaSpace.NO_WAIT);
		if (result == null) {
			result = new WriteIndex(this.mySiteId, 0);
			wanGigaSpace.write(result, Lease.FOREVER, SPACE_ACCESS_TIMEOUT, UpdateModifiers.WRITE_ONLY);
		}

		this.writeIndex = result.getIndex();
	}

	private boolean isTimeForCleanUp() {
		final LogCleanupTimestamp template = new LogCleanupTimestamp(0);
		final LogCleanupTimestamp timeStamp = wanGigaSpace.read(template, ReadModifiers.EXCLUSIVE_READ_LOCK);
		if (timeStamp == null) { // first time) {
			return true;
		}
		final long now = System.currentTimeMillis();
		return (now - timeStamp.getTimeStamp() > 30 * 1000);

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

	private void runLogCleanUpTask(final TransactionStatus status) {

		try {
			// first get the log cleanup time stamp
			if (!isTimeForCleanUp()) {
				return;
			}

			// first collect all Read indices
			final ReadIndex template = new ReadIndex(0, 0, 0);
			final int numOfLocations = this.locationsConfiguration.size();
			final int numOfReadIndices = numOfLocations * (numOfLocations - 1);

			final ReadIndex[] readIndices = this.wanGigaSpace.readMultiple(template, numOfReadIndices);

			final Map<Integer, Long> minValBySiteId = new HashMap<Integer, Long>();
			for (final WanLocation location : this.allLocations) {
				minValBySiteId.put(location.getSiteIndex(), Long.MAX_VALUE);
			}

			for (final ReadIndex readIndex : readIndices) {
				final int site = readIndex.getTargetSiteId();
				final long val = readIndex.getLogIndex();
				final long oldVal = minValBySiteId.get(site);
				minValBySiteId.put(site, Math.min(oldVal, val));
			}

			int paramIndex = 1;
			for (final Map.Entry<Integer, Long> entry : minValBySiteId.entrySet()) {
				this.deleteLogQuery.setParameter(paramIndex, entry.getKey());
				++paramIndex;
				// NOTE: to avoid any issues with transactionality, it is better
				// to
				// delete
				// old logs up to the last one, just in case we get some sort of
				// end
				// case update
				// where another transaction is still using these entries.
				// This is not supposed to occur, but better safe then sorry.
				// The entries we leave behind will be deleted on the next
				// iteration
				// of the
				// log clean up task
				this.deleteLogQuery.setParameter(paramIndex, entry.getValue() - 2);
				++paramIndex;
			}

			final WanEntry[] wanEntries = wanGigaSpace.takeMultiple(this.deleteLogQuery, Integer.MAX_VALUE);
			logger.info("Removed " + wanEntries.length + " elements from the Log Table");

			final LogCleanupTimestamp cleanUpCompletion = new LogCleanupTimestamp(System.currentTimeMillis());
			wanGigaSpace.write(cleanUpCompletion, Lease.FOREVER, SPACE_ACCESS_TIMEOUT, UpdateModifiers.UPDATE_OR_WRITE);

		} catch (final Exception e) {
			logger.log(Level.SEVERE, "Log clean up task failed! Problem was: " + e.getMessage(), e);
			status.setRollbackOnly();

		}

	}

	private void runLogCleanUpTaskInTransaction() {
		this.wanTransactionTemplate.execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(final TransactionStatus status) {
				runLogCleanUpTask(status);
			}
		});

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

	
	public void setWaitForUdatesEnabled(final boolean isWaitForUpdatesEnabled) {
		this.isWaitForUpdatesEnabled = isWaitForUpdatesEnabled;
	}

	public void shutdown() throws DataSourceException {
		// ignore
	}


	public int getMySiteId() {
		return mySiteId;
	}

	public void setMySiteId(int mySiteId) {
		this.mySiteId = mySiteId;
	}
}
