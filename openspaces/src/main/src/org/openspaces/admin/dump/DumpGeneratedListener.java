package org.openspaces.admin.dump;

/**
 * When generating dump across several components (dump providers) allows to be notified when a dump was
 * generated.
 *
 * @author kimchy (shay.banon)
 */
public interface DumpGeneratedListener {

    /**
     * A listener to be notified when a dump was generated.
     *
     * @param provider     The provider the dump was generated for
     * @param dumpResult   The dump result
     * @param currentCount The current count of dumps generated
     * @param totalCount   The total expected count of dumps generated
     */
    void onGenerated(DumpProvider provider, DumpResult dumpResult, int currentCount, int totalCount);
}
