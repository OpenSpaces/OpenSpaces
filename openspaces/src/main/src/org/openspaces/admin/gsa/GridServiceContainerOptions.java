package org.openspaces.admin.gsa;

import com.gigaspaces.grid.gsa.GSProcessOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kimchy
 */
public class GridServiceContainerOptions {

    private final List<String> vmInputArguments = new ArrayList<String>();

    private boolean overrideVmInputArguments = false;

    private boolean useScript = false;

    public GridServiceContainerOptions() {
    }

    public GridServiceContainerOptions useScript() {
        this.useScript = true;
        return this;
    }

    public GridServiceContainerOptions overrideVmInputArguments() {
        overrideVmInputArguments = true;
        return this;
    }

    public GridServiceContainerOptions vmInputArgument(String vmInputArgument) {
        vmInputArguments.add(vmInputArgument);
        return this;
    }

    public GSProcessOptions getOptions() {
        GSProcessOptions options = new GSProcessOptions("gsc");
        options.setUseScript(useScript);
        if (overrideVmInputArguments) {
            options.setVmInputArguments(vmInputArguments.toArray(new String[vmInputArguments.size()]));
        } else {
            options.setVmAppendableInputArguments(vmInputArguments.toArray(new String[vmInputArguments.size()]));
        }
        return options;
    }
}