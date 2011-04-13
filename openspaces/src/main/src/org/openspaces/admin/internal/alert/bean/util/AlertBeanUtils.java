package org.openspaces.admin.internal.alert.bean.util;

import java.util.List;
import java.util.UUID;

import org.openspaces.admin.GridComponent;
import org.openspaces.admin.internal.alert.bean.AlertBean;
import org.openspaces.admin.internal.support.AbstractAgentGridComponent;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.os.OperatingSystemDetails;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.vm.VirtualMachine;

public class AlertBeanUtils {
    
    /**
     * average in specified period.
     * @param period sliding window of timeline samples.
     * @param timeline timeline of samples.
     * @return -1 if not enough samples; average of samples within period.
     */
    public static double getAverage(int period, List<Double> timeline) {
        if (period > timeline.size()) return -1;

        double average = 0.0;
        for (int i = 0; i < period && i < timeline.size(); i++) {
            double value = timeline.get(i);
            average += value;
        }
        average /= period;

        return average;
    }

    /**
     * Generate a unique bean UUID for a specific class by it's name.
     * 
     * @param clazz
     *            the class of the alert bean to generate a UUID for.
     * @return a UUID consisting of the name as hexadecimal digits concatenated with a random UUID.
     */
    public static String generateBeanUUID(Class<? extends AlertBean> clazz) {
        return Integer.toHexString(clazz.getSimpleName().hashCode())+"-"+UUID.randomUUID();
    }
    
    /**
     * Returns the short name of a grid component running inside a JVM.
     * Inspects the JVM in the following order - GSM, GSC, GSA, LUS.
     * 
     * @param virtualMachine
     * @return initials of the grid component. empty string if no component found.
     */
    public static String getGridComponentShortName(VirtualMachine virtualMachine) {
        if (virtualMachine.getElasticServiceManager() != null) {
            return "ESM";
        } else if (virtualMachine.getGridServiceManager() != null) {
            return "GSM";
        } else if (virtualMachine.getGridServiceContainer() != null) {
            return "GSC";
        } else if (virtualMachine.getGridServiceAgent() != null) {
            return "GSA";
        } else if (virtualMachine.getLookupService() != null) {
            return "LUS";
        } else return "";
    }

    /**
     * Returns the full name of a grid component running inside a JVM.
     * Inspects the JVM in the following order - GSM, GSC, GSA, LUS.
     * 
     * @param virtualMachine
     * @return full name of a grid component. "n/a" if no component found.
     */
    public static String getGridComponentFullName(VirtualMachine virtualMachine) {
        if (virtualMachine.getElasticServiceManager() != null) {
            return "Elastic Service Manager";
        } else if (virtualMachine.getGridServiceManager() != null) {
            return "Grid Service Manager";
        } else if (virtualMachine.getGridServiceContainer() != null) {
            return "Grid Service Container";
        } else if (virtualMachine.getGridServiceAgent() != null) {
            return "Grid Service Agent";
        } else if (virtualMachine.getLookupService() != null) {
            return "Lookup Service";
        } else return "n/a";
    }
    
    /**
     * @return Returns the description of this virtual machine: "[short name] [pid] on [host name]/[host address]"
     */
    public static String getGridComponentDescription(VirtualMachine virtualMachine) {
        return getGridComponentShortName(virtualMachine)+ getGridComponentAgentId(virtualMachine) + virtualMachine.getDetails().getPid() + " on "
                + getMachineDescription(virtualMachine.getMachine());
    }
    
    private static String getGridComponentAgentId(VirtualMachine virtualMachine) {
        GridComponent gridComponent = virtualMachine.getElasticServiceManager();
        if (gridComponent == null) {
            gridComponent = virtualMachine.getGridServiceManager();
        }
        if (gridComponent == null) {
            gridComponent = virtualMachine.getGridServiceContainer();
        }
        if (gridComponent == null) {
            gridComponent = virtualMachine.getGridServiceContainer();
        }
        if (gridComponent == null) {
            gridComponent = virtualMachine.getGridServiceAgent();
        }
        if (gridComponent == null) {
            gridComponent = virtualMachine.getLookupService();
        }
        
        String agentId;
        if (gridComponent instanceof AbstractAgentGridComponent) {
            AbstractAgentGridComponent agentGridComponent = (AbstractAgentGridComponent) gridComponent;
            agentId = "-" + agentGridComponent.getAgentId();
        }else {
            agentId = "";
        }

        return agentId + " ";
    }

    /**
     * @return Returns the description of the machine: "[host name]/[host address]"
     */
    public static String getMachineDescription(Machine machine) {
        return machine.getHostName() + " (" + machine.getHostAddress()+")";
    }
    
    /**
     * @return Returns the description of the machine: "[host name]/[host address]"
     */
    public static String getMachineDescription(OperatingSystemDetails details) {
        return details.getHostName() + " (" + details.getHostAddress()+")";
    }

    /**
     * @return "[name].[partition] [primary/backup] on [host name]/[host address]"
     */
    public static String getSpaceInstanceDescription(SpaceInstance spaceInstance) {
        StringBuilder sb = new StringBuilder();
        if (spaceInstance.getSpaceUrl().getSchema().equals("mirror")) {
            sb.append("Mirror ").append(spaceInstance.getSpace().getName()).append(" on ").append(
                    getMachineDescription(spaceInstance.getMachine()));
       }else if (spaceInstance.getSpaceUrl().getSchema().equals("default")) {
           sb.append("Space ").append(spaceInstance.getSpace().getName()).append(" on ").append(
                   getMachineDescription(spaceInstance.getMachine()));
       }else {
            sb.append(spaceInstance.getSpace().getName())
            .append('.')
            .append(spaceInstance.getInstanceId())
            .append(" ["+(spaceInstance.getBackupId()+1)+"] ")
            .append(spaceInstance.getMode())
            .append(" on ")
            .append(getMachineDescription(spaceInstance.getMachine()));
        }
        
        return sb.toString();
    }
    
    /**
     * @return "[name]"
     */
    public static String getProcessingUnitDescription(ProcessingUnit processingUnit) {
        return processingUnit.getName();
    }
}
