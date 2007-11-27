package org.openspaces.remoting.scripting;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;

/**
 * Java 6 (JSR 223) sciprt executor.
 *
 * @author kimchy
 */
public class Jsr223LocalScriptExecutor implements LocalScriptExecutor<Object> {

    private ScriptEngineManager scriptEngineManager;

    public Jsr223LocalScriptExecutor() {
        scriptEngineManager = new ScriptEngineManager();
    }

    public Object compile(Script script) throws ScriptCompilationException {
        ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(script.getType());
        if (scriptEngine instanceof Compilable) {
            try {
                return ((Compilable) scriptEngine).compile(script.getScriptAsString());
            } catch (ScriptException e) {
                throw new ScriptCompilationException("Failed to compile script [" + script.getName() + "]", e);
            }
        }
        return scriptEngine;
    }

    public Object execute(Script script, Object compiledScript, Map<String, Object> parameters) throws ScriptExecutionException {
        if (compiledScript instanceof ScriptEngine) {
            ScriptEngine scriptEngine = (ScriptEngine) compiledScript;
            Bindings bindings = scriptEngine.createBindings();
            if (parameters != null) {
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    bindings.put(entry.getKey(), entry.getValue());
                }
            }
            try {
                return scriptEngine.eval(script.getScriptAsString(), bindings);
            } catch (ScriptException e) {
                throw new ScriptExecutionException("Failed to execute script [" + script.getName() + "]", e);
            }
        }
        CompiledScript cmpScript = (CompiledScript) compiledScript;
        Bindings bindings = cmpScript.getEngine().createBindings();
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                bindings.put(entry.getKey(), entry.getValue());
            }
        }
        try {
            return cmpScript.eval(bindings);
        } catch (ScriptException e) {
            throw new ScriptExecutionException("Failed to execute script [" + script.getName() + "]", e);
        }
    }

    public void close(Object compiledScript) {
    }

    public boolean isThreadSafe(Object compiledScript) {
        return compiledScript instanceof ScriptEngine;
    }
}
