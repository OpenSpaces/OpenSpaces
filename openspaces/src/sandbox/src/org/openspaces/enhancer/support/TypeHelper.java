package org.openspaces.enhancer.support;

import org.openspaces.libraries.asm.Type;

/**
 * @author kimchy
 */
public abstract class TypeHelper {

    public static boolean isPrimitive(Type type) {
        switch (type.getSort()) {
            case Type.BYTE:
            case Type.BOOLEAN:
            case Type.SHORT:
            case Type.CHAR:
            case Type.INT:
            case Type.FLOAT:
            case Type.LONG:
            case Type.DOUBLE:
                return true;
            default:
                return false;
        }
    }
}
