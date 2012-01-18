package org.openspaces.admin.internal.utils;

import org.openspaces.admin.internal.pu.InternalProcessingUnit;

/**
 * 
 * Utility class that deals with context properties
 * 
 * @author	evgenyf
 * @version	1.0
 * @since	8.0.5
 */
public class ContextPropertyUtils {

    private static final String CONTEXT_PROPERTY_SERVICE_TIER_TYPE = "com.gs.service.type";
    private static final String CONTEXT_PROPERTY_SERVICE_ICON = "com.gs.service.icon";
    private static final String CONTEXT_PROPERTY_CLOUD_NAME = "com.gs.cloudify.cloud-name";
    
    public static ServiceTierType getTierType(InternalProcessingUnit processingUnit) {
        String tierType = getContextPropertyValue(processingUnit, CONTEXT_PROPERTY_SERVICE_TIER_TYPE);
        if (tierType == null) {
            return ServiceTierType.UNDEFINED;
        }
        return ServiceTierType.valueOf(tierType);
    }
    
    public static String getIconPath(InternalProcessingUnit processingUnit) {
        return getContextPropertyValue(processingUnit, CONTEXT_PROPERTY_SERVICE_ICON);
    }
    
    public static String getIconName(InternalProcessingUnit processingUnit) {
        String iconPath = getIconPath( processingUnit );
        if( iconPath == null ){
            return null;
        }
        //find icon name after last /
        int startIndex = iconPath.lastIndexOf( "/" ) >= 0 ? 
                            iconPath.lastIndexOf( "/" ) + 1 : 0;
        return iconPath.substring( startIndex );
    }    
    
    public static String getCloudName(InternalProcessingUnit processingUnit) {
        String cloudName = getContextPropertyValue(processingUnit, CONTEXT_PROPERTY_CLOUD_NAME);
        if (cloudName == null) {
            return "";
        }
        return cloudName;
    }

    private static String getContextPropertyValue(InternalProcessingUnit processingUnit,
            String contextPropertyKey) {
        String value = processingUnit.getBeanLevelProperties().getContextProperties()
                .getProperty(contextPropertyKey);
        return value;
    }
    
}