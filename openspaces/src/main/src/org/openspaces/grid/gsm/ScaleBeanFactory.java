package org.openspaces.grid.gsm;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.bean.BeanInitializationException;
import org.openspaces.admin.internal.pu.elastic.ProcessingUnitSchemaConfig;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.core.bean.Bean;
import org.openspaces.core.bean.BeanServer;
import org.openspaces.core.bean.DefaultBeanFactory;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.machines.EagerMachinesSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.machines.EagerMachinesSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.machines.ElasticMachineProvisioning;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.machines.NonBlockingElasticMachineProvisioning;
import org.openspaces.grid.gsm.machines.NonBlockingElasticMachineProvisioningAdapter;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpoint;

public class ScaleBeanFactory extends DefaultBeanFactory<Bean> {

    private static final Log logger = LogFactory.getLog(ScaleBeanFactory.class);
    
    private final RebalancingSlaEnforcementEndpoint rebalancingSlaEnforcementEndpoint;
    private final ContainersSlaEnforcementEndpoint containersSlaEnforcementEndpoint;
    private final ServiceLevelAgreementEnforcementEndpoint machinesSlaEnforcementEndpoint;
    private final ProcessingUnit pu;
    private final ProcessingUnitSchemaConfig schemaConfig;
    
    ScaleBeanFactory(
            ProcessingUnit pu,
            ProcessingUnitSchemaConfig schemaConfig,
            RebalancingSlaEnforcementEndpoint rebalancingSlaEnforcementEndpoint, 
            ContainersSlaEnforcementEndpoint containersSlaEnforcementEndpoint,
            ServiceLevelAgreementEnforcementEndpoint machinesSlaEnforcementEndpoint) {
        
        super(pu.getAdmin());
        this.schemaConfig = schemaConfig;
        this.rebalancingSlaEnforcementEndpoint = rebalancingSlaEnforcementEndpoint;
        this.containersSlaEnforcementEndpoint = containersSlaEnforcementEndpoint;
        this.machinesSlaEnforcementEndpoint = machinesSlaEnforcementEndpoint;
        this.pu = pu;
        
    }
    
    @Override
    protected Bean createInstance(String className, Map<String,String> properties, BeanServer<Bean> beanServer) throws BeanConfigurationException, BeanInitializationException{

        logger.debug("Creating instance of class " + className /* properties may contain passwords or other PII, do not log !!+ " with properties " + properties*/);
        
        Bean instance = super.createInstance(className,properties, beanServer);
        
        if (instance instanceof MachinesSlaEnforcementEndpointAware) {
            MachinesSlaEnforcementEndpointAware minstance = (MachinesSlaEnforcementEndpointAware)instance;
            minstance.setMachinesSlaEnforcementEndpoint((MachinesSlaEnforcementEndpoint)machinesSlaEnforcementEndpoint);
        }
        if (instance instanceof EagerMachinesSlaEnforcementEndpointAware) {
            EagerMachinesSlaEnforcementEndpointAware minstance = (EagerMachinesSlaEnforcementEndpointAware)instance;
            minstance.setEagerMachinesSlaEnforcementEndpoint((EagerMachinesSlaEnforcementEndpoint)machinesSlaEnforcementEndpoint);
        }
        
        if (instance instanceof ContainersSlaEnforcementEndpointAware) {
            ContainersSlaEnforcementEndpointAware cinstance = (ContainersSlaEnforcementEndpointAware)instance;
            cinstance.setContainersSlaEnforcementEndpoint(containersSlaEnforcementEndpoint);
        }
        
        if (instance instanceof RebalancingSlaEnforcementEndpointAware) {
            RebalancingSlaEnforcementEndpointAware rinstance = (RebalancingSlaEnforcementEndpointAware)instance;
            rinstance.setRebalancingSlaEnforcementEndpoint(rebalancingSlaEnforcementEndpoint);
        }
        
        if (instance instanceof ProcessingUnitAware) {
            ((ProcessingUnitAware)instance).setProcessingUnit(pu);
            ((ProcessingUnitAware)instance).setProcessingUnitSchema(schemaConfig);
        }
        
        if (instance instanceof ElasticMachineProvisioningAware) {
            List<Bean> injectedInstances = beanServer.getEnabledBeanAssignableTo(
                    new Class[]{
                            ElasticMachineProvisioning.class,
                            NonBlockingElasticMachineProvisioning.class});
           
            
            NonBlockingElasticMachineProvisioning machineProvisioning = null;
           
            for (Bean injectedInstance : injectedInstances) {
                if (injectedInstance instanceof ElasticMachineProvisioning) {
                    machineProvisioning = 
                        new NonBlockingElasticMachineProvisioningAdapter((ElasticMachineProvisioning)injectedInstance);
                    break;
                }
                else if (injectedInstance instanceof NonBlockingElasticMachineProvisioning){
                    machineProvisioning = (NonBlockingElasticMachineProvisioning) injectedInstance;
                    break;
                }
           }
            
           if (machineProvisioning != null) {
               ((ElasticMachineProvisioningAware)instance).setElasticMachineProvisioning(machineProvisioning);
           }
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
    
    ElasticConfigBean findElasticConfigBean(BeanServer<Bean> beanServer) {
        
        List<Bean> injectedInstances = beanServer.getEnabledBeanAssignableTo(
                new Class[]{ ElasticConfigBean.class });
        if (!injectedInstances.isEmpty()) {
            return (ElasticConfigBean) injectedInstances.get(0);
        }
        return null;
    }
}
