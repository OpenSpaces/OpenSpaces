package org.openspaces.enhancer.entry;

import org.openspaces.enhancer.support.BinaryFormatHelper;
import org.openspaces.enhancer.support.CommonTypes;
import org.openspaces.enhancer.support.TypeHelper;
import org.openspaces.libraries.asm.Label;
import org.openspaces.libraries.asm.Opcodes;
import org.openspaces.libraries.asm.Type;
import org.openspaces.libraries.asm.commons.GeneratorAdapter;
import org.openspaces.libraries.asm.commons.Method;
import org.openspaces.libraries.asm.tree.AnnotationNode;
import org.openspaces.libraries.asm.tree.ClassNode;
import org.openspaces.libraries.asm.tree.FieldNode;
import org.openspaces.libraries.asm.tree.MethodNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author kimchy
 */
public class EntryEnhancer {

    private static final String BINARY_FIELD_NAME = "__payload";

    public boolean shouldTransform(ClassNode classNode) {
        if (classNode.interfaces.contains("org/openspaces/enhancer/entry/EnhancedEntry")) {
            return false;
        }
        if (classNode.visibleAnnotations == null) {
            return false;
        }
        for (Object objAnn : classNode.visibleAnnotations) {
            AnnotationNode annNode = (AnnotationNode) objAnn;
            if (annNode.desc.equals("Lorg/openspaces/enhancer/entry/Entry;") ||
                    annNode.desc.equals("Lorg/openspaces/enhancer/entry/Externalizable;")) {
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
        classNode.interfaces.add("net/jini/core/entry/Entry");
        classNode.interfaces.add("org/openspaces/enhancer/entry/EnhancedEntry");

        // add the binary field. Note, we add it before we iterate on the public fields
        List<FieldNode> binaryFields = findBinaryFields(classNode);
        if (binaryFields.size() > 0) {
            FieldNode fieldNode = new FieldNode(Opcodes.ACC_PUBLIC, BINARY_FIELD_NAME, CommonTypes.BINARY_ARRAY.getDescriptor(), null, null);
            classNode.fields.add(fieldNode);
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
            addBinaryMethods(classNode, binaryFields);

            addExternalizableMethods(classNode, publicFields);
            addEntryInfo(classNode, publicFields);
            addSpaceIndexesFields(classNode, publicFields);
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

    @SuppressWarnings({"unchecked"})
    private void addBinaryMethods(ClassNode classNode, List<FieldNode> binaryFields) {
        classNode.interfaces.add("org/openspaces/enhancer/entry/BinaryEntry");
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


            LoadStream loadStream = new LoadStream() {
                public void loadOutputStream() {
                    writeGa.loadLocal(objectStream);
                }
            };

            writeNullHeader(binaryFields, classType, writeGa, loadStream);
            writeValues(binaryFields, classType, writeGa, loadStream);


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

            LoadStream loadStream = new LoadStream() {
                public void loadOutputStream() {
                    readGa.loadLocal(objectStream);
                }
            };

            // read null values
            Type nullValueType = findNullValueType(binaryFields);
            int nullValueId = readNullHeader(loadStream, readGa, nullValueType);

            // read different values
            readValues(binaryFields, classType, loadStream, readGa, nullValueType, nullValueId);

            readGa.loadLocal(objectStream);
            readGa.invokeVirtual(CommonTypes.OBJECT_INPUT_STEAM_TYPE, Method.getMethod("void close()"));
        }

        readGa.returnValue();
        readGa.endMethod();
    }

    @SuppressWarnings({"unchecked"})
    private void addExternalizableMethods(ClassNode classNode, List<FieldNode> publicFields) {
        classNode.interfaces.add("java/io/Externalizable");
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

        LoadStream loadStream = new LoadStream() {
            public void loadOutputStream() {
                writeGa.loadArg(0);
            }
        };

        writeNullHeader(publicFields, classType, writeGa, loadStream);
        writeValues(publicFields, classType, writeGa, loadStream);

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

        loadStream = new LoadStream() {
            public void loadOutputStream() {
                readGa.loadArg(0);
            }
        };

        // read null values
        Type nullValueType = findNullValueType(publicFields);
        int nullValueId = readNullHeader(loadStream, readGa, nullValueType);

        // read different values
        readValues(publicFields, classType, loadStream, readGa, nullValueType, nullValueId);
        readGa.returnValue();
        readGa.endMethod();
    }

    private void readValues(List<FieldNode> fields, Type classType, LoadStream loadStream,
                            GeneratorAdapter readGa, Type nullValueType, int nullValueId) {
        for (int i = 0; i < fields.size(); i++) {
            FieldNode fieldNode = fields.get(i);
            Type fieldType = Type.getType(fieldNode.desc);

            Label lblNull = null;
            if (!TypeHelper.isPrimitive(fieldType)) {
                readGa.loadLocal(nullValueId);
                if (nullValueType == Type.SHORT_TYPE) {
                    readGa.push(1 << i);
                } else if (nullValueType == Type.INT_TYPE) {
                    readGa.push(1 << i);
                } else {
                    readGa.push(1l << i);
                }
                readGa.math(GeneratorAdapter.AND, nullValueType);
                lblNull = readGa.newLabel();
                readGa.ifZCmp(GeneratorAdapter.NE, lblNull);
            }

            readGa.loadThis();
            readValue(readGa, fieldType, loadStream);
            readGa.putField(classType, fieldNode.name, fieldType);

            if (!TypeHelper.isPrimitive(fieldType)) {
                readGa.visitLabel(lblNull);
            }
        }
    }

    private int readNullHeader(LoadStream loadStream, GeneratorAdapter readGa, Type nullValueType) {
        loadStream.loadOutputStream();
        if (nullValueType == Type.SHORT_TYPE) {
            readGa.invokeInterface(CommonTypes.OBJECT_INPUT_TYPE, Method.getMethod("short readShort()"));
        } else if (nullValueType == Type.INT_TYPE) {
            readGa.invokeInterface(CommonTypes.OBJECT_INPUT_TYPE, Method.getMethod("int readInt()"));
        } else {
            readGa.invokeInterface(CommonTypes.OBJECT_INPUT_TYPE, Method.getMethod("long readLong()"));
        }
        int nullValueId = readGa.newLocal(nullValueType);
        readGa.storeLocal(nullValueId);
        return nullValueId;
    }

    private void readValue(GeneratorAdapter ga, Type fieldType, LoadStream loadStream) {
        loadStream.loadOutputStream();
        if (fieldType.equals(Type.BYTE_TYPE)) {
            BinaryFormatHelper.readByte(ga);
        } else if (fieldType.equals(Type.BOOLEAN_TYPE)) {
            BinaryFormatHelper.readBoolean(ga);
        } else if (fieldType.equals(Type.SHORT_TYPE)) {
            BinaryFormatHelper.readShort(ga);
        } else if (fieldType.equals(Type.CHAR_TYPE)) {
            BinaryFormatHelper.readChar(ga);
        } else if (fieldType.equals(Type.INT_TYPE)) {
            BinaryFormatHelper.readInt(ga);
        } else if (fieldType.equals(Type.FLOAT_TYPE)) {
            BinaryFormatHelper.readFloat(ga);
        } else if (fieldType.equals(Type.LONG_TYPE)) {
            BinaryFormatHelper.readLong(ga);
        } else if (fieldType.equals(Type.DOUBLE_TYPE)) {
            BinaryFormatHelper.readDouble(ga);


        } else if (fieldType.equals(CommonTypes.BYTE_TYPE)) {
            BinaryFormatHelper.readByte(ga);
            ga.invokeStatic(CommonTypes.BYTE_TYPE, Method.getMethod("Byte valueOf(byte)"));
        } else if (fieldType.equals(CommonTypes.BOOLEAN_TYPE)) {
            BinaryFormatHelper.readBoolean(ga);
            ga.invokeStatic(CommonTypes.BOOLEAN_TYPE, Method.getMethod("Boolean valueOf(boolean)"));
        } else if (fieldType.equals(CommonTypes.SHORT_TYPE)) {
            BinaryFormatHelper.readShort(ga);
            ga.invokeStatic(CommonTypes.SHORT_TYPE, Method.getMethod("Short valueOf(short)"));
        } else if (fieldType.equals(CommonTypes.CHARACTER_TYPE)) {
            BinaryFormatHelper.readChar(ga);
            ga.invokeStatic(CommonTypes.CHARACTER_TYPE, Method.getMethod("Charecter valueOf(char)"));
        } else if (fieldType.equals(CommonTypes.INTEGER_TYPE)) {
            BinaryFormatHelper.readInt(ga);
            ga.invokeStatic(CommonTypes.INTEGER_TYPE, Method.getMethod("Integer valueOf(int)"));
        } else if (fieldType.equals(CommonTypes.FLOAT_TYPE)) {
            BinaryFormatHelper.readFloat(ga);
            ga.invokeStatic(CommonTypes.FLOAT_TYPE, Method.getMethod("Float valueOf(float)"));
        } else if (fieldType.equals(CommonTypes.LONG_TYPE)) {
            BinaryFormatHelper.readLong(ga);
            ga.invokeStatic(CommonTypes.LONG_TYPE, Method.getMethod("Long valueOf(long)"));
        } else if (fieldType.equals(CommonTypes.DOUBLE_TYPE)) {
            BinaryFormatHelper.readDouble(ga);
            ga.invokeStatic(CommonTypes.DOUBLE_TYPE, Method.getMethod("Double valueOf(double)"));


        } else if (fieldType.equals(CommonTypes.BIG_DECIMAL_TYPE)) {
            BinaryFormatHelper.readBigDecimal(ga);

        } else if (fieldType.equals(CommonTypes.STRING_TYPE)) {
            BinaryFormatHelper.readString(ga);


        } else if (fieldType.getDescriptor().equals("[B")) {
            // read the size, create the array and read it
            // TODO there has to be a faster way to do it
            ga.invokeInterface(CommonTypes.OBJECT_INPUT_TYPE, Method.getMethod("int readInt()"));
            ga.newArray(Type.BYTE_TYPE);
            int byteLocal = ga.newLocal(Type.getType("[B"));
            ga.storeLocal(byteLocal);

            // if zero lengh array don't try to read
            Label lblZeroLengh = ga.newLabel();
            ga.loadLocal(byteLocal);
            ga.arrayLength();
            ga.ifZCmp(GeneratorAdapter.EQ, lblZeroLengh);

            loadStream.loadOutputStream();
            ga.loadLocal(byteLocal);
            ga.invokeInterface(CommonTypes.OBJECT_INPUT_TYPE, Method.getMethod("void readFully(byte[])"));

            ga.visitLabel(lblZeroLengh);
            ga.loadLocal(byteLocal);
        } else {
            ga.invokeInterface(CommonTypes.OBJECT_INPUT_TYPE, Method.getMethod("Object readObject()"));
            ga.checkCast(fieldType);
        }
    }

    private void writeValues(List<FieldNode> fields, Type classType, final GeneratorAdapter writeGa,
                             LoadStream loadStream) {
        // handle write for each field
        Label lblIsNull = null;
        for (FieldNode fieldNode : fields) {
            Type fieldType = Type.getType(fieldNode.desc);

            if (!TypeHelper.isPrimitive(fieldType)) {
                writeGa.loadThis();
                writeGa.getField(classType, fieldNode.name, fieldType);
                lblIsNull = writeGa.newLabel();
                writeGa.ifNull(lblIsNull);
            }

            writeValue(classType, writeGa, fieldNode, fieldType, loadStream);
            if (!TypeHelper.isPrimitive(fieldType)) {
                writeGa.visitLabel(lblIsNull);
            }
        }
    }

    private void writeValue(Type classType, GeneratorAdapter ga, FieldNode fieldNode, Type fieldType, LoadStream loadStream) {
        loadStream.loadOutputStream();
        ga.loadThis();
        ga.getField(classType, fieldNode.name, fieldType);

        if (fieldType.equals(Type.BYTE_TYPE)) {
            BinaryFormatHelper.writeByte(ga);
        } else if (fieldType.equals(Type.BOOLEAN_TYPE)) {
            BinaryFormatHelper.writeBoolean(ga);
        } else if (fieldType.equals(Type.SHORT_TYPE)) {
            BinaryFormatHelper.writeShort(ga);
        } else if (fieldType.equals(Type.CHAR_TYPE)) {
            BinaryFormatHelper.writeChar(ga);
        } else if (fieldType.equals(Type.INT_TYPE)) {
            BinaryFormatHelper.writeInt(ga);
        } else if (fieldType.equals(Type.FLOAT_TYPE)) {
            BinaryFormatHelper.writeFloat(ga);
        } else if (fieldType.equals(Type.LONG_TYPE)) {
            BinaryFormatHelper.writeLong(ga);
        } else if (fieldType.equals(Type.DOUBLE_TYPE)) {
            BinaryFormatHelper.writeDouble(ga);


        } else if (fieldType.equals(CommonTypes.BYTE_TYPE)) {
            ga.unbox(Type.BYTE_TYPE);
            BinaryFormatHelper.writeByte(ga);
        } else if (fieldType.equals(CommonTypes.BOOLEAN_TYPE)) {
            ga.unbox(Type.BOOLEAN_TYPE);
            BinaryFormatHelper.writeBoolean(ga);
        } else if (fieldType.equals(CommonTypes.SHORT_TYPE)) {
            ga.unbox(Type.SHORT_TYPE);
            BinaryFormatHelper.writeShort(ga);
        } else if (fieldType.equals(CommonTypes.CHARACTER_TYPE)) {
            ga.unbox(Type.CHAR_TYPE);
            BinaryFormatHelper.writeChar(ga);
        } else if (fieldType.equals(CommonTypes.INTEGER_TYPE)) {
            ga.unbox(Type.INT_TYPE);
            BinaryFormatHelper.writeInt(ga);
        } else if (fieldType.equals(CommonTypes.FLOAT_TYPE)) {
            ga.unbox(Type.FLOAT_TYPE);
            BinaryFormatHelper.writeFloat(ga);
        } else if (fieldType.equals(CommonTypes.LONG_TYPE)) {
            ga.unbox(Type.LONG_TYPE);
            BinaryFormatHelper.writeLong(ga);
        } else if (fieldType.equals(CommonTypes.DOUBLE_TYPE)) {
            ga.unbox(Type.DOUBLE_TYPE);
            BinaryFormatHelper.writeDouble(ga);


        } else if (fieldType.equals(CommonTypes.BIG_DECIMAL_TYPE)) {
            BinaryFormatHelper.writeBigDecimal(ga);

        } else if (fieldType.equals(CommonTypes.STRING_TYPE)) {
            BinaryFormatHelper.writeString(ga);


        } else if (fieldType.getDescriptor().equals("[B")) {
            // write the size, then write the array
            ga.arrayLength();
            ga.invokeInterface(CommonTypes.OBJECT_OUTPUT_TYPE, Method.getMethod("void writeInt(int)"));
            loadStream.loadOutputStream();
            ga.loadThis();
            ga.getField(classType, fieldNode.name, fieldType);
            ga.invokeInterface(CommonTypes.OBJECT_OUTPUT_TYPE, Method.getMethod("void write(byte[])"));


        } else {
            ga.invokeInterface(CommonTypes.OBJECT_OUTPUT_TYPE, Method.getMethod("void writeObject(Object)"));
        }
    }

    private void writeNullHeader(List<FieldNode> fields, Type classType, GeneratorAdapter writeGa, LoadStream loadStream) {
        // a new local variable for null values
        Type nullValueType = findNullValueType(fields);
        int nullValueId = writeNullHolder(fields, classType, writeGa, nullValueType);
        loadStream.loadOutputStream();
        writeGa.loadLocal(nullValueId);
        if (nullValueType == Type.SHORT_TYPE) {
            writeGa.invokeInterface(CommonTypes.OBJECT_OUTPUT_TYPE, Method.getMethod("void writeShort(int)"));
        } else if (nullValueType == Type.INT_TYPE) {
            writeGa.invokeInterface(CommonTypes.OBJECT_OUTPUT_TYPE, Method.getMethod("void writeInt(int)"));
        } else {
            writeGa.invokeInterface(CommonTypes.OBJECT_OUTPUT_TYPE, Method.getMethod("void writeLong(long)"));
        }
    }

    private int writeNullHolder(List<FieldNode> fields, Type classType, GeneratorAdapter ga, Type nullValueType) {
        // handle null values
        int nullValueId = ga.newLocal(nullValueType);
        ga.push(0);
        ga.storeLocal(nullValueId);
        for (int i = 0; i < fields.size(); i++) {
            FieldNode fieldNode = fields.get(i);
            Type fieldType = Type.getType(fieldNode.desc);
            if (TypeHelper.isPrimitive(fieldType)) {
                continue;
            }
            ga.loadThis();
            ga.getField(classType, fieldNode.name, fieldType);
            Label lblNotNull = ga.newLabel();
            ga.ifNonNull(lblNotNull);
            ga.loadLocal(nullValueId);
            if (nullValueType == Type.SHORT_TYPE) {
                ga.push(1 << i);
            } else if (nullValueType == Type.INT_TYPE) {
                ga.push(1 << i);
            } else {
                ga.push(1l << i);
            }
            ga.math(GeneratorAdapter.OR, nullValueType);
            ga.storeLocal(nullValueId);
            ga.visitLabel(lblNotNull);
        }
        return nullValueId;
    }

    private Type findNullValueType(List<FieldNode> fields) {
        List<FieldNode> temp = new ArrayList<FieldNode>();
        for (FieldNode fieldNode : fields) {
            Type fieldType = Type.getType(fieldNode.desc);
            if (!TypeHelper.isPrimitive(fieldType)) {
                temp.add(fieldNode);
            }
        }
        Type nullValueType;
        if (temp.size() < 16) {
            nullValueType = Type.SHORT_TYPE;
        } else if (temp.size() < 32) {
            nullValueType = Type.INT_TYPE;
        } else if (temp.size() < 64) {
            nullValueType = Type.LONG_TYPE;
        } else {
            throw new IllegalArgumentException("Can't handle more than 64 fields");
        }
        return nullValueType;
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
                if (annNode.desc.equals("Lorg/openspaces/enhancer/entry/Binary;")) {
                    binaryFields.add(fieldNode);
                }
            }
        }
        return binaryFields;
    }


    private interface LoadStream {

        void loadOutputStream();
    }
}
