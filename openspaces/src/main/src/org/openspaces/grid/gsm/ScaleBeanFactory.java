package org.openspaces.grid.gsm;

import java.util.List;
import java.util.Map;

import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.bean.BeanInitializationException;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.core.bean.Bean;
import org.openspaces.core.bean.BeanServer;
import org.openspaces.core.bean.DefaultBeanFactory;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.machines.ElasticMachineProvisioning;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.machines.NonBlockingElasticMachineProvisioning;
import org.openspaces.grid.gsm.machines.NonBlockingElasticMachineProvisioningAdapter;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpointAware;

public class ScaleBeanFactory extends DefaultBeanFactory<Bean> {

    private final RebalancingSlaEnforcementEndpoint rebalancingSlaEnforcementEndpoint;
    private final ContainersSlaEnforcementEndpoint containersSlaEnforcementEndpoint;
    private final MachinesSlaEnforcementEndpoint machinesSlaEnforcementEndpoint;
    private final ProcessingUnit pu;
    
    ScaleBeanFactory(
            ProcessingUnit pu,
            RebalancingSlaEnforcementEndpoint rebalancingSlaEnforcementEndpoint, 
            ContainersSlaEnforcementEndpoint containersSlaEnforcementEndpoint,
            MachinesSlaEnforcementEndpoint machinesSlaEnforcementEndpoint) {
        
        super(pu.getAdmin());
        this.rebalancingSlaEnforcementEndpoint = rebalancingSlaEnforcementEndpoint;
        this.containersSlaEnforcementEndpoint = containersSlaEnforcementEndpoint;
        this.machinesSlaEnforcementEndpoint = machinesSlaEnforcementEndpoint;
        this.pu = pu;
        
    }
    
    @Override
    protected Bean createInstance(String className, Map<String,String> properties, BeanServer<Bean> beanServer) throws BeanConfigurationException, BeanInitializationException{

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
        
        if (instance instanceof ProcessingUnitAware) {
            ((ProcessingUnitAware)instance).setProcessingUnit(pu);
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
        
        if (instance instanceof GridServiceContainerConfigAware) {
            List<Bean> injectedInstances = beanServer.getEnabledBeanAssignableTo(
                    new Class[]{ GridServiceContainerConfigBean.class });
            for (Bean injectedInstance : injectedInstances) {
                ((GridServiceContainerConfigAware)instance)
                    .setGridServiceContainerConfig(((GridServiceContainerConfigBean)injectedInstance).getGridServiceContainerConfig());
                break;
            }
        }
        return instance;
    }
}
