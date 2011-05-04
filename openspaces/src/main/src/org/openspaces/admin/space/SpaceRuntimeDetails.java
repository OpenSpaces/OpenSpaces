package org.openspaces.admin.space;

import java.util.Map;

import org.openspaces.core.GigaSpace;

/**
 * Aggregated runtime details of all the currently discovered {@link org.openspaces.admin.space.SpaceInstance}s.
 * <p>
 * These calls avoid establishing a proxy to the Space instances. In case of failover, using the API will always return
 * results from currently discovered instances.
 * 
 * @author Moran Avigdor
 * @since 8.0.3
 */
public interface SpaceRuntimeDetails {

    /**
     * An aggregated count of any null-template matching entry/object in the Space.
     * <p>
     * Count could also be gained by establishing a proxy to the Space.
     * @see SpaceInstance#getGigaSpace()
     * @see GigaSpace#count(Object)
     * @see GigaSpace#count(Object, int)
     * @return a count (gathered periodically).
     */
    int getCount();
    
    /**
     * Returns an aggregated class names array of entry/objects in all the Space instances.
     * @return an array of class names.
     */
    String[] getClassNames();
    
    /**
     * Returns an aggregated mapping between each class name and the number of entries/objects in all the Space instances.
     * @return a mapping of class name to entry/object count.
     */
    Map<String, Integer> getCountPerClassName();
    
    /**
     * Returns an aggregated mapping between each class name and the number of notify-templates in the Space instances.
     * @return a mapping of class name to template count.
     */
    Map<String, Integer> getNotifyTemplateCountPerClassName();
}
