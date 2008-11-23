package org.openspaces.admin;

/**
 * @author kimchy
 */
public interface LookupServices {

    LookupService[] getLookupServices();

    LookupService getLookupServiceByUID(String id);
}
