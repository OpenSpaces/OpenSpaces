/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.grid.gsm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jini.rio.monitor.event.EventsStore;
import org.openspaces.admin.bean.BeanConfigException;
import org.openspaces.admin.bean.BeanConfigPropertiesManager;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.internal.pu.elastic.ElasticMachineIsolationConfig;
import org.openspaces.admin.internal.pu.elastic.MachineProvisioningBeanPropertiesManager;
import org.openspaces.admin.internal.pu.elastic.ProcessingUnitSchemaConfig;
import org.openspaces.admin.internal.pu.elastic.ScaleStrategyBeanPropertiesManager;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.core.bean.Bean;
import org.openspaces.core.bean.BeanServer;
import org.openspaces.core.bean.DefaultBeanServer;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaEnforcement;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcement;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcement;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.machines.plugins.ElasticMachineProvisioning;
import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioning;
import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioningAdapterFactory;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcement;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpoint;
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

    private final BeanServer<Bean> beanServer;
    private final ProcessingUnit pu ;
    private final RebalancingSlaEnforcement rebalancingSlaEnforcement;
    private final MachinesSlaEnforcement machinesSlaEnforcement;
    private final ContainersSlaEnforcement containersSlaEnforcement;
    private final AutoScalingSlaEnforcement autoScalingSlaEnforcement;
    
    public ScaleBeanServer(
            ProcessingUnit pu,
            ProcessingUnitSchemaConfig schemaConfig,
            RebalancingSlaEnforcement rebalancingSlaEnforcement, 
            ContainersSlaEnforcement containersSlaEnforcement,
            MachinesSlaEnforcement machinesSlaEnforcement,
            AutoScalingSlaEnforcement autoScalingSlaEnforcement,
            NonBlockingElasticMachineProvisioningAdapterFactory nonBlockingAdapterFactory,
            ElasticMachineIsolationConfig isolationConfig,
            EventsStore eventStore) {

        if (pu == null) {
            throw new IllegalArgumentException("pu cannot be null");
        }
        
        if (rebalancingSlaEnforcement == null) {
            throw new IllegalArgumentException("rebalancingSlaEnforcement cannot be null");
        }
        
        if (containersSlaEnforcement == null) {
            throw new IllegalArgumentException("containersSlaEnforcement cannot be null");
        }
        
        if (machinesSlaEnforcement == null) {
            throw new IllegalArgumentException("machinesSlaEnforcement cannot be null");
        }
        
        if (autoScalingSlaEnforcement == null) {
            throw new IllegalArgumentException("autoScalingEnforcement cannot be null");
        }
        
        if (eventStore == null) {
            throw new IllegalArgumentException("eventStorage cannot be null");
        }
        
        this.pu = pu;
        this.rebalancingSlaEnforcement = rebalancingSlaEnforcement;
        this.containersSlaEnforcement = containersSlaEnforcement;
        this.machinesSlaEnforcement = machinesSlaEnforcement;
        this.autoScalingSlaEnforcement = autoScalingSlaEnforcement;
        
        MachinesSlaEnforcementEndpoint machinesSlaEnforcementEndpoint = null;
        ContainersSlaEnforcementEndpoint containersSlaEnforcementEndpoint = null;
        RebalancingSlaEnforcementEndpoint rebalancingSlaEnforcementEndpoint = null;
        AutoScalingSlaEnforcementEndpoint autoScalingSlaEnforcementEndpoint = null;
        try {
            containersSlaEnforcementEndpoint = containersSlaEnforcement.createEndpoint(pu);
            machinesSlaEnforcementEndpoint = machinesSlaEnforcement.createEndpoint(pu);
            rebalancingSlaEnforcementEndpoint = rebalancingSlaEnforcement.createEndpoint(pu);
            autoScalingSlaEnforcementEndpoint = autoScalingSlaEnforcement.createEndpoint(pu); 
        }
        finally {
            if (containersSlaEnforcementEndpoint == null ||
                machinesSlaEnforcementEndpoint == null ||
                rebalancingSlaEnforcementEndpoint == null || 
                autoScalingSlaEnforcementEndpoint == null) {
                
                // make sure there are no leftovers in case of an exception
                this.rebalancingSlaEnforcement.destroyEndpoint(pu);
                this.containersSlaEnforcement.destroyEndpoint(pu);
                this.machinesSlaEnforcement.destroyEndpoint(pu);
                this.autoScalingSlaEnforcement.destroyEndpoint(pu);
            }
        }
        
        this.beanServer = new DefaultBeanServer<Bean>(
                
                new ScaleBeanFactory(
                        pu,
                        schemaConfig,
                        rebalancingSlaEnforcementEndpoint, 
                        containersSlaEnforcementEndpoint,
                        machinesSlaEnforcementEndpoint,
                        autoScalingSlaEnforcementEndpoint,
                        nonBlockingAdapterFactory,
                        isolationConfig,
                        eventStore));
        
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
        
        ScaleStrategyBeanPropertiesManager scaleStrategyBeanPropertiesManager = 
            new ScaleStrategyBeanPropertiesManager(elasticProperties);
        String enabledBeanClassName = getEnabledBeanClassName(scaleStrategyBeanPropertiesManager);
        
        //replace scale strategy bean if necessary
        if (enabledBeanClassName == null) {
            throw new BeanConfigurationException("scale strategy is not defined");
        }
    
        Map<String, String> beanProperties = new HashMap<String,String>(scaleStrategyBeanPropertiesManager.getBeanConfig(enabledBeanClassName));
        beanServer.replaceBeanAssignableTo(
                new Class[]{ScaleStrategyBean.class}, 
                enabledBeanClassName,
                beanProperties); 
    
    }
        
    private void setElasticMachineProvisioning(Map<String, String> elasticProperties) {
        
        final MachineProvisioningBeanPropertiesManager propertiesManager = 
            new MachineProvisioningBeanPropertiesManager(elasticProperties);
        
        String enabledBeanClassName = getEnabledBeanClassName(propertiesManager);

        if (enabledBeanClassName == null) {
            throw new BeanConfigurationException("machine provisioning is not defined");
        }       
                   
        Map<String, String> beanProperties = new HashMap<String,String>(propertiesManager.getBeanConfig(enabledBeanClassName));
        beanServer.replaceBeanAssignableTo(
                new Class[]{ElasticMachineProvisioning.class,
                            NonBlockingElasticMachineProvisioning.class}, 
                enabledBeanClassName,
                beanProperties);
    
    }
    
    public ScaleStrategyBean getEnabledBean() {
        ScaleStrategyBean bean =null;
        List<Bean> enabledBeanAssignableTo = beanServer.getEnabledBeansAssignableTo(new Class[] {ScaleStrategyBean.class});
        if (!enabledBeanAssignableTo.isEmpty()) {
            bean = (ScaleStrategyBean)enabledBeanAssignableTo.get(0);
        }
        return bean;
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
