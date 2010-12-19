package org.openspaces.grid.gsm;

import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.bean.BeanConfigNotFoundException;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.internal.pu.elastic.config.ScaleStrategyConfigUtils;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.zone.Zone;
import org.openspaces.core.bean.Bean;
import org.openspaces.core.bean.BeanFactory;
import org.openspaces.core.bean.DefaultBeanFactory;
import org.openspaces.core.util.StringProperties;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcement;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcement;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcement;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpointAware;

public class ScaleBeanFactory implements BeanFactory<Bean> {

    DefaultBeanFactory<Bean> factory;
    private final RebalancingSlaEnforcement rebalancingSlaEnforcement;
    private final ContainersSlaEnforcement containersSlaEnforcement;
    private final Admin admin;
    
    ScaleBeanFactory(
            Admin admin,
            RebalancingSlaEnforcement rebalancingSlaEnforcement, 
            ContainersSlaEnforcement containersSlaEnforcement,
            MachinesSlaEnforcement machinesSlaEnforcement) {
        this.rebalancingSlaEnforcement = rebalancingSlaEnforcement;
        this.containersSlaEnforcement = containersSlaEnforcement;
        this.factory = new DefaultBeanFactory<Bean>(admin);
        this.admin = admin;
    }
    
    public Bean create(String className, Map<String,String> properties) throws BeanConfigurationException, BeanConfigNotFoundException {

        String puName = ScaleStrategyConfigUtils.getProcessingUnitName(new StringProperties(properties));
        ProcessingUnit pu = admin.getProcessingUnits().getProcessingUnit(puName);
        if (pu == null) {
            throw new BeanConfigurationException("Cannot find processing unit by the name '" + puName + "'");
        }
            
        Bean instance = factory.create(className,properties);
        
        if (pu.getRequiredZones().length != 1 || pu.getRequiredZones()[0].length() == 0) {
            throw new BeanConfigurationException("Processing Unit " + pu.getName() + " must have a required container zone.");
        }
        
        
        
        Zone zone = admin.getZones().getByName(pu.getRequiredZones()[0]);
        
        if (instance instanceof ContainersSlaEnforcementEndpointAware) {
            ContainersSlaEnforcementEndpointAware cinstance = (ContainersSlaEnforcementEndpointAware)instance;
            cinstance.setContainersSlaEnforcementEndpoint(containersSlaEnforcement.createEndpoint(zone.getName()));
        }
        
        if (instance instanceof RebalancingSlaEnforcementEndpointAware) {
            RebalancingSlaEnforcementEndpointAware rinstance = (RebalancingSlaEnforcementEndpointAware)instance;
            rinstance.setRebalancingSlaEnforcementEndpoint(rebalancingSlaEnforcement.createEndpoint(pu));
        }
        
        if (instance instanceof ProcessingUnitAware) {
            ((ProcessingUnitAware)instance).setProcessingUnit(pu);
        }
        return instance;
    }
}
