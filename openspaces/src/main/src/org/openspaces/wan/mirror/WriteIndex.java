package org.openspaces.wan.mirror;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceProperty;

@SpaceClass
public class WriteIndex {

	private int siteId;
	private long index=0;	
	
	public WriteIndex() {
		
	}

	public WriteIndex(int siteId, long index) {
		super();
		this.siteId = siteId;
		this.index = index;
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
	
}
