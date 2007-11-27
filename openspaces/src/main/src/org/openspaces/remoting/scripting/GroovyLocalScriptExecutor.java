package org.openspaces.remoting.scripting;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.util.Map;

/**
 * Groovy local script executor.
 *
 * @author kimchy
 */
public class GroovyLocalScriptExecutor implements LocalScriptExecutor<groovy.lang.Script> {

    private GroovyShell groovyShell;

    public GroovyLocalScriptExecutor() {
        groovyShell = new GroovyShell();
    }

    public groovy.lang.Script compile(Script script) throws ScriptCompilationException {
        try {
            return groovyShell.parse(script.getScriptAsString());
        } catch (Exception e) {
            throw new ScriptCompilationException("Failed to compile script [" + script.getName() + "]", e);
        }
    }

    public Object execute(Script script, groovy.lang.Script compiledScript, Map<String, Object> parameters) throws ScriptExecutionException {
        Binding binding = new Binding();
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                binding.setVariable(entry.getKey(), entry.getValue());
            }
        }
        compiledScript.setBinding(binding);
        try {
            return compiledScript.run();
        } catch (Exception e) {
            throw new ScriptExecutionException("Failed to execute script [" + script.getName() + "]", e);
        }
    }

    public void close(groovy.lang.Script compiledScript) {
    }

    public boolean isThreadSafe(groovy.lang.Script script) {
        return false;
    }
}
