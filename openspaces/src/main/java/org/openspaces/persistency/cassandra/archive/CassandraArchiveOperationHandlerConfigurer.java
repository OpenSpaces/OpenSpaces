package org.openspaces.persistency.cassandra.archive;

import org.openspaces.core.GigaSpace;

public class CassandraArchiveOperationHandlerConfigurer {

	CassandraArchiveOperationHandler handler;
	private boolean initialized;
	
	public CassandraArchiveOperationHandlerConfigurer() {
		handler = new CassandraArchiveOperationHandler();
	}
	
	/**
	 * @see CassandraArchiveOperationHandler#setKeyspace(String)
	 */
	public CassandraArchiveOperationHandlerConfigurer keyspace(String keyspace) {
		handler.setKeyspace(keyspace);
		return this;
	}

	/**
	 * @see CassandraArchiveOperationHandler#setHosts(String)
	 */
	public CassandraArchiveOperationHandlerConfigurer hosts(String hosts) {
		handler.setHosts(hosts);
		return this;
	}

	/**
	 * @see CassandraArchiveOperationHandler#setPort(Integer)
	 */
	public CassandraArchiveOperationHandlerConfigurer port(int port) {
		handler.setPort(port);
		return this;
	}

	public CassandraArchiveOperationHandlerConfigurer gigaSpace(GigaSpace gigaSpace) {
		handler.setGigaSpace(gigaSpace);
		return this;
	}
	
	public CassandraArchiveOperationHandler create() {
		if (!initialized) {
			handler.afterPropertiesSet();
			initialized = true;
		}
		return handler;
	}
}
