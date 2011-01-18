package org.openspaces.wan.mirror;

import com.gigaspaces.annotation.pojo.SpaceIndex;
import com.gigaspaces.annotation.pojo.SpaceProperty;
import com.gigaspaces.internal.transport.EntryPacket;
import com.gigaspaces.metadata.index.SpaceIndexType;

/********************************
 * A Wan Entry contains the details of a single bulk that arrived at a mirror in one site.
 * The entry contains the operation details, as well as the site and partition IDs where
 * the bulk arrived.
 * 
 * @author barakme
 *
 */
public class WanEntry {

    public static class TxnData {
        private long txnId;
        private int participantId;
        private int pacticipantCount;
        
        public TxnData(long txnId, int participantId, int pacticipantCount) {
            super();
            this.txnId = txnId;
            this.participantId = participantId;
            this.pacticipantCount = pacticipantCount;
        }
        
        
        public TxnData() {
            super();
        }

        @SpaceIndex(type=SpaceIndexType.BASIC)
        public long getTxnId() {
            return txnId;
        }
        
        public void setTxnId(long txnId) {
            this.txnId = txnId;
        }
        
        @SpaceProperty(nullValue = "0")
        public int getParticipantId() {
            return participantId;
        }
        public void setParticipantId(int participantId) {
            this.participantId = participantId;
        }
        
        @SpaceProperty(nullValue = "0")
        public int getPacticipantCount() {
            return pacticipantCount;
        }
        public void setPacticipantCount(int pacticipantCount) {
            this.pacticipantCount = pacticipantCount;
        }
        
    }
    
    private EntryPacket[] entryPackets;
    private short[] operationTypes;
    private long writeIndex;
    private int siteIndex;
    private int partitionIndex;
    
    private TxnData txnData;
    
    public WanEntry(int siteIndex, int partitionIndex, long writeIndex,
            EntryPacket[] entryPackets, short[] operationTypes, TxnData txnData) {
        
        this.partitionIndex = partitionIndex;
        this.entryPackets = entryPackets;
        this.operationTypes = operationTypes;
        this.writeIndex = writeIndex;
        this.siteIndex = siteIndex;
        this.txnData = txnData;
        
    }
            
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

    public TxnData getTxnData() {
        return txnData;
    }

    public void setTxnData(TxnData txnData) {
        this.txnData = txnData;
    }


    
    
    
}
