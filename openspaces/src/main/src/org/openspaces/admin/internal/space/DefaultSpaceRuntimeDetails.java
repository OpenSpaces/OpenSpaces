package org.openspaces.admin.internal.space;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.SpaceRuntimeDetails;

import com.gigaspaces.cluster.activeelection.SpaceMode;

public class DefaultSpaceRuntimeDetails implements SpaceRuntimeDetails {

    private final DefaultSpace defaultSpace;

    public DefaultSpaceRuntimeDetails(DefaultSpace defaultSpace) {
        this.defaultSpace = defaultSpace;
    }
    
    public String[] getClassNames() {
        Set<String> classNames = new TreeSet<String>(); //keep same order
        for (SpaceInstance spaceInstance : defaultSpace.getSpaceInstances()) {
            if (spaceInstance.getMode() == SpaceMode.PRIMARY) {
                for (String className : spaceInstance.getRuntimeDetails().getClassNames()) {
                    classNames.add(className);
                }
            }
        }
        return classNames.toArray(new String[classNames.size()]);
    }

    public int getCount() {
        int count = 0;
        for (SpaceInstance spaceInstance : defaultSpace.getSpaceInstances()) {
            if (spaceInstance.getMode() == SpaceMode.PRIMARY) {
                count += spaceInstance.getRuntimeDetails().getCount();
            }
        }
        return count;
    }

    public Map<String, Integer> getCountPerClassName() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        for (SpaceInstance spaceInstance : defaultSpace.getSpaceInstances()) {
            if (spaceInstance.getMode() == SpaceMode.PRIMARY) {
                Map<String, Integer> countPerClassName = spaceInstance.getRuntimeDetails().getCountPerClassName();
                for (Entry<String, Integer> entry : countPerClassName.entrySet()) {
                    Integer value = mapping.get(entry.getKey());
                    if (value == null) { 
                        //class name doesn't exist, add it with its count
                        mapping.put(entry.getKey(), entry.getValue());
                    } else {
                        //class name exists, sum both counts
                        int newValue = (value.intValue() + entry.getValue().intValue());
                        mapping.put(entry.getKey(), newValue);
                    }
                }
            }
        }
        return mapping;
    }

    public Map<String, Integer> getNotifyTemplateCountPerClassName() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        for (SpaceInstance spaceInstance : defaultSpace.getSpaceInstances()) {
            if (spaceInstance.getMode() == SpaceMode.PRIMARY) {
                Map<String, Integer> notifyTemplateCountPerClassName = spaceInstance.getRuntimeDetails().getNotifyTemplateCountPerClassName();
                for (Entry<String, Integer> entry : notifyTemplateCountPerClassName.entrySet()) {
                    Integer value = mapping.get(entry.getKey());
                    if (value == null) { 
                        //class name doesn't exist, add it with its notify-template count
                        mapping.put(entry.getKey(), entry.getValue());
                    } else {
                        //class name exists, sum both notify-template counts
                        int newValue = (value.intValue() + entry.getValue().intValue());
                        mapping.put(entry.getKey(), newValue);
                    }
                }
            }
        }
        return mapping;
    }

}
