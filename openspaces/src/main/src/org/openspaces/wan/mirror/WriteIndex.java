package org.openspaces.wan.mirror;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceProperty;


/************
 * TODO: Remove this class. It is no longer required.
 * @author barakme
 *
 */
@SpaceClass
public class WriteIndex {

    private int siteId;
    private long index = 0;
    private int partitionId = 0;
    
    public WriteIndex(int siteId, int partitionId, long index) {
        super();
        this.siteId = siteId;
        this.partitionId = partitionId;
        this.index = index;
    }

    public WriteIndex() {
        
    }
    
    @SpaceId
    public int getSiteId() {
        return siteId;
    }
    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }
    
    @SpaceProperty(nullValue="0")
    public long getIndex() {
        return index;
    }
    public void setIndex(long index) {
        this.index = index;
    }

    public int getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(int partitionId) {
        this.partitionId = partitionId;
    }
    
}
