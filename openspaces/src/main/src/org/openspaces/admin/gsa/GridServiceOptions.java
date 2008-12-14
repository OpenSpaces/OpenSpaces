package org.openspaces.admin.gsa;

import com.gigaspaces.grid.gsa.GSProcessOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kimchy
 */
public class GridServiceOptions {

    private final String type;

    private boolean useScript = false;

    private final List<String> vmInputArguments = new ArrayList<String>();

    private boolean overrideVmInputArguments = false;

    private final List<String> arguments = new ArrayList<String>();

    private boolean overrideArguments;

    public GridServiceOptions(String type) {
        this.type = type;
    }

    public GridServiceOptions useScript() {
        this.useScript = true;
        return this;
    }

    public GridServiceOptions overrideVmInputArguments() {
        overrideVmInputArguments = true;
        return this;
    }

    public GridServiceOptions vmInputArgument(String vmInputArgument) {
        vmInputArguments.add(vmInputArgument);
        return this;
    }

    public GridServiceOptions overrideArguments() {
        overrideArguments = true;
        return this;
    }

    public GridServiceOptions argument(String argument) {
        arguments.add(argument);
        return this;
    }

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
                options.setSciptArguments(arguments.toArray(new String[arguments.size()]));
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
        return options;
    }
}
