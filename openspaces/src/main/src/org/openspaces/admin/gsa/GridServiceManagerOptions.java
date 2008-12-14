package org.openspaces.admin.gsa;

import com.gigaspaces.grid.gsa.GSProcessOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kimchy
 */
public class GridServiceManagerOptions {

    private final List<String> vmInputArguments = new ArrayList<String>();

    private boolean overrideVmInputArguments = false;

    private boolean useScript = false;

    public GridServiceManagerOptions() {
    }

    public GridServiceManagerOptions useScript() {
        this.useScript = true;
        return this;
    }

    public GridServiceManagerOptions overrideVmInputArguments() {
        overrideVmInputArguments = true;
        return this;
    }

    public GridServiceManagerOptions vmInputArgument(String vmInputArgument) {
        vmInputArguments.add(vmInputArgument);
        return this;
    }

    public GSProcessOptions getOptions() {
        GSProcessOptions options = new GSProcessOptions("gsm");
        options.setUseScript(useScript);
        if (overrideVmInputArguments) {
            options.setVmInputArguments(vmInputArguments.toArray(new String[vmInputArguments.size()]));
        } else {
            options.setVmAppendableInputArguments(vmInputArguments.toArray(new String[vmInputArguments.size()]));
        }
        return options;
    }
}