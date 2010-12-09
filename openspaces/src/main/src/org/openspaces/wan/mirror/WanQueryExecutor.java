package org.openspaces.wan.mirror;

import org.openspaces.core.GigaSpace;

import net.jini.core.lease.Lease;

import com.gigaspaces.datasource.BulkItem;
import com.gigaspaces.internal.client.spaceproxy.SpaceProxyImpl;
import com.gigaspaces.internal.transport.EntryPacket;
import com.j_spaces.core.client.UpdateModifiers;

/***********
 * Deprecated - I am leaving this code here in case some snippets might be useful.
 * DELETE THIS FILE WHEN DEVELOPMENT IS FINISHED.
 * @author barakme
 *
 */
public class WanQueryExecutor implements Runnable {
	private static final int QUERY_READ_TIMEOUT = 60000;
	private static java.util.logging.Logger logger =
			java.util.logging.Logger.getLogger(WanQueryExecutor.class.getName());
	
	private final long WRITE_TIMEOUT = 10000l;
	
	private WanLocation targetLocation;
	private volatile boolean isWaitForUpdatesEnabled = true;
	private WanEntry template;	
	private GigaSpace wanGigaSpace;
	private GigaSpace localClusterSpace;
	private SpaceProxyImpl localClusterSpaceImpl;
	
	
	
	public WanQueryExecutor(GigaSpace wanGigaSpace, GigaSpace localClusterSpace,
			WanLocation targetLocation) {
		super();
		this.wanGigaSpace = wanGigaSpace;		
		this.targetLocation = targetLocation;
		this.localClusterSpace = localClusterSpace;
		this.localClusterSpaceImpl = (SpaceProxyImpl)localClusterSpace.getSpace();
		
		/*this.template = new WanEntry(null, null, 
				targetLocation.getReadIndex(), targetLocation.getSiteIndex());*/
	}

	
	
	public void run() {
		while (isWaitForUpdatesEnabled) {			
			final WanEntry entry = this.wanGigaSpace.read(this.template, QUERY_READ_TIMEOUT);
			if (entry != null) {
				EntryPacket[] packets = entry.getEntryPackets();
				short[] operationTypes = entry.getOperationTypes();
				for(int i =0;i<packets.length;++i) {
					EntryPacket packet = packets[i];
					packet.setOperationID(this.localClusterSpaceImpl.createNewOperationID());
					
					switch (operationTypes[i]) {
					case BulkItem.REMOVE:
						logger.info("REMOVING ");
						localClusterSpace.take(packet);
						break;
					case BulkItem.WRITE:
						logger.info("WRITING ");
						localClusterSpace.write(packet);

						break;
					case BulkItem.UPDATE:
						logger.info("UPDATING ");
						// TODO: Change lease constant to a field of the bean
						localClusterSpace.write(packet, Lease.FOREVER, WRITE_TIMEOUT,
								UpdateModifiers.UPDATE_OR_WRITE);
						break;
					default:
						logger.severe("An update was read with " +
								"unknown operation type: " + operationTypes[i]);
						break;

					}
				}

				// increment the read index
				/*targetLocation.incReadIndex();
				this.template.setWriteIndex(targetLocation.getReadIndex());*/
			}

		}
		
	}

	public boolean isWaitForUpdatesEnabled() {
		return isWaitForUpdatesEnabled;
	}

	public void setWaitForUpdatesEnabled(boolean isWaitForUpdatesEnabled) {
		this.isWaitForUpdatesEnabled = isWaitForUpdatesEnabled;
	}

}
