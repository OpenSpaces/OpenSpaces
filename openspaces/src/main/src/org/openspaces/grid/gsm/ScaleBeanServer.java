package org.openspaces.grid.gsm;

import java.util.Map;
import java.util.logging.Logger;

import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.esm.ProcessingUnitElasticConfig;
import org.openspaces.core.bean.Bean;
import org.openspaces.grid.gsm.elastic.ScaleStrategyBean;

public class ScaleBeanServer {

    private Logger logger = Logger.getLogger(this.getClass().getPackage().getName());

    private ProcessingUnitElasticConfig properties;
    
    //private RebalancingSlaEnforcement rebalancingSlaEnforcement;
    //private ContainersSlaEnforcement containersSlaEnforcement;
    //private MachinesSlaEnforcement machinesSlaEnforcement;
    //private ScaleStrategyBean scaleStrategyBean;
    private Bean elasticMachineAllocator;
    private ScaleStrategyBean elasticScaleStrategy;
    
    private final Object lock = new Object();

    private final Admin admin;

    public ScaleBeanServer(Admin admin) {
        
        this.admin = admin;
    }
    
    public void destroy() {
        //this.rebalancingSlaEnforcement.destroy();
        //this.containersSlaEnforcement.destroy();
    }

    public Map<String, String> getProperties() {
        return properties.getProperties();
    }



    public void setProperties(Map<String, String> rawproperties, boolean rollback) {
        
        ProcessingUnitElasticConfig newProperties = new ProcessingUnitElasticConfig(rawproperties);
        
        if (!rollback &&
            properties.equals(newProperties)) {
            
            //ignore new properties, same as old ones
            return;
        }
        
        //TODO: create strategy instance if needed 
        //if (!properties.getScaleStrategy().getBeanClassName().equals(newProperties.getScaleStrategy().getBeanClassName() ||
                
        
    }
}
/*
        try {
            initMachineSlaEnforcement(new ProcessingUnitDynamicProperties(rawproperties));
            this.properties = new ProcessingUnitDynamicProperties(rawproperties);
        }
        catch (BeanConfigException e) {
            if (!rollback) {
                // need to log since roll-back exception might hide it.
                logger.log(Level.WARNING, "Failed to apply new configuration",e);
                try {
                //roll-back
                setProperties(this.properties.getProperties(),true);
                logger.log(Level.INFO, "Configuration rollback completed succesfully.");
                }
                catch (BeanConfigException e2) {
                    logger.log(Level.SEVERE, "Failed to rollback configuration back to prev state.",e2);
                    throw e2;
                }
            }
            throw e;
        }
        
            
        
    }
    
    private String getEnabledBeanClassNameOrNull(BeanConfigPropertiesManager propertiesManager) {
        String beanClassName = null;
        String[] enabledMachinesAllocators = propertiesManager.getEnabledBeansClassNames();
        if (enabledMachinesAllocators.length > 1) {
            throw new BeanConfigurationException("Only one ElasticMachineAllocator can be enabled");
        }
        
        boolean sameElasticMachineAllocator = false;
        if (enabledMachinesAllocators.length == 1) {
            beanClassName = enabledMachinesAllocators[0];
        }
        
        return beanClassName;
    }
    
    private void initMachineSlaEnforcement(ProcessingUnitElasticConfig properties) {
        BeanConfigPropertiesManager propertiesManager = properties.getElasticMachineAllocatorPropertiesManager();
        String beanClassName = getEnabledBeanClassNameOrNull(propertiesManager);
                
        if (beanClassName == null ||
            this.elasticMachineAllocator == null ||
            !this.elasticMachineAllocator.getClass().equals(beanClassName) ||
            !this.properties.getElasticMachineAllocatorPropertiesManager().getConfig(beanClassName).equals(propertiesManager.getConfig(beanClassName))) {
            
            // not the same bean or not the same bean configuration
         
            if(this.elasticMachineAllocator != null) {
                
                this.elasticMachineAllocator.destroy();
                this.elasticMachineAllocator = null;
            }
            
            if (this.machinesSlaEnforcement != null) {
                
                this.machinesSlaEnforcement.destroy();
                this.machinesSlaEnforcement = null;
            }
        
            if (beanClassName != null) {
                
                DefaultBeanFactory<Bean> beanFactory = new DefaultBeanFactory<Bean>(admin);
                Bean instance = beanFactory.create(beanClassName, propertiesManager.getConfig(beanClassName));
                this.elasticMachineAllocator = instance;
                this.machinesSlaEnforcement = new MachinesSlaEnforcement(admin, elasticMachineAllocator);
            }
        }
    }
    
    private void initContainersSlaEnforcement(ProcessingUnitDynamicProperties properties) {
        
        GridServiceContainerConfig gridServiceContainerConfig = properties.getGridServiceContainerConfig();
        if (this.containersSlaEnforcement == null ||
            !this.containersSlaEnforcement.getConfig().equals(properties.getGridServiceContainerConfig()) {
            
            
        }
        
    }
    */
