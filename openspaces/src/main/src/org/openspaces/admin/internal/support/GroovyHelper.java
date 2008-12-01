package org.openspaces.admin.internal.support;

/**
 * @author kimchy
 */
public abstract class GroovyHelper {

    private static final Class closureClass;

    static {
        Class closureClassX = null;
        try {
            closureClassX = GroovyHelper.class.getClassLoader().loadClass("groovy.lang.Closure");
        } catch (ClassNotFoundException e) {

        }
        closureClass = closureClassX;
    }

    public static boolean isClosure(Object obj) {
        if (closureClass == null) {
            return false;
        }
        return closureClass.isAssignableFrom(obj.getClass());
    }
}
