package org.openspaces.wan.mirror;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

import net.jini.core.lease.Lease;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.SpaceOptimisticLockingFailureException;
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
 * This listener receives a callback when the next update arrives for a specific site and partition.
 * This callback contains the details of a batch that arrived at the mirror of another site. The
 * bulk will be written to the local cluster in a single mahalo transaction.
 * 
 * @author barakme
 * 
 */
public class UpdateListener implements AsyncFutureListener<WanEntry> {
    private static java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(UpdateListener.class.getName());
    private final WanEntry template;
    private final GigaSpace wanGigaSpace;
    private final GigaSpace localClusterSpace;
    private final SpaceProxyImpl localClusterSpaceImpl;
    private final WanLocation location;
    private final int mySiteId;
    private final TransactionTemplate localClusterTransactionTemplate;
    private final AtomicLong entriesCounter;

    private final CollisionHandler collisionHandler;
    private boolean isVersioned;

    private volatile boolean enabled = true;

    public UpdateListener(final WanEntry template, final GigaSpace wanGigaSpace, final GigaSpace localClusterSpace,
            final SpaceProxyImpl localClusterSpaceImpl, final WanLocation location, final int mySiteId,
            final TransactionTemplate localClusterTransactionTemplate, final AtomicLong entriesCounter,
            final CollisionHandler collisionHandler) {
        super();
        this.template = template;
        this.wanGigaSpace = wanGigaSpace;
        this.localClusterSpace = localClusterSpace;
        this.localClusterSpaceImpl = localClusterSpaceImpl;
        this.location = location;
        this.mySiteId = mySiteId;
        this.localClusterTransactionTemplate = localClusterTransactionTemplate;
        this.entriesCounter = entriesCounter;
        this.collisionHandler = collisionHandler;

        this.isVersioned = this.localClusterSpace.getSpace().isOptimisticLockingEnabled();
    }

    private String createID(final WanEntry entry) {
        return entry.getSiteIndex() + "_" + entry.getPartitionIndex();
    }

    public String getId() {
        return createID(this.template);
    }

    /*********
     * This runs inside a mahalo transaction.
     * 
     * @param entry
     *            .
     */
    private void handleUpdate(final WanEntry[] entries) {
        if (entries != null) {
            writeEntriesToSpace(entries);
        }

        // Update the read index for this cluster and write it to the local cluster for HA
        final long currentIndex = template.getWriteIndex();
        final ReadIndex readIndex = new ReadIndex(this.mySiteId, template.getSiteIndex(),
                template.getPartitionIndex(), currentIndex);
        localClusterSpace.write(readIndex);

        // increment the read index
        location.setReadIndexForPartition(this.template.getPartitionIndex(), currentIndex);
        this.template.setWriteIndex(currentIndex + 1);
    }

    /*********
     * Processes a new WanEntry. This entry contains the information of the next bulk that should be
     * processed for this site and partition, according to its index. Once processing is complete,
     * the index field is incremented and a new query is issued for the next entry in the sequence.
     * 
     * Note: If this entry is part of a multi-partition transaction then the bulk will not be sent
     * to the local cluster until all the other bulks for this transaction have arrived. If they
     * have, the transaction is executed on the local cluster and normal processing continues. If
     * some of the bulks are missing, the transaction can't be reconstructed yet. As a result, the
     * bulk will not be executed, and a new query for the next entry in this partition will NOT be
     * sent. Only once all of the entries arrive will the transaction be executed and processing for
     * this partition resume.
     * 
     * 
     */
    public void onResult(final AsyncResult<WanEntry> result) {

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
                    protected void doInTransactionWithoutResult(final TransactionStatus arg0) {
                        handleUpdate(new WanEntry[] { entry });

                    }
                });

                entriesCounter.incrementAndGet();

                logger.fine("Waiting for update from site: " + location.getSiteIndex() + ", partition: "
                        + template.getPartitionIndex() + " index: " + template.getWriteIndex());

            }
        } catch (final Exception e) {
            logger.log(Level.SEVERE, "Error while processing an update from a remote site: " + e.getMessage(), e);
        } finally {
            // always make sure that the next update is issued!
            if (this.enabled) {
                wanGigaSpace.asyncRead(template, Long.MAX_VALUE, this);
            }

        }
    }

    private void writeEntriesToSpace(final WanEntry[] entries) {
        for (final WanEntry entry : entries) {
            logger.finest("Processing Entry: " + entry);
            final EntryPacket[] packets = entry.getEntryPackets();
            final short[] operationTypes = entry.getOperationTypes();
            for (int i = 0; i < packets.length; ++i) {
                final EntryPacket packet = packets[i];
                if (this.isVersioned) {
                    final int clientVersion = packet.getVersion();
                    logger.info("Current object version is: " + clientVersion);
                    packet.setVersion(clientVersion - 1);
                }

                packet.setOperationID(this.localClusterSpaceImpl.createNewOperationID());

                try {
                    switch (operationTypes[i]) {
                    case BulkItem.REMOVE:
                        localClusterSpace.take(packet);
                        break;
                        
                    case BulkItem.WRITE:
                        localClusterSpace.write(packet);
                        break;
                        
                    case BulkItem.UPDATE:
                        localClusterSpace.write(packet, Lease.FOREVER, WanDataSource.LOCAL_SPACE_ACCESS_TIMEOUT,
                                UpdateModifiers.UPDATE_OR_WRITE);
                        break;
                        
                    default:
                        logger.severe("An update was read with " +
                                "unknown operation type: " + operationTypes[i]);
                        break;

                    }
                } catch (final SpaceOptimisticLockingFailureException solfe) {

                    handleOptimisticLockingException(entry, packet, solfe);
                }

            }
        }
    }

    private void handleOptimisticLockingException(final WanEntry entry, final EntryPacket packet,
            final SpaceOptimisticLockingFailureException solfe) {
        
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Optimistic Locking Exception when writing entry: " + entry
                    + " to the local site(" + this.mySiteId + ")");
            logger.fine("Client Version: " + solfe.getClientVersionID());
            logger.fine("Space Version: " + solfe.getSpaceVersionID());
            logger.fine("Exception: " + solfe);
            logger.fine("Message: " + solfe.getMessage());
        }

        if (this.collisionHandler != null)
        {
            Object obj = packet.toObject(packet.getEntryType());
            if (obj == null)
            {
                logger.severe("Optimistic Locking failure was detected, " +
                        "but the entry of class " + packet.getClassName() +
                        " could not be deseriazlied. " +
                        "Make sure that the all classes written to the space are available " +
                        "in the classpath of the GSC. ");

            } 
            else
            {
                this.collisionHandler.handleCollision(obj, entry.getSiteIndex(), this.mySiteId,
                        solfe, localClusterSpace);
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
