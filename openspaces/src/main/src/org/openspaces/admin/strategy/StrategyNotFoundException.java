package org.openspaces.admin.strategy;

public class StrategyNotFoundException extends StrategyException {

	private static final long serialVersionUID = 1L;

	public StrategyNotFoundException(String message) {
		super(message);
	}
	
	public StrategyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
