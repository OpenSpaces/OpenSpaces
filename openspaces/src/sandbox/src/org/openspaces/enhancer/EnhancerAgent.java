package org.openspaces.enhancer;

import org.openspaces.enhancer.entry.EntryEnhancer;
import org.openspaces.libraries.asm.ClassReader;
import org.openspaces.libraries.asm.ClassWriter;
import org.openspaces.libraries.asm.tree.ClassNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * @author kimchy
 */
public class EnhancerAgent {

    private static EntryEnhancer entryEnhancer = new EntryEnhancer();

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new ClassFileTransformer() {
            public byte[] transform(ClassLoader l, String name, Class c,
                                    ProtectionDomain d, byte[] b) throws IllegalClassFormatException {
                ClassNode classNode = new ClassNode();
                ClassReader cr = new ClassReader(b);
                cr.accept(classNode, 0);

                if (!entryEnhancer.shouldTransform(classNode)) {
                    return b;
                }

                entryEnhancer.transform(classNode);
                ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);

                // turn on to the class byte code
//                classNode.accept(new TraceClassVisitor(new PrintWriter(System.out)));

                classNode.accept(cw);
                return cw.toByteArray();
            }
        });
    }
}