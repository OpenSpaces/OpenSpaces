package org.openspaces.admin.internal.admin;

import org.openspaces.admin.LookupServices;

/**
 * @author kimchy
 */
public class DefaultMachine implements InternalMachine {

    private String uid;

    private String host;

    private final InternalLookupServices lookupServices = new DefaultLookupServices();

    public DefaultMachine(String uid, String host) {
        this.uid = uid;
        this.host = host;
    }

    public String getUID() {
        return this.uid;
    }

    public String getHost() {
        return this.host;
    }

    public LookupServices getLookupServices() {
        return lookupServices;
    }

    public void addLookupService(InternalLookupService lookupService) {
        lookupServices.addLookupService(lookupService);
    }

    public void removeLookupService(String uid) {
        lookupServices.removeLookupService(uid);
    }
}
