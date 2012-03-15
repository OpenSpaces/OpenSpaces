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
import java.util.Calendar;
import java.util.Date;

/**
 * @author kimchy
 */
public class ObjectInputOutput {

    public static boolean readBoolean(ObjectInput ms) throws IOException {
        return ms.read() == 1;
    }

    public static void writeBoolean(ObjectOutput ms, boolean b) throws Exception {
        ms.write(b ? 1 : 0);
    }

    public static byte readByte(ObjectInput ms) throws IOException {
        return (byte) ms.read();
    }

    public static void writeByte(ObjectOutput ms, byte b) throws IOException {
        ms.write(b);
    }

    public static char readChar(ObjectInput ms) throws IOException {
        return ms.readChar();
    }

    public static void writeChar(ObjectOutput ms, char c) throws IOException {
        ms.writeChar(c);
    }

    public static void writeShort(ObjectOutput ms, short n) throws IOException {
        ms.writeShort(n);
    }

    public static short readShort(ObjectInput ms) throws IOException {
        return ms.readShort();
    }

    public static void writeInt(ObjectOutput ms, int n) throws IOException {
        ms.writeInt(n);
    }

    public static int readInt(ObjectInput ms) throws IOException {
        return ms.readInt();
    }

    public static void writeLong(ObjectOutput ms, long l) throws IOException {
        ms.writeLong(l);
    }

    public static long readLong(ObjectInput ms) throws IOException {
        return ms.readLong();
    }

    public static void writeFloat(ObjectOutput ms, float flt) throws IOException {
        ms.writeFloat(flt);
    }

    public static float readFloat(ObjectInput ms) throws IOException {
        return ms.readFloat();
    }

    public static void writeDouble(ObjectOutput ms, double dbl) throws IOException {
        ms.writeDouble(dbl);
    }

    public static double readDouble(ObjectInput ms) throws IOException {
        return ms.readDouble();
    }

    public static BigDecimal readDecimal(ObjectInput ms) throws IOException {
        return new BigDecimal(readString(ms));
    }

    public static void writeDecimal(ObjectOutput ms, BigDecimal bd) throws IOException {
        writeString(ms, bd.toEngineeringString());
    }

    public static Calendar readCalendar(ObjectInput ms) throws IOException {
        long value = readLong(ms);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(value);
        return cal;
    }

    public static Date readDateTime(ObjectInput ms) throws IOException {
        long value = readLong(ms);
        return new Date(value);
    }

    public static void writeCalendar(ObjectOutput ms, Calendar cal) throws IOException {
        long timestamp = cal.getTimeInMillis();
        writeLong(ms, timestamp);
    }

    public static void writeDateTime(ObjectOutput ms, Date date) throws IOException {
        long timestamp = date.getTime();
        writeLong(ms, timestamp);
    }

    public static String readUTF(ObjectInput reader) throws IOException, ClassNotFoundException {
        boolean isObject = readBoolean(reader);
        if (isObject) {
            return (String) reader.readObject();
        }
        return reader.readUTF();
    }

    public static String readString(ObjectInput reader) throws IOException {
        int length = readInt(reader);
        if (length > 0) {
            byte[] bytes = new byte[length];
            reader.readFully(bytes, 0, length);

            // convert from byte array
            // TODO write chars and not bytes
            char[] chars = new char[length];
            for (int i = 0; i < length; chars[i] = (char) bytes[i++]) ;

            return new String(chars);
        }
        return "";
    }

    public static void writeUTF(ObjectOutput writer, String str) throws IOException {
        if (str.length() <= 0xFFFFL) {
            writer.writeBoolean(false);
            writer.writeUTF(str);
        } else {
            writer.writeBoolean(true);
            writer.writeObject(str);
        }
    }

    static public void writeString(ObjectOutput writer, String str) throws IOException {
        char[] chars = str.toCharArray();
        int length = chars.length;

        writeInt(writer, length);
        if (length > 0) {
            // convert to byte array
            // TODO read chars and not bytes
            byte[] bytes = new byte[length];
            for (int i = 0; i < length; bytes[i] = (byte) chars[i++]) ;

            writer.write(bytes, 0, length);
        }
    }

}
