/*
 * Copyright 2006 GigaSpaces Technologies Inc.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS," WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED INCLUDING BUT NOT LIMITED TO WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT. GIGASPACES WILL NOT BE
 * LIABLE FOR ANY DAMAGE OR LOSS IN CONNECTION WITH THE SOFTWARE.
 */
/*
 * Copyright 2006 GigaSpaces Technologies Inc.
 *
 * Title:		 Compressor.java
 * 
 * @author		 alex
 * @version		 1.0 Jan 14, 2007
 * @since		 5.0EAG Build#
 */

package org.openspaces.enhancer.support;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigDecimal;
import java.util.Date;


public class Compressor
{
    public static void writeUTF(ObjectOutput out, String data) throws IOException
    {
        writeString(out,data); 
    }
    public static String readUTF(ObjectInput in, String data) throws IOException
    {
        return readString(in); 
    }
    
    public static void writeShort (ObjectOutput out, int data) throws IOException
    {
        writeInt(out, data);
    }
    public static short readShort (ObjectInput in) throws IOException
    {
        return (short)readInt(in);
    }
    
    public static void writeFloat(ObjectOutput out,float data) throws IOException
    {
        int idata = Float.floatToIntBits(data);
        writeInt(out, idata);
    }
    public static float readFloat(ObjectInput in) throws IOException
    {
        int data = readInt(in);
        return Float.intBitsToFloat(data);
    }
    
    public static void writeDouble(ObjectOutput out,double data) throws IOException
    {
        long ldata = Double.doubleToLongBits(data);
        writeLong(out, ldata);
    }
    public static double RreadDouble(ObjectInput in) throws IOException {
        long data = readLong(in);
        return Double.longBitsToDouble(data);
    }
    
    public static void writeByte(ObjectOutput out, byte data) throws IOException
    {
        out.writeByte(data);
    }
    public static byte readByte(ObjectInput in) throws IOException
    {
        return in.readByte();
    }
    
    public static void writeLong(ObjectOutput out,long data) throws IOException
    {
        
        byte buffer[] =  new byte[Constants._MAX_LEN];
        int size = 0;
        int mbyte = 0;
        if (data < 0L)
        {
            mbyte = Constants._FULLY;
            data = ~data;
        }
        mbyte |= (byte) ((int) data & Constants._6BIT);
        for (data >>>= 6; data != 0L; data >>>= 7)
        {
            mbyte |= Constants.BITN8;
            buffer[size++] = (byte) mbyte;
            mbyte = (int) data & Constants._7BIT;
        }
        
        if (size == 0)
        {
            out.write(mbyte);
        }
        else
        {
            buffer[size++] = (byte) mbyte;
            out.write(buffer, 0, size);
        }
    }
    public static long readLong(ObjectInput in) throws IOException
    {
        int mbyte = readUnsignedByte(in);
        long num = mbyte & Constants._6BIT;
        int len = 6;
        boolean nsig = (mbyte & Constants.BITN7) != 0;
        while ((mbyte & Constants.BITN8) != 0)
        {
            mbyte = readUnsignedByte(in);
            num |= (long) (mbyte & Constants._7BIT) << len;
            len += 7;
        }
        if (nsig)
            num = ~num;
        return num;
    }
    
