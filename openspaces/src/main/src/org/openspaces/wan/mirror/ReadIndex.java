package org.openspaces.wan.mirror;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceProperty;
import com.gigaspaces.internal.metadata.annotations.SpaceSystemClass;

/**************************
 * A ReadIndex is the index of the multi-cluster mirror in the list of operations
 * sent from a specific partition and site.
 * 
 * @author barakme
 *
 */
@SpaceClass
@SpaceSystemClass
public class ReadIndex {

    private int ownerSiteId;

    private int targetSiteId;
    private int targetPartitionId;
    private long logIndex;

    private int id;

    public ReadIndex() {

    }

    /********
     * Constructor for template objects used in querying all indices for the same owner site.
     * 
     * @param ownerSiteId
     *            the owner site ID.
     */
    public ReadIndex(final int ownerSiteId) {

        this.ownerSiteId = ownerSiteId;
        this.targetSiteId = 0;
        this.targetPartitionId = 0;
        this.logIndex = 0;

        this.id = 0;
    }

    public ReadIndex(final int ownerSiteId, final int targetSiteId, final int targetPartitionId, final long logIndex) {
        this.ownerSiteId = ownerSiteId;
        this.targetSiteId = targetSiteId;
        this.targetPartitionId = targetPartitionId;
        this.logIndex = logIndex;

        this.id = this.targetSiteId * 1000000 + this.targetPartitionId * 1000 + this.ownerSiteId;
    }

    @SpaceId
    @SpaceProperty(nullValue = "0")
    public int getId() {
        return id;
    }

    @SpaceProperty(nullValue = "0")
    public long getLogIndex() {
        return logIndex;
    }

    @SpaceProperty(nullValue = "0")
    public int getOwnerSiteId() {
        return ownerSiteId;
    }

    @SpaceProperty(nullValue = "0")
    public int getTargetPartitionId() {
        return targetPartitionId;
    }

    @SpaceProperty(nullValue = "0")
    public int getTargetSiteId() {
        return targetSiteId;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public void setLogIndex(final long logIndex) {
        this.logIndex = logIndex;
    }

    public void setOwnerSiteId(final int ownerSiteId) {
        this.ownerSiteId = ownerSiteId;
    }

    public void setTargetPartitionId(final int targetPartitionId) {
        this.targetPartitionId = targetPartitionId;
    }

    public void setTargetSiteId(final int targetSiteId) {
        this.targetSiteId = targetSiteId;
    }

    @Override
    public String toString() {
        return "ReadIndex [ownerSiteId=" + ownerSiteId + ", targetSiteId=" + targetSiteId + ", targetPartitionId="
                + targetPartitionId + ", logIndex=" + logIndex + "]";
    }

}
