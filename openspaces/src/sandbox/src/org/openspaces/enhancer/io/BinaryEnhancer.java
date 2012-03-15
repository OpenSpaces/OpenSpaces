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
import org.openspaces.enhancer.support.CommonTypes;
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
public class BinaryEnhancer implements Enhancer {

    private static final String BINARY_FIELD_NAME = "__payload";

    public boolean shouldTransform(ClassNode classNode) {
        if (classNode.interfaces.contains("org/openspaces/enhancer/entry/BinaryEntry")) {
            return false;
        }
        List<FieldNode> binaryFields = findBinaryFields(classNode);
        return binaryFields.size() > 0;
    }

    public void transform(ClassNode classNode) {
        if (!shouldTransform(classNode)) {
            return;
        }
        System.out.println("OpenSpaces enhancing class [" + classNode.name + "] with Binary support");

        List<FieldNode> binaryFields = findBinaryFields(classNode);
        // turn all the binary fields to be transient so they won't get marshalled and so on
        for (FieldNode binaryFieldNode : binaryFields) {
            binaryFieldNode.access = binaryFieldNode.access | Opcodes.ACC_TRANSIENT;
        }
        if (binaryFields.size() > 0) {
            FieldNode fieldNode = new FieldNode(Opcodes.ACC_PUBLIC, BINARY_FIELD_NAME, CommonTypes.BINARY_ARRAY.getDescriptor(), null, null);
            classNode.fields.add(fieldNode);
        }
        classNode.interfaces.add("org/openspaces/enhancer/io/BinaryEntry");

        try {
            addBinaryMethods(classNode, binaryFields);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings({"unchecked"})
    private void addBinaryMethods(ClassNode classNode, List<FieldNode> binaryFields) {
        Type ioExceptionType = Type.getType(IOException.class);
        Type classNotFoundException = Type.getType(ClassNotFoundException.class);

        Type byteArrayOutputStream = Type.getObjectType("java/io/ByteArrayOutputStream");
        Type byteArrayInputStream = Type.getObjectType("java/io/ByteArrayInputStream");

        Type classType = Type.getObjectType(classNode.name);

        Method method = Method.getMethod("void pack()");
        final GeneratorAdapter writeGa = new GeneratorAdapter(Opcodes.ACC_PUBLIC, method, null, new Type[]{ioExceptionType}, classNode);

        // call the super class pack
        if (!classNode.superName.equals("java/lang/Object")) {
            writeGa.loadThis();
            writeGa.invokeInsn(Opcodes.INVOKESPECIAL, Type.getObjectType(classNode.superName), Method.getMethod("void pack()"));
        }

        if (binaryFields.size() > 0) {
            writeGa.newInstance(byteArrayOutputStream);
            writeGa.dup();
            writeGa.invokeConstructor(byteArrayOutputStream, new Method("<init>", "()V"));
            int byteArray = writeGa.newLocal(byteArrayOutputStream);
            writeGa.storeLocal(byteArray);
            writeGa.newInstance(CommonTypes.OBJECT_OUTPUT_STREAM_TYPE);
            writeGa.dup();
            writeGa.loadLocal(byteArray);
            writeGa.invokeConstructor(CommonTypes.OBJECT_OUTPUT_STREAM_TYPE, new Method("<init>", "(Ljava/io/OutputStream;)V"));
            final int objectStream = writeGa.newLocal(CommonTypes.OBJECT_OUTPUT_STREAM_TYPE);
            writeGa.storeLocal(objectStream);


            IOHelper.LoadStream loadStream = new IOHelper.LoadStream() {
                public void loadOutputStream() {
                    writeGa.loadLocal(objectStream);
                }
            };

            IOHelper.writeNullHeader(binaryFields, classType, writeGa, loadStream);
            IOHelper.writeValues(binaryFields, classType, writeGa, loadStream);


            writeGa.loadLocal(objectStream);
            writeGa.invokeVirtual(CommonTypes.OBJECT_OUTPUT_STREAM_TYPE, Method.getMethod("void close()"));

            writeGa.loadThis();
            writeGa.loadLocal(byteArray);
            writeGa.invokeVirtual(byteArrayOutputStream, Method.getMethod("byte[] toByteArray()"));
            writeGa.putField(classType, BINARY_FIELD_NAME, CommonTypes.BINARY_ARRAY);
        }
        writeGa.returnValue();
        writeGa.endMethod();

        method = Method.getMethod("void unpack()");
        final GeneratorAdapter readGa = new GeneratorAdapter(Opcodes.ACC_PUBLIC, method, null, new Type[]{ioExceptionType, classNotFoundException}, classNode);

        // call the super class readExteranl
        if (!classNode.superName.equals("java/lang/Object")) {
            readGa.loadThis();
            readGa.invokeInsn(Opcodes.INVOKESPECIAL, Type.getObjectType(classNode.superName), Method.getMethod("void unpack()"));
        }

        if (binaryFields.size() > 0) {
            readGa.newInstance(byteArrayInputStream);
            readGa.dup();
            readGa.loadThis();
            readGa.getField(classType, BINARY_FIELD_NAME, CommonTypes.BINARY_ARRAY);
            readGa.invokeConstructor(byteArrayInputStream, new Method("<init>", "([B)V"));
            int byteArray = readGa.newLocal(byteArrayInputStream);
            readGa.storeLocal(byteArray);
            readGa.newInstance(CommonTypes.OBJECT_INPUT_STEAM_TYPE);
            readGa.dup();
            readGa.loadLocal(byteArray);
            readGa.invokeConstructor(CommonTypes.OBJECT_INPUT_STEAM_TYPE, new Method("<init>", "(Ljava/io/InputStream;)V"));
            final int objectStream = readGa.newLocal(CommonTypes.OBJECT_INPUT_STEAM_TYPE);
            readGa.storeLocal(objectStream);

            IOHelper.LoadStream loadStream = new IOHelper.LoadStream() {
                public void loadOutputStream() {
                    readGa.loadLocal(objectStream);
                }
            };

            // read null values
            Type nullValueType = IOHelper.findNullValueType(binaryFields);
            int nullValueId = IOHelper.readNullHeader(loadStream, readGa, nullValueType);

            // read different values
            IOHelper.readValues(binaryFields, classType, loadStream, readGa, nullValueType, nullValueId);

            readGa.loadLocal(objectStream);
            readGa.invokeVirtual(CommonTypes.OBJECT_INPUT_STEAM_TYPE, Method.getMethod("void close()"));
        }

        readGa.returnValue();
        readGa.endMethod();
    }

    private List<FieldNode> findBinaryFields(ClassNode classNode) {
        List<FieldNode> binaryFields = new ArrayList<FieldNode>();
        for (Object objNode : classNode.fields) {
            FieldNode fieldNode = (FieldNode) objNode;
            if (fieldNode.visibleAnnotations == null) {
                continue;
            }
            for (Object objAnn : fieldNode.visibleAnnotations) {
                AnnotationNode annNode = (AnnotationNode) objAnn;
                if (annNode.desc.equals("Lorg/openspaces/enhancer/io/Binary;")) {
                    binaryFields.add(fieldNode);
                }
            }
        }
        return binaryFields;
    }
}
