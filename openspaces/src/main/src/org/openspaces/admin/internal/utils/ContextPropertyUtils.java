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
    private static final String CONTEXT_PROPERTY_APPLICATION_DEPENDENCIES = "com.gs.application.dependsOn";
    
    public static String getDependencies(InternalProcessingUnit processingUnit) {
        String dependencies = getContextPropertyValue(processingUnit, CONTEXT_PROPERTY_APPLICATION_DEPENDENCIES);
        if (dependencies == null) {
            return "";
        }
        return dependencies;
    }

    public static ServiceTierType getTierType(InternalProcessingUnit processingUnit) {
        String tierTypeStr = getContextPropertyValue(processingUnit, CONTEXT_PROPERTY_SERVICE_TIER_TYPE);
        if (tierTypeStr == null) {
            return ServiceTierType.UNDEFINED;
        }
        return ServiceTierType.valueOf(tierTypeStr);
    }
    
    public static String getIconUrl(InternalProcessingUnit processingUnit) {
        String iconUrlStr = getContextPropertyValue(processingUnit, CONTEXT_PROPERTY_SERVICE_ICON);
        if (iconUrlStr == null) {
            return "";
        }
        return iconUrlStr;
    }

    private static String getContextPropertyValue(InternalProcessingUnit processingUnit,
            String contextPropertyKey) {
        String value = processingUnit.getBeanLevelProperties().getContextProperties()
                .getProperty(contextPropertyKey);
        return value;
    }
    
}