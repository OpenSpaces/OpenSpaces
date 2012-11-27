package org.openspaces.persistency.cassandra.archive;

import org.openspaces.persistency.cassandra.error.SpaceCassandraException;

public class SpaceCassandraArchiveOperationHandlerException extends SpaceCassandraException {

	private static final long serialVersionUID = 1L;
	
	public SpaceCassandraArchiveOperationHandlerException(
			String message) {
		super(message);
	}
	
	public SpaceCassandraArchiveOperationHandlerException(
			String message, Throwable e) {
		super(message, e);
	}


}
