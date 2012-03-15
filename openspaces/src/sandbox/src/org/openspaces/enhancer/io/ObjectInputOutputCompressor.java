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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigDecimal;
import java.util.Date;


public class ObjectInputOutputCompressor {

    public final static int _7BIT = 0x7F;
    public final static int _6BIT = 0x3F;
    public final static int _5BIT = 0x1F;
    public final static int _4BIT = 0x0F;
    public final static int BITN7 = 0x40;
    public final static int BITN8 = 0x80;

    public final static int FORBYTES = 0xffff;

    public final static int _8_7_BIT = 0xC0;
    public final static int _8_7_6_BIT = 0xE0;
    public final static int _SECOND_WORD = 0xF0;

    public final static int _ASCII = 127;
    public final static int _FULLY = 64;

    public final static int _MAX_LEN = 32;


    public static boolean readBoolean(ObjectInput ms) throws IOException {
        return ms.read() == 1;
    }

    public static void writeBoolean(ObjectOutput ms, boolean b) throws Exception {
        ms.write(b ? 1 : 0);
    }

    public static byte readByte(ObjectInput in) throws IOException {
        return in.readByte();
    }

    public static void writeByte(ObjectOutput out, byte data) throws IOException {
        out.writeByte(data);
    }

    public static char readChar(ObjectInput in) throws IOException {
        int firstPart = readUnsignedByte(in);
        int part = (firstPart & _SECOND_WORD) >>> 4;

        if (part >= 0 && part <= 7) {
            return (char) firstPart;
        } else if (part == 12 || part == 13) {
            int secondPart = readUnsignedByte(in);
            if ((secondPart & _8_7_BIT) != 128)
                throw new IOException();
            else
                return (char) ((firstPart & _5BIT) << 6 | secondPart & _6BIT);
        } else if (part == 14) {
            int secondPart = readUnsignedByte(in);
            int thirdPart = readUnsignedByte(in);
            if ((secondPart & _8_7_BIT) != 128 || (thirdPart & _8_7_BIT) != 128)
                throw new IOException();
            else
                return (char) ((firstPart & _4BIT) << 12
                        | (secondPart & _6BIT) << 6
                        | thirdPart & _6BIT);
        } else {
            throw new IOException();
        }
    }

    public static void writeChar(ObjectOutput out, int data) throws IOException {
        if (data >= 1 && data <= _ASCII)
            out.write(data);
        else if (data <= 2047) {
            byte buff[] = new byte[_MAX_LEN];
            buff[0] = (byte) (_8_7_BIT | data >>> 6 & _5BIT);
            buff[1] = (byte) (BITN8 | data & _6BIT);
            out.write(buff, 0, 2);
        } else {
            byte buff[] = new byte[_MAX_LEN];
            buff[0] = (byte) (_8_7_6_BIT | data >>> 12 & _4BIT);
            buff[1] = (byte) (BITN8 | data >>> 6 & _6BIT);
            buff[2] = (byte) (BITN8 | data & _6BIT);
            out.write(buff, 0, 3);
        }
    }

    public static short readShort(ObjectInput in) throws IOException {
        return (short) readInt(in);
    }

    public static void writeShort(ObjectOutput out, short data) throws IOException {
        writeInt(out, data);
    }

    public static int readInt(ObjectInput in) throws IOException {
        int mbyte = readUnsignedByte(in);
        int num = mbyte & _6BIT;
        int len = 6;
        boolean sign = (mbyte & BITN7) != 0;

        while ((mbyte & BITN8) != 0) {
            mbyte = readUnsignedByte(in);
            num |= (mbyte & _7BIT) << len;
            len += 7;
        }

        if (sign)
            num = ~num;

        return num;
    }

    public static void writeInt(ObjectOutput out, int data) throws IOException {
        byte buffer[] = new byte[_MAX_LEN];
        int size = 0;
        int mbyte = 0;
        if (data < 0) {
            mbyte = _FULLY;
            data = ~data;
        }
        mbyte |= (byte) (data & _6BIT);
        for (data >>>= 6; data != 0; data >>>= 7) {
            mbyte |= BITN8;
            buffer[size++] = (byte) mbyte;
            mbyte = data & _7BIT;
        }

        if (size == 0) {
            out.write(mbyte);
        } else {
            buffer[size++] = (byte) mbyte;
            out.write(buffer, 0, size);
        }
    }

    public static long readLong(ObjectInput in) throws IOException {
        int mbyte = readUnsignedByte(in);
        long num = mbyte & _6BIT;
        int len = 6;
        boolean nsig = (mbyte & BITN7) != 0;
        while ((mbyte & BITN8) != 0) {
            mbyte = readUnsignedByte(in);
            num |= (long) (mbyte & _7BIT) << len;
            len += 7;
        }
        if (nsig)
            num = ~num;
        return num;
    }

    public static void writeLong(ObjectOutput out, long data) throws IOException {
        byte buffer[] = new byte[_MAX_LEN];
        int size = 0;
        int mbyte = 0;
        if (data < 0L) {
            mbyte = _FULLY;
            data = ~data;
        }
        mbyte |= (byte) ((int) data & _6BIT);
        for (data >>>= 6; data != 0L; data >>>= 7) {
            mbyte |= BITN8;
            buffer[size++] = (byte) mbyte;
            mbyte = (int) data & _7BIT;
        }

        if (size == 0) {
            out.write(mbyte);
        } else {
            buffer[size++] = (byte) mbyte;
            out.write(buffer, 0, size);
        }
    }

