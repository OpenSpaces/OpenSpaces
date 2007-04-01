package org.openspaces.enhancer.support;

import org.openspaces.libraries.asm.Type;
import org.openspaces.libraries.asm.commons.GeneratorAdapter;
import org.openspaces.libraries.asm.commons.Method;

/**
 * @author kimchy
 */
public abstract class BinaryFormatHelper {

    private static final Type binaryFormatType = Type.getObjectType("com/gigaspaces/serialization/pbs/BinaryFormat");

    public static void writeBoolean(GeneratorAdapter ga) {
        ga.invokeStatic(binaryFormatType, Method.getMethod("void writeBoolean(java.io.ObjectOutput, boolean)"));
    }

    public static void readBoolean(GeneratorAdapter ga) {
        ga.invokeStatic(binaryFormatType, Method.getMethod("boolean readBoolean(java.io.ObjectInput)"));
    }

    public static void writeByte(GeneratorAdapter ga) {
        ga.invokeStatic(binaryFormatType, Method.getMethod("void writeByte(java.io.ObjectOutput, byte)"));
    }

    public static void readByte(GeneratorAdapter ga) {
        ga.invokeStatic(binaryFormatType, Method.getMethod("byte readByte(java.io.ObjectInput)"));
    }

    public static void writeChar(GeneratorAdapter ga) {
        ga.invokeStatic(binaryFormatType, Method.getMethod("void writeChar(java.io.ObjectOutput, char)"));
    }

    public static void readChar(GeneratorAdapter ga) {
        ga.invokeStatic(binaryFormatType, Method.getMethod("char readChar(java.io.ObjectInput)"));
    }

    public static void writeShort(GeneratorAdapter ga) {
        ga.invokeStatic(binaryFormatType, Method.getMethod("void writeShort(java.io.ObjectOutput, short)"));
    }

    public static void readShort(GeneratorAdapter ga) {
        ga.invokeStatic(binaryFormatType, Method.getMethod("short readShort(java.io.ObjectInput)"));
    }

    public static void writeInt(GeneratorAdapter ga) {
        ga.invokeStatic(binaryFormatType, Method.getMethod("void writeInt(java.io.ObjectOutput, int)"));
    }

    public static void readInt(GeneratorAdapter ga) {
        ga.invokeStatic(binaryFormatType, Method.getMethod("int readInt(java.io.ObjectInput)"));
    }

    public static void writeLong(GeneratorAdapter ga) {
        ga.invokeStatic(binaryFormatType, Method.getMethod("void writeLong(java.io.ObjectOutput, long)"));
    }

    public static void readLong(GeneratorAdapter ga) {
        ga.invokeStatic(binaryFormatType, Method.getMethod("long readLong(java.io.ObjectInput)"));
    }

    public static void writeFloat(GeneratorAdapter ga) {
        ga.invokeStatic(binaryFormatType, Method.getMethod("void writeFloat(java.io.ObjectOutput, float)"));
    }

    public static void readFloat(GeneratorAdapter ga) {
        ga.invokeStatic(binaryFormatType, Method.getMethod("float readFloat(java.io.ObjectInput)"));
    }

    public static void writeDouble(GeneratorAdapter ga) {
        ga.invokeStatic(binaryFormatType, Method.getMethod("void writeDouble(java.io.ObjectOutput, double)"));
    }

    public static void readDouble(GeneratorAdapter ga) {
        ga.invokeStatic(binaryFormatType, Method.getMethod("double readDouble(java.io.ObjectInput)"));
    }

    public static void writeString(GeneratorAdapter ga) {
        ga.invokeStatic(binaryFormatType, Method.getMethod("void writeString(java.io.ObjectOutput, String)"));
    }

    public static void readString(GeneratorAdapter ga) {
        ga.invokeStatic(binaryFormatType, Method.getMethod("String readString(java.io.ObjectInput)"));
    }

    public static void writeByteArray(GeneratorAdapter ga) {
        ga.invokeStatic(binaryFormatType, Method.getMethod("void writeByteArray(java.io.ObjectOutput, byte[])"));
    }

    public static void readByteArray(GeneratorAdapter ga) {
        ga.invokeStatic(binaryFormatType, Method.getMethod("byte[] readByteArray(java.io.ObjectInput)"));
    }

    public static void writeBigDecimal(GeneratorAdapter ga) {
        ga.invokeStatic(binaryFormatType, Method.getMethod("void writeDecimal(java.io.ObjectOutput, java.math.BigDecimal)"));
    }

    public static void readBigDecimal(GeneratorAdapter ga) {
        ga.invokeStatic(binaryFormatType, Method.getMethod("java.math.BigDecimal readDecimal(java.io.ObjectInput)"));
    }
}
