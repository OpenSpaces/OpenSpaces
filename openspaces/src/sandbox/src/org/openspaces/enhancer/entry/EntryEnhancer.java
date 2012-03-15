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
package org.openspaces.enhancer.entry;

import org.openspaces.enhancer.Enhancer;
import org.openspaces.enhancer.support.CommonTypes;
import org.openspaces.libraries.asm.Opcodes;
import org.openspaces.libraries.asm.Type;
import org.openspaces.libraries.asm.commons.GeneratorAdapter;
import org.openspaces.libraries.asm.commons.Method;
import org.openspaces.libraries.asm.tree.AnnotationNode;
import org.openspaces.libraries.asm.tree.ClassNode;
import org.openspaces.libraries.asm.tree.FieldNode;
import org.openspaces.libraries.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author kimchy
 */
public class EntryEnhancer implements Enhancer {

    public boolean shouldTransform(ClassNode classNode) {
        if (classNode.interfaces.contains("org/openspaces/enhancer/entry/EnhancedEntry")) {
            return false;
        }
        return hasEntryAnnotation(classNode);
    }

    private boolean hasEntryAnnotation(ClassNode classNode) {
        if (classNode.visibleAnnotations == null) {
            return false;
        }
        for (Object objAnn : classNode.visibleAnnotations) {
            AnnotationNode annNode = (AnnotationNode) objAnn;
            if (annNode.desc.equals("Lorg/openspaces/enhancer/entry/Entry;")) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings({"unchecked"})
    public void transform(ClassNode classNode) {
        if (!shouldTransform(classNode)) {
            return;
        }
        System.out.println("OpenSpaces enhancing class [" + classNode.name + "] with Entry information");

        classNode.version = (classNode.version & 0xFF) < Opcodes.V1_5 ? Opcodes.V1_5 : classNode.version;
        if (hasEntryAnnotation(classNode)) {
            classNode.interfaces.add("net/jini/core/entry/Entry");
            classNode.interfaces.add("org/openspaces/enhancer/entry/EnhancedEntry");
        }

        List<FieldNode> publicFields = new ArrayList<FieldNode>();
        // move all non tranisient fields to public and mark them
        for (Object obj : classNode.fields) {
            FieldNode fieldNode = (FieldNode) obj;
            if ((fieldNode.access & Opcodes.ACC_TRANSIENT) == 0) {
                fieldNode.access = fieldNode.access | Opcodes.ACC_PUBLIC;
                fieldNode.access = fieldNode.access & ~Opcodes.ACC_PRIVATE;
                fieldNode.access = fieldNode.access & ~Opcodes.ACC_PROTECTED;
                publicFields.add(fieldNode);
            }
        }

        try {
            // here we know that we might add empty pack/unpack
            if (hasEntryAnnotation(classNode)) {
                addEntryInfo(classNode, publicFields);
                addSpaceIndexesFields(classNode, publicFields);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addSpaceIndexesFields(ClassNode classNode, List<FieldNode> publicFields) {
        // filter out if has the get Method
        for (Object objNode : classNode.methods) {
            MethodNode methodNode = (MethodNode) objNode;
            if (methodNode.name.equals("__getSpaceIndexedFields")) {
                return;
            }
        }

        LinkedList<FieldNode> indexFields = new LinkedList<FieldNode>();
        for (FieldNode fieldNode : publicFields) {
            if (fieldNode.visibleAnnotations == null) {
                continue;
            }
            for (Object objAnn : fieldNode.visibleAnnotations) {
                AnnotationNode annNode = (AnnotationNode) objAnn;
                if (annNode.desc.equals("Lorg/openspaces/enhancer/entry/RoutingIndex;")) {
                    indexFields.addFirst(fieldNode);
                } else if (annNode.desc.equals("Lorg/openspaces/enhancer/entry/Indexed;")) {
                    indexFields.add(fieldNode);
                }
            }
        }

        int length = indexFields.size();
        if (length == 0) {
            return;
        }

        Method method = Method.getMethod("String[] __getSpaceIndexedFields()");
        GeneratorAdapter ga = new GeneratorAdapter(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, method, null, null, classNode);
        int arrayLocal = ga.newLocal(Type.getType(String[].class));
        ga.push(length);
        ga.newArray(CommonTypes.STRING_TYPE);
        ga.storeLocal(arrayLocal);

        for (int i = 0; i < indexFields.size(); i++) {
            ga.loadLocal(arrayLocal);
            ga.push(i);
            ga.push(indexFields.get(i).name);
            ga.arrayStore(CommonTypes.STRING_TYPE);
        }
        ga.loadLocal(arrayLocal);
        ga.returnValue();
        ga.endMethod();
    }

    private void addEntryInfo(ClassNode classNode, List<FieldNode> publicFields) {
        // filter out if has the get Method
        for (Object objNode : classNode.methods) {
            MethodNode methodNode = (MethodNode) objNode;
            if (methodNode.name.equals("__getEntryInfo")) {
                return;
            }
        }

        FieldNode idFieldNode = null;
        FieldNode versionFieldNode = null;
        FieldNode timeToLiveNode = null;
        for (FieldNode fieldNode : publicFields) {
            if (fieldNode.visibleAnnotations == null) {
                continue;
            }
            for (Object objAnn : fieldNode.visibleAnnotations) {
                AnnotationNode annNode = (AnnotationNode) objAnn;
                if (annNode.desc.equals("Lorg/openspaces/enhancer/entry/EntryId;")) {
                    if (idFieldNode != null) {
                        throw new IllegalArgumentException("Class [" + classNode.name + "] can only have a single EntryId");
                    }
                    idFieldNode = fieldNode;
                }
                if (annNode.desc.equals("Lorg/openspaces/enhancer/entry/EntryVersion;")) {
                    if (versionFieldNode != null) {
                        throw new IllegalArgumentException("Class [" + classNode.name + "] can only have a single EntryVerison");
                    }
                    versionFieldNode = fieldNode;
                }
                if (annNode.desc.equals("Lorg/openspaces/enhancer/entry/EntryTimeToLive;")) {
                    if (versionFieldNode != null) {
                        throw new IllegalArgumentException("Class [" + classNode.name + "] can only have a single EntryTimeToLive");
                    }
                    timeToLiveNode = fieldNode;
                }
            }
        }

        // don't do anything if nothing is defined
        if (idFieldNode == null && versionFieldNode == null && timeToLiveNode == null) {
            return;
        }

        // perform validation on the types
        Type idFieldType = null;
        if (idFieldNode != null) {
            idFieldType = Type.getType(idFieldNode.desc);
            if (!idFieldType.equals(CommonTypes.STRING_TYPE)) {
                throw new IllegalStateException("EntryId field type must be String");
            }
        }
        Type versionFieldType = null;
        if (versionFieldNode != null) {
            versionFieldType = Type.getType(versionFieldNode.desc);
            if (!versionFieldType.equals(Type.INT_TYPE)) {
                throw new IllegalStateException("EntryVersion field type must be int");
            }
        }
        Type timeToLiveFieldType = null;
        if (timeToLiveNode != null) {
            timeToLiveFieldType = Type.getType(timeToLiveNode.desc);
            if (!timeToLiveFieldType.equals(Type.LONG_TYPE)) {
                throw new IllegalStateException("EntryTimeToLive field type must be long");
            }
        }

        Type entryInfoType = Type.getObjectType("com/j_spaces/core/client/EntryInfo");

        // add public String __getEntryUID()
        Method method = Method.getMethod("com.j_spaces.core.client.EntryInfo __getEntryInfo()");
        GeneratorAdapter ga = new GeneratorAdapter(Opcodes.ACC_PUBLIC, method, null, null, classNode);
        ga.newInstance(entryInfoType);
        ga.dup();
        if (idFieldNode != null) {
            ga.loadThis();
            ga.getField(Type.getObjectType(classNode.name), idFieldNode.name, idFieldType);
        } else {
            ga.push((String) null);
        }
        if (versionFieldNode != null) {
            ga.loadThis();
            ga.getField(Type.getObjectType(classNode.name), versionFieldNode.name, versionFieldType);
        } else {
            ga.push(0);
        }
        if (timeToLiveNode != null) {
            ga.loadThis();
            ga.getField(Type.getObjectType(classNode.name), timeToLiveNode.name, timeToLiveFieldType);
        } else {
            ga.push((long) 0);
        }
        ga.invokeConstructor(entryInfoType, new Method("<init>", "(Ljava/lang/String;IJ)V"));
        ga.returnValue();
        ga.endMethod();

        // add public String __getEntryUID()
        method = Method.getMethod("void __setEntryInfo(com.j_spaces.core.client.EntryInfo)");
        ga = new GeneratorAdapter(Opcodes.ACC_PUBLIC, method, null, null, classNode);

        if (idFieldNode != null) {
            ga.loadThis();
            ga.loadArg(0);
            ga.getField(entryInfoType, "m_UID", CommonTypes.STRING_TYPE);
            ga.putField(Type.getObjectType(classNode.name), idFieldNode.name, idFieldType);
        }

        if (versionFieldNode != null) {
            ga.loadThis();
            ga.loadArg(0);
            ga.getField(entryInfoType, "m_VersionID", Type.INT_TYPE);
            ga.putField(Type.getObjectType(classNode.name), versionFieldNode.name, versionFieldType);
        }

        if (timeToLiveFieldType != null) {
            ga.loadThis();
            ga.loadArg(0);
            ga.getField(entryInfoType, "m_TimeToLive", Type.LONG_TYPE);
            ga.putField(Type.getObjectType(classNode.name), timeToLiveNode.name, timeToLiveFieldType);
        }

        ga.returnValue();
        ga.endMethod();
    }

}