    public static float readFloat(ObjectInput in) throws IOException {
        int data = readInt(in);
        return Float.intBitsToFloat(data);
    }

    public static void writeFloat(ObjectOutput out, float data) throws IOException {
        int idata = Float.floatToIntBits(data);
        writeInt(out, idata);
    }

    public static double readDouble(ObjectInput in) throws IOException {
        long data = readLong(in);
        return Double.longBitsToDouble(data);
    }

    public static void writeDouble(ObjectOutput out, double data) throws IOException {
        long ldata = Double.doubleToLongBits(data);
        writeLong(out, ldata);
    }

    public static String readUTF(ObjectInput in) throws IOException {
        return readString(in);
    }

    public static void writeUTF(ObjectOutput out, String data) throws IOException {
        writeString(out, data);
    }

    public static String readString(ObjectInput in) throws IOException {
        int count = readInt(in);
        if (count == 0)
            return "";
        int len = readInt(in);
        byte byff[] = len > 32 ? new byte[len] : new byte[_MAX_LEN];
        readBytes(in, byff, 0, len);
        char charArr[] = new char[count];
        int ofset = 0;
        int size = 0;
        for (; ofset < count; ofset++) {
            int firstPart = byff[size++];
            switch ((firstPart & _SECOND_WORD) >>> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7: {
                    charArr[ofset] = (char) firstPart;
                    break;
                }

                case 12:
                case 13: {
                    int secondPart = byff[size++];
                    if ((secondPart & _8_7_BIT) != 128)
                        throw new RuntimeException();
                    charArr[ofset] = (char) ((firstPart & _5BIT) << 6 | secondPart & _6BIT);
                    break;
                }

                case 14: {
                    int secondPart = byff[size++];
                    int thirdPart = byff[size++];
                    if ((secondPart & _8_7_BIT) != 128 || (thirdPart & _8_7_BIT) != 128)
                        throw new RuntimeException();
                    charArr[ofset] = (char) ((firstPart & _4BIT) << 12
                            | (secondPart & _6BIT) << 6 | thirdPart & _6BIT);
                    break;
                }

                case 8:
                case 9:
                case 10:
                case 11:
                default: {
                    throw new IOException();
                }
            }
        }

        return new String(charArr);
    }

    public static void writeString(ObjectOutput out, String data) throws IOException {
        char buffer[] = data.toCharArray();
        int size = buffer.length;
        writeInt(out, size);
        if (size > 0) {
            int len = size;
            for (int ofset = 0; ofset < size; ofset++) {
                int symbol = buffer[ofset];
                if (symbol <= _ASCII) {
                    if (symbol == 0)
                        len++;
                } else {
                    len += symbol > 2047 ? 2 : 1;
                }
            }

            writeInt(out, len);
            byte mbyte[] = len > _MAX_LEN ? new byte[len] : new byte[_MAX_LEN];
            int offset = 0;
            int pos = 0;
            for (; offset < size; offset++) {
                int symbol = buffer[offset];
                if (symbol >= 1 && symbol <= _ASCII) {
                    mbyte[pos++] = (byte) symbol;
                    continue;
                }
                if (symbol <= 2047) {
                    mbyte[pos++] = (byte) (_8_7_BIT | symbol >>> 6 & _5BIT);
                    mbyte[pos++] = (byte) (BITN8 | symbol & _6BIT);
                } else {
                    mbyte[pos++] = (byte) (_8_7_6_BIT | symbol >>> 12 & _4BIT);
                    mbyte[pos++] = (byte) (BITN8 | symbol >>> 6 & _6BIT);
                    mbyte[pos++] = (byte) (BITN8 | symbol & _6BIT);
                }
            }

            out.write(mbyte, 0, len);
        }
    }

    public static void readBytes(ObjectInput in, byte buff[], int ofset, int len) throws IOException {
        if (ofset < 0 || len < 0 || ofset + len > buff.length)
            throw new IOException();
        int mbyte;
        for (; len > 0; len -= mbyte) {
            mbyte = in.read(buff, ofset, len);
            if (mbyte < 0)
                throw new IOException();
            ofset += mbyte;
        }
    }

    public static char[] readCharArray(ObjectInput in) throws IOException {
        int sz = readInt(in);
        if (sz > 0) {
            byte[] arr = new byte[sz];
            in.read(arr, 0, sz);
            char[] ret = new char[sz];
            for (int i = 0; i < sz; ret[i] = (char) arr[i++]) ;
            return ret;
        } else {
            return null;
        }
    }

    public static void writeCharArray(ObjectOutput out, char[] arr) throws IOException {
        int sz = arr.length;

        writeInt(out, sz);
        if (sz > 0) {
            byte[] bytes = new byte[sz];
            for (int i = 0; i < sz; bytes[i] = (byte) arr[i++]) ;
            out.write(bytes, 0, sz);
        }
    }

    public static Date readDateTime(ObjectInput in) throws IOException {
        long data = readLong(in);
        return new Date(data);
    }

    public static void writeDateTime(ObjectOutput out, Date date) throws IOException {
        long timestamp = date.getTime();
        writeLong(out, timestamp);
    }

    public static BigDecimal readDecimal(ObjectInput in) throws IOException {
        return new BigDecimal(readString(in));
    }

    public static void writeDecimal(ObjectOutput out, BigDecimal bd) throws IOException {
        writeString(out, bd.toEngineeringString());
    }

    public static int readUnsignedByte(ObjectInput in) throws IOException {
        int mbyte = in.read();
        if (mbyte < 0)
            throw new IOException();
        else
            return mbyte;

    }
}


