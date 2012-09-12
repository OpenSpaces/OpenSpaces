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

package org.openspaces.admin.gsa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gigaspaces.grid.gsa.GSProcessOptions;

/**
 * {@link org.openspaces.admin.esm.ElasticServiceManager} process options to be started by the
 * {@link org.openspaces.admin.gsa.GridServiceAgent}.
 *
 * @author Moran Avigdor
 * @see org.openspaces.admin.gsa.GridServiceAgent#startGridService(ElasticServiceManagerOptions)
 */
public class ElasticServiceManagerOptions {

    private final List<String> vmInputArguments = new ArrayList<String>();

    private boolean overrideVmInputArguments = false;

    private boolean useScript = false;

    private final Map<String, String> environmentVariables = new HashMap<String, String>();
    
    /**
     * Constructs a new elastic service manager options. By default will use JVM process execution.
     */
    public ElasticServiceManagerOptions() {
    }

    /**
     * Will cause the {@link org.openspaces.admin.esm.ElasticServiceManager} to be started using a script
     * and not a pure Java process.
     */
    public ElasticServiceManagerOptions useScript() {
        this.useScript = true;
        return this;
    }

    /**
     * Will cause JVM options added using {@link #vmInputArgument(String)} to override all the vm arguments
     * that the JVM will start by default with.
     */
    public ElasticServiceManagerOptions overrideVmInputArguments() {
        overrideVmInputArguments = true;
        return this;
    }

    /**
     * Will add a JVM level argument when the process is executed using pure JVM. For example, the memory
     * can be controlled using <code>-Xmx512m</code>.
     */
    public ElasticServiceManagerOptions vmInputArgument(String vmInputArgument) {
        vmInputArguments.add(vmInputArgument);
        return this;
    }

    /**
     * Sets an environment variable that will be passed to forked process.
     */
    public ElasticServiceManagerOptions environmentVariable(String name, String value) {
        environmentVariables.put(name, value);
        return this;
    }
    
    /**
     * Returns the agent process options that represents what was set on this ESM options.
     */
    public GSProcessOptions getOptions() {
        GSProcessOptions options = new GSProcessOptions("esm");
        options.setUseScript(useScript);
        if (overrideVmInputArguments) {
            options.setVmInputArguments(vmInputArguments.toArray(new String[vmInputArguments.size()]));
        } else {
            options.setVmAppendableInputArguments(vmInputArguments.toArray(new String[vmInputArguments.size()]));
        }
        options.setEnvironmentVariables(environmentVariables);
        return options;
    }
}