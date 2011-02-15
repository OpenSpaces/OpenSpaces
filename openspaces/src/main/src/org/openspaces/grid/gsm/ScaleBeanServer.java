package org.openspaces.grid.gsm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openspaces.admin.bean.BeanConfigException;
import org.openspaces.admin.bean.BeanConfigPropertiesManager;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.internal.pu.elastic.MachineProvisioningBeanPropertiesManager;
import org.openspaces.admin.internal.pu.elastic.ProcessingUnitSchemaConfig;
import org.openspaces.admin.internal.pu.elastic.ScaleStrategyBeanPropertiesManager;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.core.bean.Bean;
import org.openspaces.core.bean.BeanServer;
import org.openspaces.core.bean.DefaultBeanServer;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcement;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.machines.DefaultMachineProvisioning;
import org.openspaces.grid.gsm.machines.ElasticMachineProvisioning;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcement;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.machines.NonBlockingElasticMachineProvisioning;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcement;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.strategy.ManualCapacityScaleStrategyBean;
import org.openspaces.grid.gsm.strategy.ScaleStrategyBean;
import org.openspaces.grid.gsm.strategy.UndeployScaleStrategyBean;
/**
 * Creates the Scalability Strategy bean servers, based on the specified elasticProperties.
 * When the elasticProperties are modified the bean is restarted with the new properties.
 * 
 * @author itaif
 *
 */
public class ScaleBeanServer {

    private static final HashMap<String, String> DEFAULT_SCALE_STRATEGY_BEAN_PROPERTIES = new HashMap<String,String>();
    private static final String DEFAULT_SCALE_STRATEGY_BEAN_CLASSNAME = ManualCapacityScaleStrategyBean.class.getName();
    
    private final BeanServer<Bean> beanServer;
    private final ProcessingUnit pu ;
    private final RebalancingSlaEnforcement rebalancingSlaEnforcement;
    private final MachinesSlaEnforcement machinesSlaEnforcement;
    private final ContainersSlaEnforcement containersSlaEnforcement;
    
    public ScaleBeanServer(
            ProcessingUnit pu,
            RebalancingSlaEnforcement rebalancingSlaEnforcement, 
            ContainersSlaEnforcement containersSlaEnforcement,
            MachinesSlaEnforcement machinesSlaEnforcement,
            Map<String,String> elasticProperties) {

        this.pu = pu;
        this.rebalancingSlaEnforcement = rebalancingSlaEnforcement;
        this.containersSlaEnforcement = containersSlaEnforcement;
        this.machinesSlaEnforcement = machinesSlaEnforcement;
        
        ContainersSlaEnforcementEndpoint containersSlaEnforcementEndpoint = null;
        MachinesSlaEnforcementEndpoint machinesSlaEnforcementEndpoint = null;
        RebalancingSlaEnforcementEndpoint rebalancingSlaEnforcementEndpoint = null;
        try {
            containersSlaEnforcementEndpoint = containersSlaEnforcement.createEndpoint(pu);
            machinesSlaEnforcementEndpoint = machinesSlaEnforcement.createEndpoint(pu);
            rebalancingSlaEnforcementEndpoint = rebalancingSlaEnforcement.createEndpoint(pu);
        }
        finally {
            if (containersSlaEnforcementEndpoint == null ||
                machinesSlaEnforcementEndpoint == null ||
                rebalancingSlaEnforcementEndpoint == null) {
                
                // make sure there are no leftovers in case of an exception
                this.rebalancingSlaEnforcement.destroyEndpoint(pu);
                this.containersSlaEnforcement.destroyEndpoint(pu);
                this.machinesSlaEnforcement.destroyEndpoint(pu);
            }
        }
        
        this.beanServer = new DefaultBeanServer<Bean>(
                
                new ScaleBeanFactory(
                        pu,
                        new ProcessingUnitSchemaConfig(elasticProperties),
                        rebalancingSlaEnforcementEndpoint, 
                        containersSlaEnforcementEndpoint,
                        machinesSlaEnforcementEndpoint));
        
        // order of beans is important due to implicit dependency inject order
        setElasticConfig(elasticProperties);
        
        MachineProvisioningBeanPropertiesManager machineProvisioningPropertiesManager = 
                new MachineProvisioningBeanPropertiesManager(elasticProperties);
        String enabledMachineProvisioningClassName = getEnabledBeanClassName(machineProvisioningPropertiesManager);
        if (enabledMachineProvisioningClassName != null) {
            beanServer.setBeanConfig(
                    enabledMachineProvisioningClassName, 
                    machineProvisioningPropertiesManager.getBeanConfig(enabledMachineProvisioningClassName));
            beanServer.enableBean(enabledMachineProvisioningClassName);
        }
        

        String scaleStrategyClassName = DEFAULT_SCALE_STRATEGY_BEAN_CLASSNAME;
        Map<String,String> scaleStrategyProperties = DEFAULT_SCALE_STRATEGY_BEAN_PROPERTIES;
        ScaleStrategyBeanPropertiesManager scaleStrategyPropertiesManager = 
            new ScaleStrategyBeanPropertiesManager(elasticProperties);
        String enabledScaleStrategyClassName = getEnabledBeanClassName(scaleStrategyPropertiesManager);
        
        if (enabledScaleStrategyClassName != null) {
            scaleStrategyClassName = enabledScaleStrategyClassName;
            scaleStrategyProperties = scaleStrategyPropertiesManager.getBeanConfig(scaleStrategyClassName);
        }
        
        beanServer.setBeanConfig(scaleStrategyClassName, scaleStrategyProperties);
        beanServer.enableBean(scaleStrategyClassName);
    }

