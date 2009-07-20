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

import org.openspaces.core.GigaSpace;
import org.openspaces.remoting.SyncRemotingProxyConfigurer;

/**
 * A simple programmatic configurer creating a remote syncronous scripting proxy
 *
 * <p>Usage example:
 * <pre>
 * IJSpace space = new UrlSpaceConfigurer("jini://&#42;/&#42;/mySpace")
 *                        .space();
 * GigaSpace gigaSpace = new GigaSpaceConfigurer(space).gigaSpace();
 * ScriptingExecutor<Integer> executor = new SyncScriptingProxyConfigurer<Integer>(gigaSpace)
 *                                       .syncScriptingExecutor();
 * Integer result = executor.execute(new StaticScript()
 *                  .type("groovy")
 *                  .name("myScript")
 *                  .script("return 1")));
 * </pre>
 *
 * @author Uri Cohen
 * @deprecated Use {@link org.openspaces.remoting.scripting.ExecutorScriptingProxyConfigurer}.
 */
public class SyncScriptingProxyConfigurer<T> {

    private SyncRemotingProxyConfigurer<ScriptingExecutor> remotingConfigurer;

    public SyncScriptingProxyConfigurer(GigaSpace gigaSpace) {
        remotingConfigurer = new SyncRemotingProxyConfigurer<ScriptingExecutor>(gigaSpace, ScriptingExecutor.class)
                             .metaArgumentsHandler(new ScriptingMetaArgumentsHandler())
                             .remoteInvocationAspect(new LazyLoadingRemoteInvocationAspect())
                             .remoteRoutingHandler(new ScriptingRemoteRoutingHandler());
    }

    /**
     * Create a new sync <code>ScriptingExecutor</code> proxy
     */
    public ScriptingExecutor<T> syncScriptingExecutor() {
        return remotingConfigurer.syncProxy();
    }



}