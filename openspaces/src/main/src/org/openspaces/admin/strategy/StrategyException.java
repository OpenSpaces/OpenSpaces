package org.openspaces.admin.strategy;

import org.openspaces.admin.AdminException;

public class StrategyException extends AdminException {

	private static final long serialVersionUID = 1L;

	public StrategyException(String message) {
		super(message);
	}
	
	public StrategyException(String message, Throwable cause) {
		super(message, cause);
	}

}
