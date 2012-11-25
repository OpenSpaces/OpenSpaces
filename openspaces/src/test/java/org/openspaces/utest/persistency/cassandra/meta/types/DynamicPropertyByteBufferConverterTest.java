package org.openspaces.utest.persistency.cassandra.meta.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openspaces.itest.persistency.cassandra.data.MyCassandraPojo1;
import org.openspaces.persistency.cassandra.meta.types.dynamic.DynamicPropertyValueSerializer;


public class DynamicPropertyByteBufferConverterTest
{

    @Test
    public void test()
    {
        test('a');
        test((byte)23);
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 0xFFFFL * 2; i++)
            sb.append('a');
        test(sb.toString());
        
        test(true);
        test(1234);
        test(31434324324l);
        test(12.12f);
        test(123.123);
        test((short)12323);
        test(UUID.randomUUID());
        test(new Date(123456789));
        test(new MyCassandraPojo1("some string"));
        test(new BigInteger("123123"));
        test(new BigDecimal(new BigInteger("123213213")));
        test(new byte[] { 1, 2, 3, 4 });

    }
    
    private static void test(Object value)
    {
        DynamicPropertyValueSerializer converter = DynamicPropertyValueSerializer.get();
        
        Object actual = converter.fromByteBuffer(converter.toByteBuffer(value));
        if (value.getClass() == byte[].class)
            Assert.assertArrayEquals((byte[])value, (byte[])actual);
        else
            Assert.assertEquals(value, actual);
    }
    
}