    /**
     * Changes scale strategy to undeployed processing unit strategy (remove Containers/Machines)
     */
    public void undeploy() {
        List<String> enabledBeanClassNames;
        try {
            enabledBeanClassNames = beanServer.getEnabledBeansClassNamesAssignableTo(new Class[]{ScaleStrategyBean.class});
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(),e);
        }
        
        if (enabledBeanClassNames.size() > 0) {
        // we need the previously enabled strategy properties in case it had machine provisioning configured.
        Map<String,String> properties = beanServer.getBeanConfig(enabledBeanClassNames.get(0));
        // enable the undeploy strategy
        beanServer.replaceBeanAssignableTo(
                new Class[]{ScaleStrategyBean.class}, 
                UndeployScaleStrategyBean.class.getName(),
                properties);
        }
    }
    
    public void destroy() {
        this.beanServer.destroy();
        this.rebalancingSlaEnforcement.destroyEndpoint(pu);
        this.containersSlaEnforcement.destroyEndpoint(pu);
        this.machinesSlaEnforcement.destroyEndpoint(pu);
    }

    public void setElasticProperties(Map<String,String> elasticProperties) throws BeanConfigException {
        // order of beans is important due to implicit dependency inject order
        // see ScaleBeanFactory
        setElasticConfig(elasticProperties);
        setElasticMachineProvisioning(elasticProperties);
        setElasticScaleStrategy(elasticProperties);
    }

    private void setElasticConfig(Map<String, String> elasticProperties) {
        beanServer.replaceBeanAssignableTo(
                new Class[]{ElasticConfigBean.class}, 
                ElasticConfigBean.class.getName(),
                elasticProperties); 
    }

    private void setElasticScaleStrategy(Map<String, String> elasticProperties) {
        
        ScaleStrategyBeanPropertiesManager scaleStrategyBeanPropertiesManager = new ScaleStrategyBeanPropertiesManager(elasticProperties);
        String enabledBeanClassName = getEnabledBeanClassName(scaleStrategyBeanPropertiesManager);
        
        //replace scale strategy bean if necessary
        if (enabledBeanClassName == null) {
            beanServer.disableAllBeansAssignableTo(ScaleStrategyBean.class);
        }
        else {

            Map<String, String> beanProperties = new HashMap<String,String>(scaleStrategyBeanPropertiesManager.getBeanConfig(enabledBeanClassName));
            beanServer.replaceBeanAssignableTo(
                    new Class[]{ScaleStrategyBean.class}, 
                    enabledBeanClassName,
                    beanProperties); 
        }
    }
    
    private void setElasticMachineProvisioning(Map<String, String> elasticProperties) {
        
        final MachineProvisioningBeanPropertiesManager propertiesManager = 
            new MachineProvisioningBeanPropertiesManager(elasticProperties);
        
        String enabledBeanClassName = getEnabledBeanClassName(propertiesManager);

        if (enabledBeanClassName != null && enabledBeanClassName.equals(DefaultMachineProvisioning.class.getName())) {
            // this is a special machine provisioning that is not a bean
            enabledBeanClassName = null;
        }
        
        List<Bean> existingEnabledBeans = beanServer.getEnabledBeanAssignableTo(
                new Class[]{
                        ElasticMachineProvisioning.class,
                        NonBlockingElasticMachineProvisioning.class});

        if (enabledBeanClassName == null && !existingEnabledBeans.isEmpty()) {
            throw new BeanConfigurationException("Cannot disable " + existingEnabledBeans.get(0).getClass() + " at runtime.");
        }
        
        if (enabledBeanClassName != null && existingEnabledBeans.isEmpty()) {
            throw new BeanConfigurationException("Cannot enable " + enabledBeanClassName + " at runtime.");
        }
        
        // replace machine provisioning bean if possible    
        if (enabledBeanClassName != null && !existingEnabledBeans.isEmpty()) {
                       
            Map<String, String> beanProperties = propertiesManager.getBeanConfig(enabledBeanClassName);
            beanServer.replaceBeanAssignableTo(
                    new Class[]{ElasticMachineProvisioning.class,
                                NonBlockingElasticMachineProvisioning.class}, 
                    enabledBeanClassName,
                    beanProperties);
        }
    }
    
    private String getEnabledBeanClassName(BeanConfigPropertiesManager propertiesManager) {
        final String[] enabledBeansClassNames = propertiesManager.getEnabledBeansClassNames();
        
        if (enabledBeansClassNames.length > 1) {
            throw new BeanConfigurationException("At most one of the following beans can be enabled: " + Arrays.toString(enabledBeansClassNames));
        }
        String enabledBeanClassName = null; 
        if (enabledBeansClassNames.length == 1) {
            enabledBeanClassName = enabledBeansClassNames[0];
        }
        return enabledBeanClassName;
    }

}
