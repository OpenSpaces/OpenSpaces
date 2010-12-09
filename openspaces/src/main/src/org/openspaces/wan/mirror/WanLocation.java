package org.openspaces.wan.mirror;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

public class WanLocation {

    private static final int DEFAULT_LOCATION_PORT = 10000;

    private String host;
    private int port = DEFAULT_LOCATION_PORT;
    private String name;
    private boolean isMe = false;
    private int numberOfPartitions;

    private int siteIndex;

    // This has to be a concurrent hash map because changes from each partition can arrive
    // concurrently, though only one at a time per partition
    private Map<Integer, Long> readIndexByPartitionId = new ConcurrentHashMap<Integer, Long>();

    public WanLocation(final int index, final String location, final Set<String> localAddresses) {

        this.siteIndex = index;
        final StringTokenizer tokenizer = new StringTokenizer(location, ";");
        final String hostAndPort = tokenizer.nextToken();

        if (hostAndPort.contains(":")) {
            final String[] parts = hostAndPort.split(":");
            this.host = parts[0];
            this.port = Integer.parseInt(parts[1]);
        } else {
            this.host = hostAndPort;
            this.port = DEFAULT_LOCATION_PORT;
        }

        /*
         * try { final String partitions = tokenizer.nextToken(); this.numberOfPartitions =
         * Integer.parseInt(partitions); } catch(NoSuchElementException nse) {
         * logger.severe("Could not find number of partitions in the location descriptor: " +
         * location); throw new
         * IllegalArgumentException("Could not find number of partitions in the location descriptor: "
         * + location); } catch(NumberFormatException nfe) {
         * logger.severe("Illegal number of partitions in location descriptor: " + location); throw
         * new IllegalArgumentException("Illegal number of partitions in location descriptor: " +
         * location); }
         */

        try {
            final String parsedName = tokenizer.nextToken();
            this.name = parsedName;
        } catch (NoSuchElementException e) {
            this.name = this.host + ":" + this.port;
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

    public int getPort() {
        return port;
    }

    public int getSiteIndex() {
        return siteIndex;
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

    public void setPort(final int port) {
        this.port = port;
    }

    public void setSiteIndex(final int siteIndex) {
        this.siteIndex = siteIndex;
    }

    public String toLocatorString() {
        return this.host + ":" + this.port;
    }

    public long getReadIndexForPartition(int partitionId) {
        Long val = this.readIndexByPartitionId.get(partitionId);
        if (val == null) {
            return 0;
        }

        return val;
    }

    public long incReadIndexForPartition(int partitionId) {
        long newval = 1;

        Long val = this.readIndexByPartitionId.get(partitionId);
        if (val != null) {
            newval = val.longValue() + 1;
        }

        this.readIndexByPartitionId.put(partitionId, newval);
        return newval;
    }

    public void setReadIndexForPartition(int partitionId, long newValue) {
        this.readIndexByPartitionId.put(partitionId, newValue);
    }

    public int getNumberOfPartitions() {
        return numberOfPartitions;
    }

    public void setNumberOfPartitions(int numberOfPartitions) {
        this.numberOfPartitions = numberOfPartitions;
    }

    @Override
    public String toString() {
        return "WanLocation [siteIndex=" + siteIndex + ", name=" + name + ", host=" + host + ", port=" + port
                + ", isMe=" + isMe + ", numberOfPartitions=" + numberOfPartitions + "]";
    }
    

}