    public static void writeChar(ObjectOutput out, int data) throws IOException
    {
        if (data >= 1 && data <= Constants._ASCII)
            out.write(data);
        else if (data <= 2047)
        {
            byte buff[] = new byte[Constants._MAX_LEN];
            buff[0] = (byte) (Constants._8_7_BIT | data >>> 6 & Constants._5BIT);
            buff[1] = (byte) (Constants.BITN8 | data & Constants._6BIT);
            out.write(buff, 0, 2);
        }
        else
        {
            byte buff[] = new byte[Constants._MAX_LEN];
            buff[0] = (byte) (Constants._8_7_6_BIT | data >>> 12 & Constants._4BIT);
            buff[1] = (byte) (Constants.BITN8 | data >>> 6 & Constants._6BIT);
            buff[2] = (byte) (Constants.BITN8 | data & Constants._6BIT);
            out.write(buff, 0, 3);
        }
    }
    public static char readChar(ObjectInput in) throws IOException
    {
        int firstPart = readUnsignedByte(in);
        int part = (firstPart & Constants._SECOND_WORD) >>> 4;
        
        if (part>=0 && part <= 7)   
        {
            return (char) firstPart;
        }
        else if (part == 12 || part == 13)
        {
            int secondPart = readUnsignedByte(in);
            if ((secondPart & Constants._8_7_BIT) != 128)
                throw new IOException();
            else
                return (char) ((firstPart & Constants._5BIT) << 6 | secondPart & Constants._6BIT);
        }
        else if (part == 14) 
        {
            int secondPart = readUnsignedByte(in);
            int thirdPart = readUnsignedByte(in);
            if ((secondPart & Constants._8_7_BIT) != 128 || (thirdPart & Constants._8_7_BIT) != 128)
                throw new IOException();
            else
                return (char) ((firstPart & Constants._4BIT) << 12
                        | (secondPart & Constants._6BIT) << 6
                        |  thirdPart & Constants._6BIT);
        }
        else
        {
            throw new IOException();
        }
    }
    
    public static void writeString(ObjectOutput out,String data) throws IOException
    {
        char buffer[] = data.toCharArray();
        int size = buffer.length;
        writeInt(out,size);
        if (size > 0)
        {
            int len = size;
            for (int ofset = 0; ofset < size; ofset++)
            {
                int symbol = buffer[ofset];
                if (symbol <= Constants._ASCII)
                {
                    if (symbol == 0)
                        len++;
                }
                else
                {
                    len += symbol > 2047 ? 2 : 1;
                }
            }
            
            writeInt(out,len);
            byte mbyte[] = len > Constants._MAX_LEN ? new byte[len] : new byte[Constants._MAX_LEN];
            int offset = 0;
            int pos = 0;
            for (; offset < size; offset++)
            {
                int symbol = buffer[offset];
                if (symbol >= 1 && symbol <= Constants._ASCII)
                {
                    mbyte[pos++] = (byte) symbol;
                    continue;
                }
                if (symbol <= 2047)
                {
                    mbyte[pos++] = (byte) (Constants._8_7_BIT | symbol >>> 6 & Constants._5BIT);
                    mbyte[pos++] = (byte) (Constants.BITN8 | symbol & Constants._6BIT);
                }
                else
                {
                    mbyte[pos++] = (byte) (Constants._8_7_6_BIT | symbol >>> 12 & Constants._4BIT);
                    mbyte[pos++] = (byte) (Constants.BITN8 | symbol >>> 6 & Constants._6BIT);
                    mbyte[pos++] = (byte) (Constants.BITN8 | symbol & Constants._6BIT);
                }
            }
            
            out.write(mbyte, 0, len);
        }
    }   
    public static String readString(ObjectInput in) throws IOException
    {
        int count = readInt(in);
        if (count == 0)
            return "";
        int len = readInt(in);
        byte byff[] = len > 32 ? new byte[len] : new byte[Constants._MAX_LEN];
        readBytes(in,byff, 0, len);
        char charArr[] = new char[count];
        int ofset = 0;
        int size = 0;
        for (; ofset < count; ofset++)
        {
            int firstPart = byff[size++];
            switch ((firstPart & Constants._SECOND_WORD) >>> 4)
            {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            {
                charArr[ofset] = (char) firstPart;
                break;
            }
            
            case 12:
            case 13:
            {
                int secondPart = byff[size++];
                if ((secondPart & Constants._8_7_BIT) != 128)
                    throw new RuntimeException();
                charArr[ofset] = (char) ((firstPart & Constants._5BIT) << 6 | secondPart & Constants._6BIT);
                break;
            }
            
            case 14:
            {
                int secondPart = byff[size++];
                int thirdPart = byff[size++];
                if ((secondPart & Constants._8_7_BIT) != 128 || (thirdPart & Constants._8_7_BIT) != 128)
                    throw new RuntimeException();
                charArr[ofset] = (char) ((firstPart & Constants._4BIT) << 12
                        | (secondPart & Constants._6BIT) << 6 | thirdPart & Constants._6BIT);
                break;
            }
            
            case 8:
            case 9:
            case 10:
            case 11:
            default:
            {
                throw new IOException();
            }
            }
        }
        
        return new String(charArr);
    }
    
