package org.openspaces.admin.dump;

/**
 * @author kimchy (shay.banon)
 */
public interface DumpGeneratedListener {

    void onGenerated(DumpProvider provider, DumpResult dumpResult);
}
