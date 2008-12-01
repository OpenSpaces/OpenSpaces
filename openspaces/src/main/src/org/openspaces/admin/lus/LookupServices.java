package org.openspaces.admin.lus;

import java.util.Map;

/**
 * @author kimchy
 */
public interface LookupServices extends Iterable<LookupService> {

    LookupService[] getLookupServices();

    LookupService getLookupServiceByUID(String id);

    Map<String, LookupService> getUids();

    int size();

    boolean isEmpty();

    void addEventListener(LookupServiceEventListener eventListener);

    void removeEventListener(LookupServiceEventListener eventListener);
}
