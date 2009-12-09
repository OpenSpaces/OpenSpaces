package org.openspaces.admin.dump;

import org.openspaces.admin.AdminException;

import java.util.Map;

/**
 * Elements in the admin API that support dump operations. Allows to generate dump of the given element(s).
 *
 * @author kimchy
 */
public interface DumpProvider {

    /**
     * Generates dump for all the provided dump processors. <b>Note, this will include a heap dump which
     * can consume time and potentially be of very large size</b>
     *
     * @param cause   The cause that this dump was generated
     * @param context Allows to provide specific parameters to specific processors
     */
    DumpResult generateDump(String cause, Map<String, Object> context) throws AdminException;

    /**
     * Generates dump for the provided processors. The current list of processors include:
     * <ul>
     * <li><b>summary</b>: General summary information of the process.
     * <li><b>network</b>: Information on the network layer of the process and the OS network stats.
     * <li><b>thread</b>: Thread dump of the process.
     * <li><b>heap</b>: Heap dump of the process. <b>Note, this is a heavy operation and can produce very large dump files</b>
     * <li><b>log</b>: Adds all the log files of the process to the dump file.
     * <li><b>processingUnits</b>: Dump of all the processing units (applicable only for GSCs) information.
     * </ul>
     *
     * @param cause     The cause this dump was generated.
     * @param context   Allows to provide specific parameters to specific processors.
     * @param processor The list of processors to be used.
     */
    DumpResult generateDump(String cause, Map<String, Object> context, String... processor) throws AdminException;
}