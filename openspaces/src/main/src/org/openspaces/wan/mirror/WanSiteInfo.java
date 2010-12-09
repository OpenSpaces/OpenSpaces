package org.openspaces.wan.mirror;

import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceProperty;

/******************
 * An item written to the shared wan space when a mirror starts up. This lets
 * the other sites know how many partitions are available in each site.
 * 
 * @author barakme
 *
 */
public class WanSiteInfo {

    private int siteId;
    private int numberOfPartitions;
    private String name;
    private String host;
    private int port;
    
    
    
    public WanSiteInfo() {
        
    }

    public WanSiteInfo(int siteId) {
        this(siteId, 0, null, null, 0);
    }
    
    public WanSiteInfo(int siteId, int numberOfPartitions, String name, String host, int port) {
        
        this.siteId = siteId;
        this.numberOfPartitions = numberOfPartitions;
        this.name = name;
        this.host = host;
        this.port = port;
    }
    
    @SpaceId
    @SpaceProperty(nullValue = "0")
    public int getSiteId() {
        return siteId;
    }
    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }
    
    @SpaceProperty(nullValue = "0")
    public int getNumberOfPartitions() {
        return numberOfPartitions;
    }
    public void setNumberOfPartitions(int numberOfPartitions) {
        this.numberOfPartitions = numberOfPartitions;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    
    @SpaceProperty(nullValue = "0")
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
}
