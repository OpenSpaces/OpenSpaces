package org.openspaces.enhancer;

import org.openspaces.enhancer.entry.EntryEnhancer;
import org.openspaces.enhancer.io.BinaryEnhancer;
import org.openspaces.enhancer.io.ExternalizableEnhancer;
import org.openspaces.libraries.asm.ClassReader;
import org.openspaces.libraries.asm.ClassWriter;
import org.openspaces.libraries.asm.tree.ClassNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kimchy
 */
public class EnhancerAgent {

    private static List<Enhancer> enhancers = new ArrayList<Enhancer>();

    static {
        // NOTE, the order is very important here
        enhancers.add(new BinaryEnhancer());
        enhancers.add(new ExternalizableEnhancer());
        enhancers.add(new EntryEnhancer());
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new ClassFileTransformer() {
            public byte[] transform(ClassLoader l, String name, Class c,
                                    ProtectionDomain d, byte[] b) throws IllegalClassFormatException {
                ClassNode classNode = new ClassNode();
                ClassReader cr = new ClassReader(b);
                cr.accept(classNode, 0);

                for (Enhancer enhancer : enhancers) {
                    if (enhancer.shouldTransform(classNode)) {
                        enhancer.transform(classNode);
                    }
                }

                ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);

                // turn on to the class byte code
//                classNode.accept(new TraceClassVisitor(new PrintWriter(System.out)));

                classNode.accept(cw);
                return cw.toByteArray();
            }
        });
    }
}