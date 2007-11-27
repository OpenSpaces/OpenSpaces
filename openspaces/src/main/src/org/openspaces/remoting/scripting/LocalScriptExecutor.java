package org.openspaces.remoting.scripting;

import java.util.Map;

/**
 * An SPI implementation for script executor ("on the server side") that can handle a specific script
 * type (or labguage).
 *
 * @author kimchy
 */
public interface LocalScriptExecutor<T> {

    /**
     * Compiles the given sctipt.
     */
    T compile(Script script) throws ScriptCompilationException;

    /**
     * Executes the given compiled script.
     */
    Object execute(Script script, T compiledScript, Map<String, Object> parameters) throws ScriptExecutionException;

    /**
     * Closes the compiled script.
     */
    void close(T compiledScript);

    /**
     * Returns <code>true</code> if the same compiled script can be used by different threads (note, parameres
     * or bindings usually make a scripting library not thread safe).
     */
    boolean isThreadSafe(T compiledScript);
}
