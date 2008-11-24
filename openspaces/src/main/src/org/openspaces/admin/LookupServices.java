package org.openspaces.admin;

/**
 * @author kimchy
 */
public interface LookupServices extends Iterable<LookupService> {

    LookupService[] getLookupServices();

    LookupService getLookupServiceByUID(String id);

    int size();
}
