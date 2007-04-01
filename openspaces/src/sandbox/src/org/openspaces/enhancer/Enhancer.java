package org.openspaces.enhancer;

import org.openspaces.libraries.asm.tree.ClassNode;

/**
 * @author kimchy
 */
public interface Enhancer {

    boolean shouldTransform(ClassNode classNode);

    void transform(ClassNode classNode);
}
