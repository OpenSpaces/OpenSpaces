package org.openspaces.grid.gsm.rebalancing;

@SuppressWarnings("serial")
class WrongContainerRelocationException extends ProcessingUnitInstanceDeploymentException {

    private static final long serialVersionUID = 1239832832671562407L;

    public WrongContainerRelocationException(String message) {
        super(message);
    }

}
