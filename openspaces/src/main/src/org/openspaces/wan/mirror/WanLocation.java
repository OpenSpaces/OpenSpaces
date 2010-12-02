package org.openspaces.wan.mirror;

import java.util.Set;
import java.util.StringTokenizer;

public class WanLocation {

	private static final int DEFAULT_LOCATION_PORT = 10000;

	private String host;
	private int port = DEFAULT_LOCATION_PORT;
	private String name;
	private boolean isMe = false;

	private int siteIndex;
	private long readIndex = 0;
	
	private WanQueryExecutor queryExecutor = null;

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

		final String parsedName = tokenizer.nextToken();
		if (name != null) {
			this.name = parsedName;
		} else {
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

	public long getReadIndex() {
		return readIndex;
	}

	public int getSiteIndex() {
		return siteIndex;
	}

	public void incReadIndex() {
		++this.readIndex;
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

	public void setReadIndex(final long readIndex) {
		this.readIndex = readIndex;
	}

	public void setSiteIndex(final int siteIndex) {
		this.siteIndex = siteIndex;
	}

	public String toLocatorString() {
		return this.host + ":" + this.port;
	}

	public WanQueryExecutor getQueryExecutor() {
		return this.queryExecutor;
	}

	public void setQueryExecutor(WanQueryExecutor queryExecutor) {
		this.queryExecutor = queryExecutor;
	}

}
