package org.openspaces.admin.internal.os;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.os.OperatingSystemDetails;
import org.openspaces.admin.support.StatisticsUtils;

import com.gigaspaces.internal.os.OSDetails;
import com.gigaspaces.internal.os.OSDetails.OSDriveDetails;
import com.gigaspaces.internal.os.OSDetails.OSNetInterfaceDetails;
import com.gigaspaces.internal.os.OSDetails.OSVendorDetails;

/**
 * @author kimchy
 * @author itaif - added DriveDetails in v8.0.3
 */
public class DefaultOperatingSystemDetails implements OperatingSystemDetails {

    private final OSDetails details;
    private final Map<String, NetworkDetails> networkDetailsMap = 
                            new HashMap<String, OperatingSystemDetails.NetworkDetails>();
    private final Map<String, DriveDetails> driveDetailsMap = 
                            new HashMap<String, OperatingSystemDetails.DriveDetails>();
    private final VendorDetails vendorDetails;
    
    public DefaultOperatingSystemDetails(OSDetails details) {
        this.details = details;
        
        OSNetInterfaceDetails[] netInterfaceConfigs = details.getNetInterfaceConfigs();
        if( netInterfaceConfigs != null ){
            for(OSNetInterfaceDetails netInterfaceConfig : netInterfaceConfigs){
                networkDetailsMap.put( netInterfaceConfig.getName(), 
                        new DefaultNetworkDetails( netInterfaceConfig ) );    
            }
        }
        
        OSDriveDetails[] driveConfigs = details.getDriveConfigs();
        if( driveConfigs != null ){
            for(OSDriveDetails driveConfig : driveConfigs){
                driveDetailsMap.put( driveConfig.getName(), 
                        new DefaultDriveDetails( driveConfig ) );    
            }
        }
        if (details.getVendorDetails() != null) {
            this.vendorDetails = new DefaultVendorDetails(details.getVendorDetails());
        } else {
            this.vendorDetails = null;
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

        return Collections.unmodifiableMap(networkDetailsMap);
    }
    
    public Map<String, DriveDetails> getDriveDetails() {

        return Collections.unmodifiableMap(driveDetailsMap);
    }
    
    public VendorDetails getVendorDetails() {
        return vendorDetails;
    }
    
    private static class DefaultNetworkDetails implements NetworkDetails {

        private final String name;
        private final String description;
        private final String address; 
        
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
    
    private static class DefaultDriveDetails implements DriveDetails {

        private final String name;
        private final Long capacityInMB;
        
        private DefaultDriveDetails( 
                OSDetails.OSDriveDetails driveDetails ){
            name = driveDetails.getName();
            capacityInMB = driveDetails.getCapacityInMB();
        }
        
        public String getName() {
            return name;
        }

        public Long getCapacityInMB() {
            return capacityInMB;
        }
        
    }
    
    private static class DefaultVendorDetails implements VendorDetails {

        private final String vendor;
        private final String vendorCodeName;
        private final String vendorName;
        private final String vendorVersion;

        public DefaultVendorDetails(OSVendorDetails vendorDetails) {
            vendor = vendorDetails.getVendor();
            vendorCodeName = vendorDetails.getVendorCodeName();
            vendorName = vendorDetails.getVendorName();
            vendorVersion = vendorDetails.getVendorVersion();
        }
        
        @Override
        public String getVendor() {
            return vendor;
        }

        @Override
        public String getVendorCodeName() {
            return vendorCodeName;
        }

        @Override
        public String getVendorName() {
            return vendorName;
        }

        @Override
        public String getVendorVersion() {
            return vendorVersion;
        }
        
    }
}
