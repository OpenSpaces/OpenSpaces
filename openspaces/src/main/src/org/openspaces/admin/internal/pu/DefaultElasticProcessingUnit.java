package org.openspaces.admin.internal.pu;

import java.util.Map;

import org.openspaces.admin.bean.BeanPropertiesManager;
import org.openspaces.admin.pu.ElasticProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.elastic.config.ScaleBeanConfig;


public class DefaultElasticProcessingUnit implements ElasticProcessingUnit {

    private ProcessingUnit pu;

    public DefaultElasticProcessingUnit(ProcessingUnit pu) {
        this.pu = pu;
    }

    public BeanPropertiesManager getBeanPropertiesManager() {
        //TODO: Auto-generated
        return null;
    }
    
    public void scale(ScaleBeanConfig scaleBeanConfiguration) {
      //TODO: Auto-generated
    }
    
    /*
     * // flatten all scale strategy bean properties to dynamic processing unit properties.
        StringProperties dynamicProperties = new StringProperties();
        String enabledStrategyKeyPrefix = scaleBeanClassName+".";
        dynamicProperties.putMap(enabledStrategyKeyPrefix, this.scaleBeanProperties);
        dynamicProperties.put(ENABLED_SCALE_BEAN_CLASS_NAME_DYNAMIC_PROPERTY, scaleBeanClassName);

        // flatten all grid service container options to dynamic processing unit properties.
        dynamicProperties.putMap(ENVIRONMENT_VARIABLES_DYNAMIC_PROPERTY_PREFIX, environmentVariables);
        dynamicProperties.putBoolean(OVERRIDE_JVM_INPUT_ARGUMENTS_DYNAMIC_PROPERTY, this.overrideVmInputArguments);
        dynamicProperties.putBoolean(USE_SCRIPT_DYNAMIC_PROPERTY, this.useScript);
        dynamicProperties.putArgumentsArray(JVM_INPUT_ARGUMENTS_DYNAMIC_PROPERTY_PREFIX, getVmInputArguments());
        
        // flatten all machine pool bean properties
        dynamicProperties.put(MACHINE_POOL_BEAN_CLASS_NAME_DYNAMIC_PROPERTY,machinePoolBeanClassName);
        String machinePoolKeyPrefix = machinePoolBeanClassName+".";
        dynamicProperties.putMap(machinePoolKeyPrefix, this.machinePoolBeanProperties);

     */
    
    public Map<String,String> getEnvironmentVariables() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public void setEnvironmentVariables(Map<String,String> environmentVariables) {
        // TODO Auto-generated method stub
                
    }
    
    public boolean getOverrideVmInputArguments() {
     // TODO Auto-generated method stub
        return false;
    }
    
    public void setOverrideVmInputArguments(boolean override) {
     // TODO Auto-generated method stub
    }
    
    public void setUseScript(boolean useScript) {
     // TODO Auto-generated method stub
    }
    
    public boolean getUseScript() {
     // TODO Auto-generated method stub
        return false;
    }
    
    public String[] getVmInputArguments() {
     // TODO Auto-generated method stub
        return null;
    }
    
    public void setVmInputArguments(String[] args) {
     // TODO Auto-generated method stub   
    }

}
