package org.openspaces.wan.mirror;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WanLocation {

    private String host;

    private int discoveryPort = 0;
    private int replicationPort = 0;
    private String name;
    private boolean isMe = false;
    private int numberOfPartitions;
    private int siteIndex;

    // This has to be a concurrent hash map because changes from each partition can arrive
    // concurrently, though only one at a time per partition
    private final Map<Integer, Long> readIndexByPartitionId = new ConcurrentHashMap<Integer, Long>();

    public WanLocation(final int index, final LocationConfiguration location, final Set<String> localAddresses) {

        this.siteIndex = index;
        this.host = location.getHost();
        this.discoveryPort = location.getDiscoveryPort();
        this.replicationPort = location.getReplicationPort();

        if ((location.getName() == null) || (location.getName().length() == 0)) {
            this.name = this.host + ":" + this.discoveryPort;
        } else {
            this.name = location.getName();
        }

        if (localAddresses != null) {
            this.isMe = localAddresses.contains(host);
        }

    }

    public String getHost() {
        return host;
    }

    public String getName() {
        return name;
    }

    public int getNumberOfPartitions() {
        return numberOfPartitions;
    }

    public long getReadIndexForPartition(final int partitionId) {
        final Long val = this.readIndexByPartitionId.get(partitionId);
        if (val == null) {
            return 0;
        }

        return val;
    }

    public int getSiteIndex() {
        return siteIndex;
    }

    public long incReadIndexForPartition(final int partitionId) {
        long newval = 1;

        final Long val = this.readIndexByPartitionId.get(partitionId);
        if (val != null) {
            newval = val.longValue() + 1;
        }

        this.readIndexByPartitionId.put(partitionId, newval);
        return newval;
    }

    public boolean isMe() {
        return isMe;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public void setMe(final boolean isMe) {
        this.isMe = isMe;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setNumberOfPartitions(final int numberOfPartitions) {
        this.numberOfPartitions = numberOfPartitions;
    }

    public void setReadIndexForPartition(final int partitionId, final long newValue) {
        this.readIndexByPartitionId.put(partitionId, newValue);
    }

    public void setSiteIndex(final int siteIndex) {
        this.siteIndex = siteIndex;
    }

    public String toLocatorString() {
        return this.host + ":" + this.discoveryPort;
    }

    @Override
    public String toString() {
        return "WanLocation [siteIndex=" + siteIndex + ", name=" + name + ", host=" + host + ", discoveryPort="
                + this.discoveryPort
                + ", replicationPort=" + this.replicationPort + ", isMe=" + isMe + ", numberOfPartitions="
                + numberOfPartitions + "]";
    }

    public int getDiscoveryPort() {
        return discoveryPort;
    }

    public void setDiscoveryPort(final int discoveryPort) {
        this.discoveryPort = discoveryPort;
    }

    public int getReplicationPort() {
        return replicationPort;
    }

    public void setReplicationPort(final int replicationPort) {
        this.replicationPort = replicationPort;
    }

}
