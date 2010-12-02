package org.openspaces.wan.mirror;

import com.gigaspaces.internal.transport.EntryPacket;

public class WanEntry {

	private EntryPacket[] entryPackets;
	public WanEntry(EntryPacket[] entryPackets, short[] operationTypes, long writeIndex, int siteIndex) {
		super();
		this.entryPackets = entryPackets;
		this.operationTypes = operationTypes;
		this.writeIndex = writeIndex;
		this.siteIndex = siteIndex;
	}

	
	private short[] operationTypes;
	private long writeIndex;
	private int siteIndex;

	
	
		
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
	
	
}
