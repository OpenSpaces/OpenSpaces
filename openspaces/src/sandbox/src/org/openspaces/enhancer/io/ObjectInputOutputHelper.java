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

import org.openspaces.libraries.asm.Type;
import org.openspaces.libraries.asm.commons.GeneratorAdapter;
import org.openspaces.libraries.asm.commons.Method;

/**
 * @author kimchy
 */
public abstract class ObjectInputOutputHelper {

    private static final Type OBJECT_COMPRESSOR_TYPE = Type.getObjectType("org/openspaces/enhancer/io/ObjectInputOutputCompressor");

    private static final Type OBJECT_IO_TYPE = Type.getObjectType("org/openspaces/enhancer/io/ObjectInputOutput");

    public static void writeBoolean(GeneratorAdapter ga) {
        ga.invokeStatic(OBJECT_COMPRESSOR_TYPE, Method.getMethod("void writeBoolean(java.io.ObjectOutput, boolean)"));
    }

    public static void readBoolean(GeneratorAdapter ga) {
        ga.invokeStatic(OBJECT_COMPRESSOR_TYPE, Method.getMethod("boolean readBoolean(java.io.ObjectInput)"));
    }

    public static void writeByte(GeneratorAdapter ga) {
        ga.invokeStatic(OBJECT_COMPRESSOR_TYPE, Method.getMethod("void writeByte(java.io.ObjectOutput, byte)"));
    }

    public static void readByte(GeneratorAdapter ga) {
        ga.invokeStatic(OBJECT_COMPRESSOR_TYPE, Method.getMethod("byte readByte(java.io.ObjectInput)"));
    }

    public static void writeChar(GeneratorAdapter ga) {
        ga.invokeStatic(OBJECT_COMPRESSOR_TYPE, Method.getMethod("void writeChar(java.io.ObjectOutput, char)"));
    }

    public static void readChar(GeneratorAdapter ga) {
        ga.invokeStatic(OBJECT_COMPRESSOR_TYPE, Method.getMethod("char readChar(java.io.ObjectInput)"));
    }

    public static void writeShort(GeneratorAdapter ga) {
        ga.invokeStatic(OBJECT_COMPRESSOR_TYPE, Method.getMethod("void writeShort(java.io.ObjectOutput, short)"));
    }

    public static void readShort(GeneratorAdapter ga) {
        ga.invokeStatic(OBJECT_COMPRESSOR_TYPE, Method.getMethod("short readShort(java.io.ObjectInput)"));
    }

    public static void writeInt(GeneratorAdapter ga) {
        ga.invokeStatic(OBJECT_COMPRESSOR_TYPE, Method.getMethod("void writeInt(java.io.ObjectOutput, int)"));
    }

    public static void readInt(GeneratorAdapter ga) {
        ga.invokeStatic(OBJECT_COMPRESSOR_TYPE, Method.getMethod("int readInt(java.io.ObjectInput)"));
    }

    public static void writeLong(GeneratorAdapter ga) {
        ga.invokeStatic(OBJECT_COMPRESSOR_TYPE, Method.getMethod("void writeLong(java.io.ObjectOutput, long)"));
    }

    public static void readLong(GeneratorAdapter ga) {
        ga.invokeStatic(OBJECT_COMPRESSOR_TYPE, Method.getMethod("long readLong(java.io.ObjectInput)"));
    }

    public static void writeFloat(GeneratorAdapter ga) {
        ga.invokeStatic(OBJECT_COMPRESSOR_TYPE, Method.getMethod("void writeFloat(java.io.ObjectOutput, float)"));
    }

    public static void readFloat(GeneratorAdapter ga) {
        ga.invokeStatic(OBJECT_COMPRESSOR_TYPE, Method.getMethod("float readFloat(java.io.ObjectInput)"));
    }

    public static void writeDouble(GeneratorAdapter ga) {
        ga.invokeStatic(OBJECT_COMPRESSOR_TYPE, Method.getMethod("void writeDouble(java.io.ObjectOutput, double)"));
    }

    public static void readDouble(GeneratorAdapter ga) {
        ga.invokeStatic(OBJECT_COMPRESSOR_TYPE, Method.getMethod("double readDouble(java.io.ObjectInput)"));
    }

    public static void writeString(GeneratorAdapter ga) {
        ga.invokeStatic(OBJECT_COMPRESSOR_TYPE, Method.getMethod("void writeString(java.io.ObjectOutput, String)"));
    }

    public static void readString(GeneratorAdapter ga) {
        ga.invokeStatic(OBJECT_COMPRESSOR_TYPE, Method.getMethod("String readString(java.io.ObjectInput)"));
    }

    public static void writeBigDecimal(GeneratorAdapter ga) {
        ga.invokeStatic(OBJECT_COMPRESSOR_TYPE, Method.getMethod("void writeDecimal(java.io.ObjectOutput, java.math.BigDecimal)"));
    }

    public static void readBigDecimal(GeneratorAdapter ga) {
        ga.invokeStatic(OBJECT_COMPRESSOR_TYPE, Method.getMethod("java.math.BigDecimal readDecimal(java.io.ObjectInput)"));
    }
}
