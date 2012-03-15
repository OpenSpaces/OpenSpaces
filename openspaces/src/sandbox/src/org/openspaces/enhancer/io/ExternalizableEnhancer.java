/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.enhancer.io;

import org.openspaces.enhancer.Enhancer;
import org.openspaces.libraries.asm.Opcodes;
import org.openspaces.libraries.asm.Type;
import org.openspaces.libraries.asm.commons.GeneratorAdapter;
import org.openspaces.libraries.asm.commons.Method;
import org.openspaces.libraries.asm.tree.AnnotationNode;
import org.openspaces.libraries.asm.tree.ClassNode;
import org.openspaces.libraries.asm.tree.FieldNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kimchy
 */
public class ExternalizableEnhancer implements Enhancer {

    private List<String> annotations = new ArrayList<String>();

    public ExternalizableEnhancer() {
        this.annotations.add("Lorg/openspaces/enhancer/io/Externalizable;");
        this.annotations.add("Lorg/openspaces/enhancer/entry/Entry;");
    }

    public void addAnnotation(String annotation) {
        annotations.add(annotation);
    }

    public boolean shouldTransform(ClassNode classNode) {
        if (classNode.interfaces.contains("java/io/Externalizable")) {
            return false;
        }
        return hasAnnotation(classNode);
    }

    public void transform(ClassNode classNode) {
        if (!shouldTransform(classNode)) {
            return;
        }
        System.out.println("OpenSpaces enhancing class [" + classNode.name + "] with Externalizable support");

        //noinspection unchecked
        classNode.interfaces.add("java/io/Externalizable");

        List<FieldNode> transientFields = new ArrayList<FieldNode>();
        for (Object obj : classNode.fields) {
            FieldNode fieldNode = (FieldNode) obj;
            if ((fieldNode.access & Opcodes.ACC_TRANSIENT) == 0) {
                transientFields.add(fieldNode);
            }
        }
        try {
            addExternalizableMethods(classNode, transientFields);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean hasAnnotation(ClassNode classNode) {
        if (classNode.visibleAnnotations == null) {
            return false;
        }
        for (Object objAnn : classNode.visibleAnnotations) {
            AnnotationNode annNode = (AnnotationNode) objAnn;
            if (annotations.contains(annNode.desc)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings({"unchecked"})
    private void addExternalizableMethods(ClassNode classNode, List<FieldNode> publicFields) {
        Type ioExceptionType = Type.getType(IOException.class);
        Type classNotFoundException = Type.getType(ClassNotFoundException.class);

        Type classType = Type.getObjectType(classNode.name);

        Method method = Method.getMethod("void writeExternal(java.io.ObjectOutput)");
        final GeneratorAdapter writeGa = new GeneratorAdapter(Opcodes.ACC_PUBLIC, method, null, new Type[]{ioExceptionType}, classNode);

        // call the super class writeExteranl
        if (!classNode.superName.equals("java/lang/Object")) {
            writeGa.loadThis();
            writeGa.loadArg(0);
            writeGa.invokeInsn(Opcodes.INVOKESPECIAL, Type.getObjectType(classNode.superName), Method.getMethod("void writeExternal(java.io.ObjectOutput)"));
        }

        IOHelper.LoadStream loadStream = new IOHelper.LoadStream() {
            public void loadOutputStream() {
                writeGa.loadArg(0);
            }
        };

        IOHelper.writeNullHeader(publicFields, classType, writeGa, loadStream);
        IOHelper.writeValues(publicFields, classType, writeGa, loadStream);

        writeGa.returnValue();
        writeGa.endMethod();

        method = Method.getMethod("void readExternal(java.io.ObjectInput)");
        final GeneratorAdapter readGa = new GeneratorAdapter(Opcodes.ACC_PUBLIC, method, null, new Type[]{ioExceptionType, classNotFoundException}, classNode);

        // call the super class readExteranl
        if (!classNode.superName.equals("java/lang/Object")) {
            readGa.loadThis();
            readGa.loadArg(0);
            readGa.invokeInsn(Opcodes.INVOKESPECIAL, Type.getObjectType(classNode.superName), Method.getMethod("void readExternal(java.io.ObjectInput)"));
        }

        loadStream = new IOHelper.LoadStream() {
            public void loadOutputStream() {
                readGa.loadArg(0);
            }
        };

        // read null values
        Type nullValueType = IOHelper.findNullValueType(publicFields);
        int nullValueId = IOHelper.readNullHeader(loadStream, readGa, nullValueType);

        // read different values
        IOHelper.readValues(publicFields, classType, loadStream, readGa, nullValueType, nullValueId);
        readGa.returnValue();
        readGa.endMethod();
    }
}