    public static void writeInt(ObjectOutput out, int data) throws IOException
    {
        byte buffer[] = new byte[Constants._MAX_LEN];
        int size = 0;
        int mbyte = 0;
        if (data < 0)
        {
            mbyte = Constants._FULLY;
            data = ~data;
        }
        mbyte |= (byte) (data & Constants._6BIT);
        for (data >>>= 6; data != 0; data >>>= 7)
        {
            mbyte |= Constants.BITN8;
            buffer[size++] = (byte) mbyte;
            mbyte = data & Constants._7BIT;
        }
        
        if (size == 0)
        {
            out.write(mbyte);
        }
        else
        {
            buffer[size++] = (byte) mbyte;
            out.write(buffer, 0, size);
        }
    }   
    public static int readInt(ObjectInput in) throws IOException
    {
        int mbyte = readUnsignedByte(in);
        int num = mbyte & Constants._6BIT;
        int len = 6;
        boolean sign = (mbyte & Constants.BITN7) != 0;
        
        while ((mbyte & Constants.BITN8) != 0)
        {
            mbyte = readUnsignedByte(in);
            num |= (mbyte & Constants._7BIT) << len;
            len += 7;
        }
        
        if (sign)
            num = ~num;
        
        return num;
    }
    
    public static void writeByteArray(ObjectOutput out, byte[] arr) throws IOException
    {
        out.write(arr, 0, arr.length);
    }   
    public static void readBytesArray(ObjectInput in, byte[] buff) throws IOException
    {
        readBytes(in, buff, 0, buff.length);
    }
    
    public static void readBytes(ObjectInput in, byte buff[], int ofset, int len) throws IOException
    {
        if (ofset < 0 || len < 0 || ofset + len > buff.length)
            throw new IOException();
        int mbyte;
        for (; len > 0; len -= mbyte)
        {
            mbyte = in.read(buff, ofset, len);
            if (mbyte < 0)
                throw new IOException();
            ofset += mbyte;
        }
    }
    
    public static void writeCharArray(ObjectOutput out, char[] arr)throws IOException 
    {
        int sz = arr.length;
        
        writeInt(out, sz);
        if (sz > 0) 
        {
            byte[] bytes = new byte[sz];
            for (int i = 0; i < sz; bytes[i] = (byte) arr[i++]) ;
            out.write(bytes, 0, sz);
        }
    }
    public static char[] readCharArray(ObjectInput in) throws IOException 
    {
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
    
    public static void writeDateTime(ObjectOutput out, Date date) throws IOException 
    {
        long timestamp = (long) date.getTime();
        writeLong(out, timestamp);
    }
    public static Date readDateTime(ObjectInput in) throws IOException 
    {
        long data = readLong(in);
        return new Date(data);
    }
    
    public static void writeDecimal(ObjectOutput out, BigDecimal bd) throws IOException
    {
        writeString(out, bd.toEngineeringString());
    }
    public static BigDecimal readDecimal(ObjectInput in) throws IOException
    {
        return new BigDecimal(readString(in));
    }
    
    public static int readUnsignedByte(ObjectInput in) throws IOException
    {
        int mbyte = in.read();
        if (mbyte < 0)
            throw new IOException();
        else
            return mbyte;
        
    }
}


