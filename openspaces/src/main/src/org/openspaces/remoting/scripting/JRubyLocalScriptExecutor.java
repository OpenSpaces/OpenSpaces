package org.openspaces.remoting.scripting;

import org.jruby.Ruby;
import org.jruby.ast.Node;
import org.jruby.internal.runtime.GlobalVariables;
import org.jruby.javasupport.Java;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.Block;
import org.jruby.runtime.GlobalVariable;
import org.jruby.runtime.IAccessor;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * JRuby local script executor.
 *
 * @author kimchy
 */
public class JRubyLocalScriptExecutor implements LocalScriptExecutor<JRubyLocalScriptExecutor.JRubyCompiledScript> {

    public JRubyCompiledScript compile(Script script) throws ScriptCompilationException {
        Ruby runtime = JavaEmbedUtils.initialize(new ArrayList());
        IRubyObject rubyScript = JavaEmbedUtils.javaToRuby(runtime, script.getScriptAsString());
        Node node = runtime.parse(rubyScript.asSymbol(), "<unknown>", null, 0);
        return new JRubyCompiledScript(runtime, node);
    }

    public Object execute(Script script, JRubyCompiledScript compiledScript, Map<String, Object> parameters) throws ScriptExecutionException {
        GlobalVariables globablVariables = compiledScript.runtime.getGlobalVariables();
        if (parameters != null) {
            compiledScript.runtime.setGlobalVariables(new ParametersGlobalVariables(compiledScript.runtime, parameters));
        }
        try {
            return rubyToJava(compiledScript.runtime.eval(compiledScript.node));
        } finally {
            compiledScript.runtime.setGlobalVariables(globablVariables);
        }
    }

    public void close(JRubyCompiledScript compiledScript) {
        JavaEmbedUtils.terminate(compiledScript.runtime);
    }

    public boolean isThreadSafe(JRubyCompiledScript compiledScript) {
        return false;
    }

    private Object rubyToJava(IRubyObject value) {
        return rubyToJava(value, Object.class);
    }

    private Object rubyToJava(IRubyObject value, Class type) {
        return JavaUtil.convertArgument(Java.ruby_to_java(value, value, Block.NULL_BLOCK), type);
    }

    public static class JRubyCompiledScript {
        public Ruby runtime;
        public Node node;

        private JRubyCompiledScript(Ruby runtime, Node node) {
            this.runtime = runtime;
            this.node = node;
        }
    }

    private class ParametersGlobalVariables extends GlobalVariables {

        private Ruby runtime;

        private Map<String, Object> parameters;

        GlobalVariables parent;

        public ParametersGlobalVariables(Ruby runtime, Map<String, Object> parameters) {
            super(runtime);
            this.runtime = runtime;
            this.parameters = parameters;
            this.parent = runtime.getGlobalVariables();
        }

        public void define(String name, IAccessor accessor) {
            parameters.put(name, new GlobalVariable(runtime, name, accessor.getValue()));
        }

        public void defineReadonly(String name, IAccessor accessor) {
            parameters.put(name, new GlobalVariable(runtime, name, accessor.getValue()));
        }

        public boolean isDefined(String name) {
            String modifiedName = name.substring(1);
            boolean defined = parameters.containsKey(modifiedName);
            return defined || parent.isDefined(name);
        }

        public void alias(String name, String oldName) {
            if (runtime.getSafeLevel() >= 4) {
                throw runtime.newSecurityError("Insecure: can't alias global variable");
            }

            IRubyObject value = get(oldName);
            parameters.put(name, rubyToJava(value));
        }

        public IRubyObject get(String name) {
            String modifiedName = name.substring(1);

            Object obj = parameters.get(modifiedName);
            if (obj instanceof IAccessor) {
                return ((IAccessor) obj).getValue();
            } else {
                return JavaEmbedUtils.javaToRuby(runtime, obj);
            }
        }

        public IRubyObject set(String name, IRubyObject value) {
            if (runtime.getSafeLevel() >= 4) {
                throw runtime.newSecurityError("Insecure: can't change global variable value");
            }
            // skip '$' and try
            String modifiedName = name.substring(1);
            IRubyObject oldValue = get(name);
            Object obj = parameters.get(modifiedName);
            if (obj instanceof IAccessor) {
                ((IAccessor) obj).setValue(value);
            } else {
                parameters.put(modifiedName, rubyToJava(value));
            }
            return oldValue;
        }

        public Iterator getNames() {
            List<String> list = new ArrayList<String>();
            for (String key : parameters.keySet()) {
                list.add(key);
            }
            for (Iterator names = parent.getNames(); names.hasNext();) {
                list.add((String) names.next());
            }
            return Collections.unmodifiableList(list).iterator();
        }
    }
}
