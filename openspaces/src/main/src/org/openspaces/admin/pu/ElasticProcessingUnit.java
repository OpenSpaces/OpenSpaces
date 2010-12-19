package org.openspaces.admin.pu;

import java.util.Map;

public interface ElasticProcessingUnit  {

    Map<String,String> getEnvironmentVariables();
    void setEnvironmentVariables(Map<String,String> environmentVariables);
    boolean getOverrideVmInputArguments();
    void setOverrideVmInputArguments(boolean override);
    void setUseScript(boolean useScript);
    boolean getUseScript();
    String[] getVmInputArguments();
    void setVmInputArguments(String[] args);
}
