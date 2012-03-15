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

import org.openspaces.enhancer.support.CommonTypes;
import org.openspaces.enhancer.support.TypeHelper;
import org.openspaces.libraries.asm.Label;
import org.openspaces.libraries.asm.Type;
import org.openspaces.libraries.asm.commons.GeneratorAdapter;
import org.openspaces.libraries.asm.commons.Method;
import org.openspaces.libraries.asm.tree.FieldNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kimchy
 */
public class IOHelper {

    public static interface LoadStream {

        void loadOutputStream();
    }

    public static void writeNullHeader(List<FieldNode> fields, Type classType, GeneratorAdapter writeGa, LoadStream loadStream) {
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

    private static int writeNullHolder(List<FieldNode> fields, Type classType, GeneratorAdapter ga, Type nullValueType) {
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

    public static Type findNullValueType(List<FieldNode> fields) {
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


    public static void writeValues(List<FieldNode> fields, Type classType, final GeneratorAdapter writeGa,
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

    private static void writeValue(Type classType, GeneratorAdapter ga, FieldNode fieldNode, Type fieldType, LoadStream loadStream) {
        loadStream.loadOutputStream();
        ga.loadThis();
        ga.getField(classType, fieldNode.name, fieldType);

        if (fieldType.equals(Type.BYTE_TYPE)) {
            ObjectInputOutputHelper.writeByte(ga);
        } else if (fieldType.equals(Type.BOOLEAN_TYPE)) {
            ObjectInputOutputHelper.writeBoolean(ga);
        } else if (fieldType.equals(Type.SHORT_TYPE)) {
            ObjectInputOutputHelper.writeShort(ga);
        } else if (fieldType.equals(Type.CHAR_TYPE)) {
            ObjectInputOutputHelper.writeChar(ga);
        } else if (fieldType.equals(Type.INT_TYPE)) {
            ObjectInputOutputHelper.writeInt(ga);
        } else if (fieldType.equals(Type.FLOAT_TYPE)) {
            ObjectInputOutputHelper.writeFloat(ga);
        } else if (fieldType.equals(Type.LONG_TYPE)) {
            ObjectInputOutputHelper.writeLong(ga);
        } else if (fieldType.equals(Type.DOUBLE_TYPE)) {
            ObjectInputOutputHelper.writeDouble(ga);


        } else if (fieldType.equals(CommonTypes.BYTE_TYPE)) {
            ga.unbox(Type.BYTE_TYPE);
            ObjectInputOutputHelper.writeByte(ga);
        } else if (fieldType.equals(CommonTypes.BOOLEAN_TYPE)) {
            ga.unbox(Type.BOOLEAN_TYPE);
            ObjectInputOutputHelper.writeBoolean(ga);
        } else if (fieldType.equals(CommonTypes.SHORT_TYPE)) {
            ga.unbox(Type.SHORT_TYPE);
            ObjectInputOutputHelper.writeShort(ga);
        } else if (fieldType.equals(CommonTypes.CHARACTER_TYPE)) {
            ga.unbox(Type.CHAR_TYPE);
            ObjectInputOutputHelper.writeChar(ga);
        } else if (fieldType.equals(CommonTypes.INTEGER_TYPE)) {
            ga.unbox(Type.INT_TYPE);
            ObjectInputOutputHelper.writeInt(ga);
        } else if (fieldType.equals(CommonTypes.FLOAT_TYPE)) {
            ga.unbox(Type.FLOAT_TYPE);
            ObjectInputOutputHelper.writeFloat(ga);
        } else if (fieldType.equals(CommonTypes.LONG_TYPE)) {
            ga.unbox(Type.LONG_TYPE);
            ObjectInputOutputHelper.writeLong(ga);
        } else if (fieldType.equals(CommonTypes.DOUBLE_TYPE)) {
            ga.unbox(Type.DOUBLE_TYPE);
            ObjectInputOutputHelper.writeDouble(ga);


        } else if (fieldType.equals(CommonTypes.BIG_DECIMAL_TYPE)) {
            ObjectInputOutputHelper.writeBigDecimal(ga);

        } else if (fieldType.equals(CommonTypes.STRING_TYPE)) {
            ObjectInputOutputHelper.writeString(ga);


        } else if (fieldType.getDescriptor().equals("[B")) {
            // write the getSize, then write the array
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

    public static int readNullHeader(LoadStream loadStream, GeneratorAdapter readGa, Type nullValueType) {
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

    public static void readValues(List<FieldNode> fields, Type classType, LoadStream loadStream,
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

    private static void readValue(GeneratorAdapter ga, Type fieldType, LoadStream loadStream) {
        loadStream.loadOutputStream();
        if (fieldType.equals(Type.BYTE_TYPE)) {
            ObjectInputOutputHelper.readByte(ga);
        } else if (fieldType.equals(Type.BOOLEAN_TYPE)) {
            ObjectInputOutputHelper.readBoolean(ga);
        } else if (fieldType.equals(Type.SHORT_TYPE)) {
            ObjectInputOutputHelper.readShort(ga);
        } else if (fieldType.equals(Type.CHAR_TYPE)) {
            ObjectInputOutputHelper.readChar(ga);
        } else if (fieldType.equals(Type.INT_TYPE)) {
            ObjectInputOutputHelper.readInt(ga);
        } else if (fieldType.equals(Type.FLOAT_TYPE)) {
            ObjectInputOutputHelper.readFloat(ga);
        } else if (fieldType.equals(Type.LONG_TYPE)) {
            ObjectInputOutputHelper.readLong(ga);
        } else if (fieldType.equals(Type.DOUBLE_TYPE)) {
            ObjectInputOutputHelper.readDouble(ga);


        } else if (fieldType.equals(CommonTypes.BYTE_TYPE)) {
            ObjectInputOutputHelper.readByte(ga);
            ga.invokeStatic(CommonTypes.BYTE_TYPE, Method.getMethod("Byte valueOf(byte)"));
        } else if (fieldType.equals(CommonTypes.BOOLEAN_TYPE)) {
            ObjectInputOutputHelper.readBoolean(ga);
            ga.invokeStatic(CommonTypes.BOOLEAN_TYPE, Method.getMethod("Boolean valueOf(boolean)"));
        } else if (fieldType.equals(CommonTypes.SHORT_TYPE)) {
            ObjectInputOutputHelper.readShort(ga);
            ga.invokeStatic(CommonTypes.SHORT_TYPE, Method.getMethod("Short valueOf(short)"));
        } else if (fieldType.equals(CommonTypes.CHARACTER_TYPE)) {
            ObjectInputOutputHelper.readChar(ga);
            ga.invokeStatic(CommonTypes.CHARACTER_TYPE, Method.getMethod("Charecter valueOf(char)"));
        } else if (fieldType.equals(CommonTypes.INTEGER_TYPE)) {
            ObjectInputOutputHelper.readInt(ga);
            ga.invokeStatic(CommonTypes.INTEGER_TYPE, Method.getMethod("Integer valueOf(int)"));
        } else if (fieldType.equals(CommonTypes.FLOAT_TYPE)) {
            ObjectInputOutputHelper.readFloat(ga);
            ga.invokeStatic(CommonTypes.FLOAT_TYPE, Method.getMethod("Float valueOf(float)"));
        } else if (fieldType.equals(CommonTypes.LONG_TYPE)) {
            ObjectInputOutputHelper.readLong(ga);
            ga.invokeStatic(CommonTypes.LONG_TYPE, Method.getMethod("Long valueOf(long)"));
        } else if (fieldType.equals(CommonTypes.DOUBLE_TYPE)) {
            ObjectInputOutputHelper.readDouble(ga);
            ga.invokeStatic(CommonTypes.DOUBLE_TYPE, Method.getMethod("Double valueOf(double)"));


        } else if (fieldType.equals(CommonTypes.BIG_DECIMAL_TYPE)) {
            ObjectInputOutputHelper.readBigDecimal(ga);

        } else if (fieldType.equals(CommonTypes.STRING_TYPE)) {
            // TODO use UTF
            ObjectInputOutputHelper.readString(ga);

            // TODO Support Date
            // TODO Support Calendar
            // TODO Support other array types

        } else if (fieldType.getDescriptor().equals("[B")) {
            // read the getSize, create the array and read it
            // TODO there has to be a faster way to do it - why BinaryFormat.readByteArray does not work?
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

}
