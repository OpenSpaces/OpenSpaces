package org.openspaces.admin.lus;

/**
 * @author kimchy
 */
public interface LookupServices extends Iterable<LookupService> {

    LookupService[] getLookupServices();

    LookupService getLookupServiceByUID(String id);

    int size();

    boolean isEmpty();

    void addEventListener(LookupServiceEventListener eventListener);

    void removeEventListener(LookupServiceEventListener eventListener);
}
