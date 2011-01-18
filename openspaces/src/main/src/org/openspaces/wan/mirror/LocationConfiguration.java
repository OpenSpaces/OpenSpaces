package org.openspaces.wan.mirror;

public class LocationConfiguration {

    
    
    private String host;
    private String name;
    private int discoveryPort;
    private int replicationPort;
    
    public LocationConfiguration() {
    
    }
    public LocationConfiguration(String host, int discoveryPort, int replicationPort, String name) {

        this.discoveryPort = discoveryPort;
        this.replicationPort = replicationPort;
        this.host = host;
        this.name = name;
    }
    
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
   
    public int getDiscoveryPort() {
        return discoveryPort;
    }
    public void setDiscoveryPort(int discoveryPort) {
        this.discoveryPort = discoveryPort;
    }
    public int getReplicationPort() {
        return replicationPort;
    }
    public void setReplicationPort(int replicationPort) {
        this.replicationPort = replicationPort;
    }
    
}
