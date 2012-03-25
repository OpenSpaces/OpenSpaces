package org.openspaces.admin.pu.statistics;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * Defines that statistics are not aggregated but for a specific instance.
 * 
 * @author itaif
 * @since 9.0.0
 */
public class SingleInstanceStatisticsConfig 
    extends AbstractInstancesStatisticsConfig 
    implements InstancesStatisticsConfig {

    private static final String INSTANCE_UID_KEY = "instance-uid";

    public SingleInstanceStatisticsConfig() {
        this(new HashMap<String,String>());
    }
    
    public SingleInstanceStatisticsConfig(Map<String,String> properties) {
        super(properties);
    }
    
    /**
     * @see ProcessingUnitInstance#getUid()
     */
    public String getInstanceUid() {
        return super.getStringProperties().get(INSTANCE_UID_KEY, null);
    }

    public void setInstanceUid(String instanceUid) {
        super.getStringProperties().put(INSTANCE_UID_KEY, instanceUid);
    }

    @Override
    public void validate() throws IllegalStateException {
        if (getInstanceUid() == null) {
            throw new IllegalStateException("instance UID was not specified. Consider using " + EachSingleInstanceStatisticsConfig.class.getName() + " instead");
        }
    }
}
