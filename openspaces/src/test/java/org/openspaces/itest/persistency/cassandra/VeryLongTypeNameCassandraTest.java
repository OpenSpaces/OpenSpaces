package org.openspaces.itest.persistency.cassandra;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openspaces.itest.persistency.cassandra.mock.MockOperationsBatchDataBuilder;

import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.document.SpaceDocument;

public class VeryLongTypeNameCassandraTest extends AbstractCassandraTest
{
    private final String keyName     = "key";
    
    // a simple name longer than 48 chars would suffice just as well 
    private final String typeName = "com.gigaspaces.ridiculously.long.type.name.that.would.cassandra." + 
            "to.complain.on.a.windows.machine." +
            "if.we.were.using.naive.name.conversion.IAmHereForTheSolePurposeOfBeingRidiculouslyLong";
    
    @Before
    public void before()
    {
        _syncInterceptor.onIntroduceType(createIntroduceTypeDataFromSpaceDocument(createSpaceDocument(),
                                                                                  keyName));
        _dataSource.initialMetadataLoad();
    }
    
    @Test
    public void test()
    {
        MockOperationsBatchDataBuilder builder = new MockOperationsBatchDataBuilder();
        builder.write(createSpaceDocument(), keyName);
        _syncInterceptor.onOperationsBatchSynchronization(builder.build());
        DataIterator<Object> iterator = _dataSource.initialDataLoad();
        Assert.assertTrue("Missing document", iterator.hasNext());
        SpaceDocument doc = (SpaceDocument) iterator.next();
        iterator.close();
        Assert.assertEquals("Wrong type name", typeName, doc.getTypeName());
        Assert.assertEquals("Wrong value", 1, doc.getProperty(keyName));
        Assert.assertEquals("Wrong value", true, doc.getProperty("some_prop"));
    }
    
    private SpaceDocument createSpaceDocument()
    {
        return new SpaceDocument(typeName)
            .setProperty(keyName, 1)
            .setProperty("some_prop", true);
    }
    
}
