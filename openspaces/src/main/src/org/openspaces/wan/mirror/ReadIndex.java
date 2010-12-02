package org.openspaces.wan.mirror;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceProperty;

@SpaceClass
public class ReadIndex {

	private int ownerSiteId;
	private int targetSiteId;
	private long logIndex;

	private int id;
	
	public ReadIndex() {

	}

	public ReadIndex(int ownerSiteId, int targetSiteId, int index) {
		super();
		this.ownerSiteId = ownerSiteId;
		this.targetSiteId = targetSiteId;
		this.logIndex = index;
		this.id = this.ownerSiteId * 1000000 + this.targetSiteId;
	}

	@SpaceId
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	@SpaceProperty(nullValue = "0")
	public int getOwnerSiteId() {
		return ownerSiteId;
	}

	public void setOwnerSiteId(int ownerSiteId) {
		this.ownerSiteId = ownerSiteId;
	}

	@SpaceProperty(nullValue = "0")
	public int getTargetSiteId() {
		return targetSiteId;
	}

	public void setTargetSiteId(int targetSiteId) {
		this.targetSiteId = targetSiteId;
	}

	@SpaceProperty(nullValue = "0")
	public long getLogIndex() {
		return logIndex;
	}

	public void setLogIndex(long logIndex) {
		this.logIndex = logIndex;
	}
}
