package org.openspaces.wan.mirror;

import java.util.logging.Level;

import net.jini.core.lease.Lease;

import org.openspaces.core.GigaSpace;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.gigaspaces.async.AsyncFutureListener;
import com.gigaspaces.async.AsyncResult;
import com.gigaspaces.datasource.BulkItem;
import com.gigaspaces.internal.client.spaceproxy.SpaceProxyImpl;
import com.gigaspaces.internal.transport.EntryPacket;
import com.j_spaces.core.client.UpdateModifiers;

/*****************
 * This listener receives a callback when the next update arrives for a specific
 * site and partition. This callback contains the details of a batch that arrived
 * at the mirror of another site. The bulk will be written to the local cluster
 * in a single mahalo transaction.
 * 
 * @author barakme
 *
 */
public class UpdateListener implements AsyncFutureListener<WanEntry> {
    private static java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(UpdateListener.class.getName());
    private WanEntry template;
    private GigaSpace wanGigaSpace;
    private GigaSpace localClusterSpace;
    private SpaceProxyImpl localClusterSpaceImpl;
    private WanLocation location;
    private int mySiteId;
    private TransactionTemplate localClusterTransactionTemplate;

    public UpdateListener(WanEntry template, GigaSpace wanGigaSpace, GigaSpace localClusterSpace,
            SpaceProxyImpl localClusterSpaceImpl, WanLocation location, int mySiteId,
            TransactionTemplate localClusterTransactionTemplate) {
        super();
        this.template = template;
        this.wanGigaSpace = wanGigaSpace;
        this.localClusterSpace = localClusterSpace;
        this.localClusterSpaceImpl = localClusterSpaceImpl;
        this.location = location;
        this.mySiteId = mySiteId;
        this.localClusterTransactionTemplate = localClusterTransactionTemplate;
    }

    public void onResult(AsyncResult<WanEntry> result) {

        try {
            final WanEntry entry = result.getResult();
            if (entry == null) {
                if (result.getException() != null) {
                    logger.log(Level.SEVERE,
                            "Async read for template: " + template +
                            " Failed with an exception: " + result.getException().getMessage(),
                            result.getException());
                }
                // will call for the same template again in finally block                

            } else {

                localClusterTransactionTemplate.execute(new TransactionCallbackWithoutResult() {

                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus arg0) {
                        handleUpdate(entry);

                    }
                });

                logger.info("Waiting for update from site: " + location.getSiteIndex() + ", partition: "
                        + template.getPartitionIndex() + " index: " + template.getWriteIndex());

            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while processing an update from a remote site: " + e.getMessage(), e);
        }finally {
            // always make sure that the next update is issued!
            wanGigaSpace.asyncRead(template, Long.MAX_VALUE, this);
        }
    }

    
    /*********
     * This runs inside a mahalo transaction.
     * 
     * @param entry .
     */
    private void handleUpdate(WanEntry entry) {
        EntryPacket[] packets = entry.getEntryPackets();
        short[] operationTypes = entry.getOperationTypes();
        for (int i = 0; i < packets.length; ++i) {
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
                localClusterSpace.write(packet, Lease.FOREVER, WanDataSource.LOCAL_SPACE_ACCESS_TIMEOUT,
                        UpdateModifiers.UPDATE_OR_WRITE);
                break;
            default:
                logger.severe("An update was read with " +
                        "unknown operation type: " + operationTypes[i]);
                break;

            }

        }

        // Update the read index for this cluster and write it to the local cluster for HA
        long currentIndex = template.getWriteIndex();
        ReadIndex readIndex = new ReadIndex(this.mySiteId, template.getSiteIndex(),
                template.getPartitionIndex(), currentIndex);
        localClusterSpace.write(readIndex);

        // increment the read index
        location.setReadIndexForPartition(this.template.getPartitionIndex(), currentIndex);
        this.template.setWriteIndex(currentIndex + 1);
    }

}
