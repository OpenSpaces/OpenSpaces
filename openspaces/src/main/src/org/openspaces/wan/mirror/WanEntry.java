package org.openspaces.wan.mirror;

import com.gigaspaces.internal.transport.EntryPacket;

/********************************
 * A Wan Entry contains the details of a single bulk that arrived at a mirror in one site.
 * The entry contains the operation details, as well as the site and partition IDs where
 * the bulk arrived.
 * 
 * @author barakme
 *
 */
public class WanEntry {

	private EntryPacket[] entryPackets;
	public WanEntry(int siteIndex, int partitionIndex, long writeIndex,
	        EntryPacket[] entryPackets, short[] operationTypes) {
		
	    this.partitionIndex = partitionIndex;
		this.entryPackets = entryPackets;
		this.operationTypes = operationTypes;
		this.writeIndex = writeIndex;
		this.siteIndex = siteIndex;
		
	}

	
	private short[] operationTypes;
	private long writeIndex;
	private int siteIndex;
	private int partitionIndex;

	
	
		
	public WanEntry() {
		
	}

	public long getWriteIndex() {
		return writeIndex;
	}
	public void setWriteIndex(long writeIndex) {
		this.writeIndex = writeIndex;
	}
	public int getSiteIndex() {
		return siteIndex;
	}
	public void setSiteIndex(int siteIndex) {
		this.siteIndex = siteIndex;
	}

	
	public EntryPacket[] getEntryPackets() {
		return entryPackets;
	}

	public void setEntryPackets(EntryPacket[] entryPackets) {
		this.entryPackets = entryPackets;
	}

	public short[] getOperationTypes() {
		return operationTypes;
	}

	public void setOperationTypes(short[] operationTypes) {
		this.operationTypes = operationTypes;
	}

    public int getPartitionIndex() {
        return partitionIndex;
    }

    public void setPartitionIndex(int partitionIndex) {
        this.partitionIndex = partitionIndex;
    }

    @Override
    public String toString() {
        return "WanEntry [siteIndex=" + siteIndex + ", partitionIndex=" + partitionIndex + ", writeIndex=" + writeIndex
                + "]";
    }


    
	
	
}
