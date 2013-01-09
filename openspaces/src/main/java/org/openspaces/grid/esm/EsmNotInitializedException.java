package org.openspaces.grid.esm;

public class EsmNotInitializedException extends IllegalStateException {

    private static final long serialVersionUID = 1L;
    
    public EsmNotInitializedException() {
        super("ESM not initialized yet");
    }

}
