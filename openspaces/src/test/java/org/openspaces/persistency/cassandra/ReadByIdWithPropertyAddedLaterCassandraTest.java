package org.openspaces.persistency.cassandra;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openspaces.persistency.cassandra.data.MyCassandraPojo2;
import org.openspaces.persistency.cassandra.mock.MockDataSourceIdQuery;
import org.openspaces.persistency.cassandra.mock.MockOperationsBatchDataBuilder;

import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.sync.IntroduceTypeData;

public class ReadByIdWithPropertyAddedLaterCassandraTest extends AbstractCassandraTest
{
    private final String           keyName       = "key";
    private final MyCassandraPojo2 keyValue      = new MyCassandraPojo2("dank",
                                                                        13);
    private final String           someProp      = "some_prop";
    private final boolean          somePropValue = true;
    private final String           newProp       = "new_prop";
    private final int              newPropValue  = 2;
    private final String           typeName      = "TypeName";
    private IntroduceTypeData      introduceDataType;
    
    @Before
    public void before()
    {
        introduceDataType = createIntroduceTypeDataFromSpaceDocument(createSpaceDocument(),
                                                                                  keyName);
        _syncInterceptor.onIntroduceType(introduceDataType);
        _dataSource.initialMetadataLoad();
    }
    
    @Test
    public void test()
    {
        MockOperationsBatchDataBuilder builder = new MockOperationsBatchDataBuilder();
        builder.write(createSpaceDocument(), keyName);
        _syncInterceptor.onOperationsBatchSynchronization(builder.build());

        SpaceDocument doc = (SpaceDocument) _dataSource.getById(new MockDataSourceIdQuery(introduceDataType.getTypeDescriptor(), 
                                                                 keyValue));
        
        Assert.assertNotNull("No object found", doc);
        Assert.assertEquals("Wrong type name", typeName, doc.getTypeName());
        Assert.assertEquals("Wrong value", keyValue, doc.getProperty(keyName));
        Assert.assertEquals("Wrong value", somePropValue, doc.getProperty(someProp));
        
        builder.clear().write(createSpaceDocument().setProperty(newProp, newPropValue), keyName);
        _syncInterceptor.onOperationsBatchSynchronization(builder.build());
        
        doc = (SpaceDocument) _dataSource.getById(new MockDataSourceIdQuery(introduceDataType.getTypeDescriptor(), 
                                                   keyValue));
        
        Assert.assertNotNull("No object found", doc);
        Assert.assertEquals("Wrong type name", typeName, doc.getTypeName());
        Assert.assertEquals("Wrong value", keyValue, doc.getProperty(keyName));
        Assert.assertEquals("Wrong value", somePropValue, doc.getProperty(someProp));
        Assert.assertEquals("Wrong value", newPropValue, doc.getProperty(newProp));
    }
    
    private SpaceDocument createSpaceDocument()
    {
        return new SpaceDocument(typeName)
            .setProperty(keyName, keyValue)
            .setProperty(someProp, somePropValue);
    }
    
}
