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

import com.gigaspaces.grid.gsa.GSProcessOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * {@link org.openspaces.admin.lus.LookupService} process options to be started by the
 * {@link org.openspaces.admin.gsa.GridServiceAgent}.
 *
 * @author kimchy
 * @see org.openspaces.admin.gsa.GridServiceAgent#startGridService(LookupServiceOptions)
 */
public class LookupServiceOptions {

    private final List<String> vmInputArguments = new ArrayList<String>();

    private boolean overrideVmInputArguments = false;

    private boolean useScript = false;

    private final Map<String, String> environmentVariables = new HashMap<String, String>();
    
    /**
     * Constructs a new lookup service options. By default will use JVM process execution.
     */
    public LookupServiceOptions() {
    }

    /**
     * Will cause the {@link org.openspaces.admin.lus.LookupService} to be started using a script
     * and not a pure Java process.
     */
    public LookupServiceOptions useScript() {
        this.useScript = true;
        return this;
    }

    /**
     * Will cause JVM options added using {@link #vmInputArgument(String)} to override all the vm arguments
     * that the JVM will start by default with.
     */
    public LookupServiceOptions overrideVmInputArguments() {
        overrideVmInputArguments = true;
        return this;
    }

    /**
     * Will add a JVM level argument when the process is executed using pure JVM. For example, the memory
     * can be controlled using <code>-Xmx512m</code>.
     */
    public LookupServiceOptions vmInputArgument(String vmInputArgument) {
        vmInputArguments.add(vmInputArgument);
        return this;
    }

    /**
     * Sets an environment variable that will be passed to forked process.
     */
    public LookupServiceOptions environmentVariable(String name, String value) {
        environmentVariables.put(name, value);
        return this;
    }
    
    /**
     * Returns the agent process options that represents what was set on this LUS options.
     */
    public GSProcessOptions getOptions() {
        GSProcessOptions options = new GSProcessOptions("lus");
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