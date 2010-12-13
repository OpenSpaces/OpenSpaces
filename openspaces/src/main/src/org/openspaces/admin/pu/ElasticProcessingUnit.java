package org.openspaces.admin.pu;

import java.util.Map;

import org.openspaces.admin.bean.BeanConfigPropertiesManager;
import org.openspaces.admin.pu.elastic.config.ScaleBeanConfig;

public interface ElasticProcessingUnit  {

    BeanConfigPropertiesManager getBeanPropertiesManager();
    void scale(ScaleBeanConfig scaleBeanConfiguration);
    Map<String,String> getEnvironmentVariables();
    void setEnvironmentVariables(Map<String,String> environmentVariables);
    boolean getOverrideVmInputArguments();
    void setOverrideVmInputArguments(boolean override);
    void setUseScript(boolean useScript);
    boolean getUseScript();
    String[] getVmInputArguments();
    void setVmInputArguments(String[] args);
}
