package org.openspaces.admin.space;

import java.util.Map;

import org.openspaces.core.GigaSpace;

/**
 * API for accessing Space runtime details - classes, templates, count, etc.
 * <p>
 * These calls avoid establishing a proxy to the Space, but is equivalent to
 * <code>
 * ...
 * GigaSpace gigaspace = spaceInstance.getGigaSpace();
 * IJSpace spaceProxy = gigaspace.getSpace();
 * IRemoteJSpaceAdmin spaceAdmin = spaceProxy.getAdmin();
 * SpaceRuntimeInfo info = spaceAdmin.getRuntimeInfo(); 
 * </code>
 * @author Moran Avigdor
 * @since 8.0.3
 */
public interface SpaceInstanceRuntimeDetails {

    /**
     * A count of any null-template matching entry/object in the Space.
     * <p>
     * Count could also be gained by establishing a proxy to the Space.
     * @see SpaceInstance#getGigaSpace()
     * @see GigaSpace#count(Object)
     * @see GigaSpace#count(Object, int)
     * @return a count (gathered periodically).
     */
    int getCount();
    
    /**
     * Returns an array of class names of entry/objects in the Space.
     * @return an array of class names.
     */
    String[] getClassNames();
    
    /**
     * Returns a mapping between each class name and the number of entries/objects in the Space.
     * @return a mapping of class name to entry/object count.
     */
    Map<String, Integer> getCountPerClassName();
    
    /**
     * Returns a mapping between each class name and the number of notify-templates in the Space.
     * @return a mapping of class name to template count.
     */
    Map<String, Integer> getNotifyTemplateCountPerClassName();
}
