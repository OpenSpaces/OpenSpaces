/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.remoting.scripting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.ClassUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * A Default "server" side script executor. Can execute {@link org.openspaces.remoting.scripting.Script}s
 * based on a set of registered {@link org.openspaces.remoting.scripting.LocalScriptExecutor}s.
 *
 * <p>Will automatically register <code>groovy</code> ({@link org.openspaces.remoting.scripting.GroovyLocalScriptExecutor})
 * if it exists within the classpath. Will also automatically register <code>ruby</code>
 * ({@link org.openspaces.remoting.scripting.JRubyLocalScriptExecutor}) if the jruby jars exists within the classpath.
 *
 * <p>If working under Java 6, or adding JSR 223 jars into the classpath, will use its scripting support as a fallback
 * if no local script executors are found for a given type. The JSR allows for a unified API on top of different scripting
 * libraries with pluggable types.
 *
 * <p>The executor will automatically add the Spring application context as a parameter to the script under the
 * name <code>applicationContext</code> allowing to get any beans defined on the "server side". Since <code>GigaSpace</code>
 * instances are often used within the script, it will automatically add all the different <code>GigaSpace</code>
 * instances defined within the application context under their respective bean names.
 *
 * @author kimchy
 */
public class DefaultScriptingExecutor implements ScriptingExecutor, ApplicationContextAware, InitializingBean, ApplicationListener {

    private static final Log logger = LogFactory.getLog(DefaultScriptingExecutor.class);

    public static final String APPLICATION_CONTEXT_KEY = "applicationContext";

    public static final String GROOVY_LOCAL_EXECUTOR_TYPE = "groovy";

    public static final String JRUBY_LOCAL_EXECUTOR_TYPE = "ruby";

    private ApplicationContext applicationContext;

    private Map<String, Object> parameters;

    private Map<String, LocalScriptExecutor> executors = new HashMap<String, LocalScriptExecutor>();

    private LocalScriptExecutor jsr223Executor;

    private Map<String, GigaSpace> gigaSpacesBeans = new HashMap<String, GigaSpace>(); 

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public void setExecutors(Map<String, LocalScriptExecutor> executors) {
        this.executors = executors;
    }

    public void afterPropertiesSet() throws Exception {
        if (!executors.containsKey(GROOVY_LOCAL_EXECUTOR_TYPE)) {
            // try and create the groovy executor if it exists in the class path
            try {
                LocalScriptExecutor groovyExecutor = (LocalScriptExecutor) ClassUtils.forName("org.openspaces.remoting.scripting.GroovyLocalScriptExecutor").newInstance();
                executors.put(GROOVY_LOCAL_EXECUTOR_TYPE, groovyExecutor);
                if (logger.isDebugEnabled()) {
                    logger.debug("Groovy detected in the classpath, adding it as a local executor under the [" + GROOVY_LOCAL_EXECUTOR_TYPE + "] type");
                }
            } catch (Exception e) {
                // no grovy in the classpath, don't register it
            }
        }
        if (!executors.containsKey(JRUBY_LOCAL_EXECUTOR_TYPE)) {
            // try and create the groovy executor if it exists in the class path
            try {
                LocalScriptExecutor jrubyExecutor = (LocalScriptExecutor) ClassUtils.forName("org.openspaces.remoting.scripting.JRubyLocalScriptExecutor").newInstance();
                executors.put(JRUBY_LOCAL_EXECUTOR_TYPE, jrubyExecutor);
                if (logger.isDebugEnabled()) {
                    logger.debug("JRuby detected in the classpath, adding it as a local executor under the [" + JRUBY_LOCAL_EXECUTOR_TYPE + "] type");
                }
            } catch (Exception e) {
                // no jruby in the classpath, don't register it
            }
        }
        try {
            jsr223Executor = (LocalScriptExecutor) ClassUtils.forName("org.openspaces.remoting.scripting.Jsr223LocalScriptExecutor").newInstance();
            if (logger.isDebugEnabled()) {
                logger.debug("Java 6 (JSR 223) detected in the classpath, adding it as a default executor");
            }
        } catch (Exception e) {
            // not working with Java 6, or JSR 223 jars are not included
        }

        // validate that we have at least one local script executor
        if (executors.size() == 0 && jsr223Executor == null) {
            throw new IllegalArgumentException("No local script executors are configured or automatically detected");
        }
    }

    /**
     * On applicaiton context refresh event get all the GigaSpace beans and put them in a map
     * that will later be appeneded to any script parameters.
     */
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ContextRefreshedEvent) {
            Map gigaBeans = applicationContext.getBeansOfType(GigaSpace.class);
            if (gigaBeans != null) {
                for (Iterator it = gigaBeans.entrySet().iterator(); it.hasNext();) {
                    Map.Entry entry = (Map.Entry) it.next();
                    gigaSpacesBeans.put((String) entry.getKey(), (GigaSpace) entry.getValue());
                }
            }
        }
    }

    public Object execute(Script script) throws ScriptingException {
        if (script.getType() == null) {
            throw new IllegalArgumentException("Script must contain type");
        }
        LocalScriptExecutor localScriptExecutor = executors.get(script.getType());
        // if we fail to find one specific for that type, fail (if possible) to JSR 223
        if (localScriptExecutor == null) {
            localScriptExecutor = jsr223Executor;
            if (localScriptExecutor == null) {
                throw new ScriptingException("Failed to find executor for type [" + script.getType() + "]");
            }
        }
        Object compiledScript = localScriptExecutor.compile(script);
        Map<String, Object> scriptParams = new HashMap<String, Object>();
        if (parameters != null) {
            scriptParams.putAll(parameters);
        }
        scriptParams.putAll(gigaSpacesBeans);
        scriptParams.put(APPLICATION_CONTEXT_KEY, applicationContext);
        if (script.getParameters() != null) {
            scriptParams.putAll(script.getParameters());
        }
        try {
            return localScriptExecutor.execute(script, compiledScript, scriptParams);
        } finally {
            localScriptExecutor.close(compiledScript);
        }
    }

    public Future asyncExecute(Script script) throws ScriptingException {
        return null;
    }
}
