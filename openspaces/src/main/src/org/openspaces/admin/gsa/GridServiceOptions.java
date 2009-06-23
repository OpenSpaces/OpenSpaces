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
 * A generic process options.
 *
 * @author kimchy
 */
public class GridServiceOptions {

    private final String type;

    private boolean useScript = false;

    private final List<String> vmInputArguments = new ArrayList<String>();

    private boolean overrideVmInputArguments = false;

    private final List<String> arguments = new ArrayList<String>();

    private boolean overrideArguments;

    private final Map<String, String> environmentVariables = new HashMap<String, String>();

    /**
     * Constructs a new grid service options with the given process type. By default, will use JVM to start it.
     */
    public GridServiceOptions(String type) {
        this.type = type;
    }

    /**
     * Will use a scipt to start the process, and not a pure JVM process.
     */
    public GridServiceOptions useScript() {
        this.useScript = true;
        return this;
    }

    /**
     * Will cause JVM options added using {@link #vmInputArgument(String)} to override all the vm arguments
     * that the JVM will start by default with.
     */
    public GridServiceOptions overrideVmInputArguments() {
        overrideVmInputArguments = true;
        return this;
    }

    /**
     * Will add a JVM level argument when the process is executed using pure JVM. For example, the memory
     * can be controlled using <code>-Xmx512m</code>.
     */
    public GridServiceOptions vmInputArgument(String vmInputArgument) {
        vmInputArguments.add(vmInputArgument);
        return this;
    }

    /**
     * Will cause the process arguments added using {@link #argument(String)} to override any arguments defined
     * in the process descriptor.
     */
    public GridServiceOptions overrideArguments() {
        overrideArguments = true;
        return this;
    }

    /**
     * Will add a process level argument.
     */
    public GridServiceOptions argument(String argument) {
        arguments.add(argument);
        return this;
    }

    /**
     * Sets an environment variable that will be passed to forked process.
     */
    public GridServiceOptions environmentVariable(String name, String value) {
        environmentVariables.put(name, value);
        return this;
    }

    /**
     * Returns the agent process options that represents what was set on this generic service options.
     */
    public GSProcessOptions getOptions() {
        GSProcessOptions options = new GSProcessOptions(type);
        options.setUseScript(useScript);
        if (overrideVmInputArguments) {
            options.setVmInputArguments(vmInputArguments.toArray(new String[vmInputArguments.size()]));
        } else {
            options.setVmAppendableInputArguments(vmInputArguments.toArray(new String[vmInputArguments.size()]));
        }
        if (options.isUseScript()) {
            if (overrideArguments) {
                options.setScriptArguments(arguments.toArray(new String[arguments.size()]));
            } else {
                options.setScriptAppendableArguments(arguments.toArray(new String[arguments.size()]));
            }
        } else {
            if (overrideArguments) {
                options.setVmArguments(arguments.toArray(new String[arguments.size()]));
            } else {
                options.setVmAppendableArguments(arguments.toArray(new String[arguments.size()]));
            }
        }
        options.setEnvironmentVariables(environmentVariables);
        return options;
    }
}
