package org.openspaces.persistency.cassandra;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openspaces.persistency.cassandra.data.MyCassandraPojoWithPrimitives;
import org.openspaces.persistency.cassandra.mock.MockOperationsBatchDataBuilder;

import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.document.SpaceDocument;

public class PojoWithPrimitiveTypesCassandraTest extends AbstractCassandraTest
{
    private final String keyName = "key";
    private final String dynamicPropertyName = "dynamic";
    private final String fixedPropertyName = "fixed";
    
    @Before
    public void before()
    {
        _syncInterceptor.onIntroduceType(createIntroduceTypeDataFromSpaceDocument(createDocument(),
                                                                                  keyName));
        _dataSource.initialMetadataLoad();
    }
    
    @Test
    public void test()
    {
        SpaceDocument document = createDocument();
        document.setProperty(dynamicPropertyName, createPojo());
        MockOperationsBatchDataBuilder builder = new MockOperationsBatchDataBuilder();
        builder.write(document, keyName);
        _syncInterceptor.onOperationsBatchSynchronization(builder.build());
        
        DataIterator<Object> iterator = _dataSource.initialDataLoad();
        Assert.assertTrue("no result", iterator.hasNext());
        SpaceDocument documentResult = (SpaceDocument) iterator.next();
        Assert.assertEquals("Bad result", document, documentResult);
        Assert.assertFalse("unexpected result", iterator.hasNext());
        iterator.close();
    }
    
    private SpaceDocument createDocument()
    {
        return new SpaceDocument("MyType")
            .setProperty(keyName, 1)
            .setProperty(fixedPropertyName , createPojo());
    }
    
    private MyCassandraPojoWithPrimitives createPojo()
    {
        MyCassandraPojoWithPrimitives result = new MyCassandraPojoWithPrimitives();
        
        result.setBooleanProperty(true);
        result.setByteProperty((byte)1);
        result.setCharProperty('a');
        result.setShortProperty((short)1);
        result.setIntProperty(1);
        result.setLongProperty(1);
        result.setFloatProperty(1.0f);
        result.setDoubleProperty(1.0);

        result.setBooleanArrayProperty(new boolean[] { true });
        result.setByteArrayProperty(new byte[] { 1 });
        result.setCharArrayProperty(new char[] { 'a' });
        result.setShortArrayProperty(new short[] { 1 });
        result.setIntArrayProperty(new int[] { 1 });
        result.setLongArrayProperty(new long[] { 1 });
        result.setFloatArrayProperty(new float[] { 1.0f });
        result.setDoubleArrayProperty(new double[] { 1.0 });
        
        return result;
    }
}
