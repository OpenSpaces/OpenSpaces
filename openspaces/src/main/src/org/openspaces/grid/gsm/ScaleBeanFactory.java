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

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jini.rio.monitor.event.EventsStore;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.bean.BeanInitializationException;
import org.openspaces.admin.internal.pu.elastic.ElasticMachineIsolationConfig;
import org.openspaces.admin.internal.pu.elastic.ProcessingUnitSchemaConfig;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.core.bean.Bean;
import org.openspaces.core.bean.BeanServer;
import org.openspaces.core.bean.DefaultBeanFactory;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.machines.isolation.ElasticProcessingUnitMachineIsolation;
import org.openspaces.grid.gsm.machines.isolation.ElasticProcessingUnitMachineIsolationAware;
import org.openspaces.grid.gsm.machines.isolation.ElasticProcessingUnitMachineIsolationFactory;
import org.openspaces.grid.gsm.machines.plugins.ElasticMachineProvisioning;
import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioning;
import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioningAdapterFactory;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.strategy.ElasticScaleStrategyEventStorageAware;

public class ScaleBeanFactory extends DefaultBeanFactory<Bean> {

    private static final Log logger = LogFactory.getLog(ScaleBeanFactory.class);
    
    private final RebalancingSlaEnforcementEndpoint rebalancingSlaEnforcementEndpoint;
    private final ContainersSlaEnforcementEndpoint containersSlaEnforcementEndpoint;
    private final MachinesSlaEnforcementEndpoint machinesSlaEnforcementEndpoint;
    private final AutoScalingSlaEnforcementEndpoint autoScalingSlaEnforcementEndpoint;
    private final ProcessingUnit pu;
    private final ProcessingUnitSchemaConfig schemaConfig;
    private final NonBlockingElasticMachineProvisioningAdapterFactory nonBlockingAdapterFactory;
    private final ElasticMachineIsolationConfig isolationConfig;
    private final EventsStore eventStore;

    
    ScaleBeanFactory(
            ProcessingUnit pu,
            ProcessingUnitSchemaConfig schemaConfig,
            RebalancingSlaEnforcementEndpoint rebalancingSlaEnforcementEndpoint, 
            ContainersSlaEnforcementEndpoint containersSlaEnforcementEndpoint,
            MachinesSlaEnforcementEndpoint machinesSlaEnforcementEndpoint,
            AutoScalingSlaEnforcementEndpoint autoScalingSlaEnforcementEndpoint,
            NonBlockingElasticMachineProvisioningAdapterFactory nonBlockingAdapterFactory,
            ElasticMachineIsolationConfig isolationConfig,
            EventsStore eventStore) {
        
        super(pu.getAdmin());
        this.schemaConfig = schemaConfig;
        this.rebalancingSlaEnforcementEndpoint = rebalancingSlaEnforcementEndpoint;
        this.containersSlaEnforcementEndpoint = containersSlaEnforcementEndpoint;
        this.machinesSlaEnforcementEndpoint = machinesSlaEnforcementEndpoint;
        this.autoScalingSlaEnforcementEndpoint = autoScalingSlaEnforcementEndpoint;
        this.nonBlockingAdapterFactory = nonBlockingAdapterFactory;
        this.pu = pu;
        this.isolationConfig = isolationConfig;
        this.eventStore = eventStore;
        
    }
    
