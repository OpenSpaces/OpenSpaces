package org.openspaces.admin.internal.zone.config;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.openspaces.admin.AdminException;
import org.openspaces.admin.zone.config.ZonesConfig;

import com.gigaspaces.internal.utils.StringUtils;

public class ZonesConfigUtils {

    private static final String UNIQUE_ZONES_DELIMITER = "__ZONE__";
    private static final CharSequence UNIQUE_DOT_DELIMITER = "__DOT__";
    
    /**
     * Serializes ZonesConfig into a single string. Sample Input: new
     * ExactZonesConfigurer().addZones("zone1","zone2").create() Sample Output:
     * "org.openspaces.admin.zones.config.ExactZonesConfig__&__zone1__&__zone2"
     */
    public static String zonesToString(ZonesConfig zones) {
        List<String> sortedZones = new ArrayList<String>(zones.getZones());
        Collections.sort(sortedZones);
        for (String zone : sortedZones) {
            if (zone.contains(UNIQUE_ZONES_DELIMITER)) {
                throw new IllegalArgumentException("Zone cannot contain the reserved " + UNIQUE_ZONES_DELIMITER
                        + " sequence");
            }
        }
        sortedZones.add(0, zones.getClass().getName().replace(".", UNIQUE_DOT_DELIMITER));
        return StringUtils.collectionToDelimitedString(sortedZones, UNIQUE_ZONES_DELIMITER);
    }

    /**
     * Deserializes a string into ZonesConfig. Sample Input:
     * "org.openspaces.admin.zones.configExactZonesConfig__&__zone1__&__zone2" Sample Output: new
     * ExactZonesConfigurer().addZones("zone1","zone2").create()
     */
    @SuppressWarnings("unchecked")
    public static ZonesConfig zonesFromString(String key) {
        List<String> sortedZones = new ArrayList<String>(Arrays.asList(StringUtils.delimitedListToStringArray(key,
                UNIQUE_ZONES_DELIMITER)));
        String className = sortedZones.remove(0).replace(UNIQUE_DOT_DELIMITER, ".");
        Class<? extends ZonesConfig> clazz;
        try {
            clazz = (Class<? extends ZonesConfig>) Class.forName(className);
        } catch (ClassCastException e) {
            throw new AdminException("Failed to create class " + className, e);
        } catch (ClassNotFoundException e) {
            throw new AdminException("Failed to create class " + className, e);
        }

        ZonesConfig zonesConfig;
        try {
            zonesConfig = clazz.getConstructor().newInstance();
        } catch (IllegalArgumentException e) {
            throw new AdminException("Failed to launch " + clazz.getName() + " default constructor", e);
        } catch (SecurityException e) {
            throw new AdminException("Failed to launch " + clazz.getName() + " default constructor", e);
        } catch (InstantiationException e) {
            throw new AdminException("Failed to launch " + clazz.getName() + " default constructor", e);
        } catch (IllegalAccessException e) {
            throw new AdminException("Failed to launch " + clazz.getName() + " default constructor", e);
        } catch (InvocationTargetException e) {
            throw new AdminException("Failed to launch " + clazz.getName() + " default constructor", e);
        } catch (NoSuchMethodException e) {
            throw new AdminException("Failed to launch " + clazz.getName() + " default constructor", e);
        }
        zonesConfig.setZones(new HashSet<String>(sortedZones));
        return zonesConfig;
    }
}