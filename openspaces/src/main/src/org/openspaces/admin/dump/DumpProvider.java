package org.openspaces.admin.dump;

import org.openspaces.admin.AdminException;

import java.util.Map;

/**
 * @author kimchy
 */
public interface DumpProvider {

    DumpResult generateDump(String cause, Map<String, Object> context) throws AdminException;

    DumpResult generateDump(String cause, Map<String, Object> context, String... processor) throws AdminException;
}