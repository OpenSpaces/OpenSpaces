package org.openspaces.admin.gsa;

import com.gigaspaces.grid.gsa.GSProcessOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kimchy
 */
public class LookupServiceOptions {

    private final List<String> inputParameters = new ArrayList<String>();

    private boolean overrideInputParameters = false;

    private boolean useScript = false;

    public LookupServiceOptions() {
    }

    public LookupServiceOptions addInputParameter(String inputParameter) {
        inputParameters.add(inputParameter);
        return this;
    }

    public LookupServiceOptions overrideInputParameters() {
        this.overrideInputParameters = true;
        return this;
    }

    public LookupServiceOptions useScript() {
        this.useScript = true;
        return this;
    }

    public GSProcessOptions getOptions() {
        GSProcessOptions options = new GSProcessOptions(GSProcessOptions.Type.LUS);
        options.setUseScript(useScript);
        if (overrideInputParameters) {
            options.setInputArguments(inputParameters.toArray(new String[inputParameters.size()]));
        } else {
            options.setAppendableInputArguments(inputParameters.toArray(new String[inputParameters.size()]));
        }
        return options;
    }
}