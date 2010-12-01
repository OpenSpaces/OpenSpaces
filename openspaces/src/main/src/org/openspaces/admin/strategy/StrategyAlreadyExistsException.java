package org.openspaces.admin.strategy;

public class StrategyAlreadyExistsException extends StrategyException {

	private static final long serialVersionUID = 1L;

	public StrategyAlreadyExistsException(String message) {
		super(message);
	}

	public StrategyAlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}
}
