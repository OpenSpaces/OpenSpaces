package org.openspaces.grid.esm;

import net.jini.id.Uuid;

/**
 * @author Niv Ingberg
 * @since 9.7.0
 */
public class SecuredESMProxy extends ESMProxy {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a ESM proxy, returning an instance that implements
     * RemoteMethodControl if the server does too.
     *
     * @param monitor The ESM server
     * @param id The Uuid of the ESM
     */
    public static SecuredESMProxy getInstance(ESM monitor, Uuid id) {
        return (new SecuredESMProxy(monitor, id));
    }

    /**
     * @param monitor The ESM server
     * @param id The Uuid of the ESM
     */
    private SecuredESMProxy(ESM monitor, Uuid id) {
        super(monitor, id);
    }

    @Override
    protected boolean isSecuredProxy() {
        return true;
    }
}
