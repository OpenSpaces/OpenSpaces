package org.openspaces.admin.internal.os;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.os.OperatingSystemDetails;
import org.openspaces.admin.support.StatisticsUtils;

import com.gigaspaces.internal.os.OSDetails;
import com.gigaspaces.internal.os.OSDetails.OSNetInterfaceDetails;

/**
 * @author kimchy
 */
public class DefaultOperatingSystemDetails implements OperatingSystemDetails {

    private final OSDetails details;
    private Map<String, NetworkDetails> networkDetailsMap = 
                            new HashMap<String, OperatingSystemDetails.NetworkDetails>();

    public DefaultOperatingSystemDetails(OSDetails details) {
        this.details = details;
        
        OSNetInterfaceDetails[] netInterfaceConfigs = details.getNetInterfaceConfigs();
        if( netInterfaceConfigs != null ){
            for(OSNetInterfaceDetails netInterfaceConfig : netInterfaceConfigs){
                networkDetailsMap.put( netInterfaceConfig.getName(), 
                        new DefaultNetworkDetails( netInterfaceConfig ) );    
            }
        }
    }

    public boolean isNA() {
        return details.isNA();
    }

    public String getUid() {
        return details.getUID();
    }

    public String getName() {
        return details.getName();
    }

    public String getArch() {
        return details.getArch();
    }

    public String getVersion() {
        return details.getVersion();
    }

    public int getAvailableProcessors() {
        return details.getAvailableProcessors();
    }

    public long getTotalSwapSpaceSizeInBytes() {
        return details.getTotalSwapSpaceSize();
    }

    public double getTotalSwapSpaceSizeInMB() {
        return StatisticsUtils.convertToMB(getTotalSwapSpaceSizeInBytes());
    }

    public double getTotalSwapSpaceSizeInGB() {
        return StatisticsUtils.convertToGB(getTotalSwapSpaceSizeInBytes());
    }

    public long getTotalPhysicalMemorySizeInBytes() {
        return details.getTotalPhysicalMemorySize();
    }

    public double getTotalPhysicalMemorySizeInMB() {
        return StatisticsUtils.convertToMB(getTotalPhysicalMemorySizeInBytes());
    }

    public double getTotalPhysicalMemorySizeInGB() {
        return StatisticsUtils.convertToGB(getTotalPhysicalMemorySizeInBytes());
    }

    public String getHostName() {
        return details.getHostName();
    }

    public String getHostAddress() {
        return details.getHostAddress();
    }

    public Map<String, NetworkDetails> getNetworkDetails() {

        return networkDetailsMap;
    }
    
    private class DefaultNetworkDetails implements NetworkDetails {

        private String name;
        private String description;
        private String address; 
        
        private DefaultNetworkDetails( 
                OSDetails.OSNetInterfaceDetails networkInterfaceDetails ){
            name = networkInterfaceDetails.getName();
            description = networkInterfaceDetails.getDescription();
            address = networkInterfaceDetails.getAddress();
        }
        
        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getAddress() {
            return address;
        }
        
    }
}