    @Override
    protected Bean createInstance(String className, Map<String,String> properties, BeanServer<Bean> beanServer) throws BeanConfigurationException, BeanInitializationException{

        logger.debug("Creating instance of class " + className /* properties may contain passwords or other PII, do not log !!+ " with properties " + properties*/);
        
        Bean instance = super.createInstance(className,properties, beanServer);
                
        if (instance instanceof MachinesSlaEnforcementEndpointAware) {
            MachinesSlaEnforcementEndpointAware minstance = (MachinesSlaEnforcementEndpointAware)instance;
            minstance.setMachinesSlaEnforcementEndpoint(machinesSlaEnforcementEndpoint);
        }
        
        if (instance instanceof ContainersSlaEnforcementEndpointAware) {
            ContainersSlaEnforcementEndpointAware cinstance = (ContainersSlaEnforcementEndpointAware)instance;
            cinstance.setContainersSlaEnforcementEndpoint(containersSlaEnforcementEndpoint);
        }
        
        if (instance instanceof RebalancingSlaEnforcementEndpointAware) {
            RebalancingSlaEnforcementEndpointAware rinstance = (RebalancingSlaEnforcementEndpointAware)instance;
            rinstance.setRebalancingSlaEnforcementEndpoint(rebalancingSlaEnforcementEndpoint);
        }
        
        if (instance instanceof AutoScalingSlaEnforcementEndpointAware) {
            AutoScalingSlaEnforcementEndpointAware ainstance = (AutoScalingSlaEnforcementEndpointAware) instance;
            ainstance.setAutoScalingSlaEnforcementEndpoint(autoScalingSlaEnforcementEndpoint);
        }
        
        if (instance instanceof ProcessingUnitAware) {
            ((ProcessingUnitAware)instance).setProcessingUnit(pu);
            ((ProcessingUnitAware)instance).setProcessingUnitSchema(schemaConfig);
        }
                
        if (instance instanceof ElasticMachineProvisioningAware) {
            NonBlockingElasticMachineProvisioning machineProvisioning = getNonBlockingElasticMachineProvisioningBean(beanServer);
            ((ElasticMachineProvisioningAware)instance).setElasticMachineProvisioning(machineProvisioning);
        }
        
        if (instance instanceof ElasticScaleStrategyEventStorageAware) {
            ((ElasticScaleStrategyEventStorageAware)instance).setElasticScaleStrategyEventStorage(eventStore);
        }
        
        if (instance instanceof ElasticProcessingUnitMachineIsolationAware) {
            ElasticProcessingUnitMachineIsolation isolation = new ElasticProcessingUnitMachineIsolationFactory().create(pu.getName(), isolationConfig);
            ((ElasticProcessingUnitMachineIsolationAware)instance).setElasticProcessingUnitMachineIsolation(isolation); 
        }
        
        ElasticConfigBean elasticConfigBean = findElasticConfigBean(beanServer);
        if (elasticConfigBean != null) {
            if (instance instanceof GridServiceContainerConfigAware) {
                ((GridServiceContainerConfigAware)instance)
                    .setGridServiceContainerConfig((elasticConfigBean.getGridServiceContainerConfig()));
            }
        }
        
        return instance;
    }

    private NonBlockingElasticMachineProvisioning getNonBlockingElasticMachineProvisioningBean(BeanServer<Bean> beanServer) {
        List<Bean> injectedInstances = beanServer.getEnabledBeansAssignableTo(
                new Class[]{
                        ElasticMachineProvisioning.class,
                        NonBlockingElasticMachineProvisioning.class});
         
        NonBlockingElasticMachineProvisioning machineProvisioning = null;
        
        for (Bean injectedInstance : injectedInstances) {
            if (injectedInstance instanceof ElasticMachineProvisioning) {
                machineProvisioning = nonBlockingAdapterFactory.create(pu, (ElasticMachineProvisioning)injectedInstance);
                break;
            }
            else if (injectedInstance instanceof NonBlockingElasticMachineProvisioning){
                machineProvisioning = (NonBlockingElasticMachineProvisioning) injectedInstance;
                break;
            }
         }

        if (machineProvisioning == null) {
            throw new IllegalStateException("machineProvisioning bean cannot be found");
        }

        return machineProvisioning;
    }

    ElasticConfigBean findElasticConfigBean(BeanServer<Bean> beanServer) {
        
        List<Bean> injectedInstances = beanServer.getEnabledBeansAssignableTo(
                new Class[]{ ElasticConfigBean.class });
        if (!injectedInstances.isEmpty()) {
            return (ElasticConfigBean) injectedInstances.get(0);
        }
        return null;
    }
}
