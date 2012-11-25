package org.openspaces.itest.persistency.cassandra;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.openspaces.itest.persistency.cassandra.data.MyCassandraPojo1;
import org.openspaces.itest.persistency.cassandra.data.MyCassandraPojo3;
import org.openspaces.itest.persistency.cassandra.data.MyCassandraPojo4;
import org.openspaces.itest.persistency.cassandra.data.MyCassandraSpaceDocumentFactory;
import org.openspaces.itest.persistency.cassandra.mock.MockAddIndexData;
import org.openspaces.itest.persistency.cassandra.mock.MockDataSourceQuery;
import org.openspaces.itest.persistency.cassandra.mock.MockDataSourceSqlQuery;
import org.openspaces.itest.persistency.cassandra.mock.MockIntroduceTypeData;
import org.openspaces.itest.persistency.cassandra.mock.MockOperationsBatchDataBuilder;
import org.openspaces.itest.persistency.cassandra.mock.MockSpaceIndex;

import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.metadata.index.SpaceIndex;
import com.gigaspaces.metadata.index.SpaceIndexType;

public class MultiTypeNestedPropertiesCassandraTest extends AbstractCassandraTest
{

    private SpaceDocument _topLevelSpaceDocument; 
    
    private final MyCassandraPojo3 _pojoInsidePojo = createPojoInsidePojo();
    
    private final SpaceDocument _pojoInsideDocument = createPojoInsideDocument();
    
    private final MyCassandraPojo1 _documentInsidePojo = createDocumentInsidePojo();
    
    private final SpaceDocument _documentInsideDocument = createDocumentInsideDocument();

    private final SpaceDocument _pojoInsidePojoInsideDocument = createPojoInsidePojoInsideDocument(_pojoInsidePojo);
    
    private SpaceTypeDescriptor _typeDescriptor;

    private MockIntroduceTypeData _typeData;

    /**
     we test two different things in this test:
     1) adding index in different stages: 
        on type introduction,
        after type introduction but before writing the actual columns 
        after writing the columns to cassandra
     2) queries different nested properties (document, pojo)
    */
    @Test
    public void test()
    {
        // test indexing

        _topLevelSpaceDocument = createTopLevelDocument();
        
        // not really document only as means of builder style set
        SpaceDocument indexes = new SpaceDocument("indexes")
            .setProperty("pojoInsidePojo.name", null)
            .setProperty("pojoInsidePojo.cassandraPojo4_1.longProperty", null)
            .setProperty("pojoInsidePojo.cassandraPojo4_2.longProperty", null)
            .setProperty("pojoInsideDocument.intProperty", null)
            .setProperty("documentInsidePojo.spaceDocument.firstName", null)
            .setProperty("documentInsideDocument.intProperty", null)
            .setProperty("documentInsideDocument.spaceDocument.intProperty", null)
            .setProperty("pojoInsidePojoInsideDocument.myCassandraPojo3.name", null);
        
        _typeData = createIntroduceTypeDataFromSpaceDocument(_topLevelSpaceDocument, "key", indexes.getProperties().keySet());
        _typeDescriptor = _typeData.getTypeDescriptor();

        _syncInterceptor.onIntroduceType(_typeData);

        addDynamicIndex("pojoInsidePojo.cassandraPojo4_2.dateProperty");
        addDynamicIndex("pojoInsideDocument.myCassandraPojo4.longProperty");
        addDynamicIndex("documentInsidePojo.str");
        addDynamicIndex("documentInsideDocument.spaceDocument.longProperty");
        
        _topLevelSpaceDocument.setProperty("pojoInsidePojoInsideDocument", _pojoInsidePojoInsideDocument);
        
        addDynamicIndex("pojoInsidePojoInsideDocument.myCassandraPojo3.age");
        addDynamicIndex("pojoInsidePojoInsideDocument.myCassandraPojo3.cassandraPojo4_1.longProperty");

        MockOperationsBatchDataBuilder builder = new MockOperationsBatchDataBuilder();
        builder.write(_topLevelSpaceDocument, "key");
        _syncInterceptor.onOperationsBatchSynchronization(builder.build());
        
        _dataSource.initialMetadataLoad();
        
        addDynamicIndex("pojoInsidePojo.age");
        addDynamicIndex("pojoInsidePojo.cassandraPojo4_1.dateProperty");
        addDynamicIndex("pojoInsideDocument.myCassandraPojo4.dateProperty");
        addDynamicIndex("documentInsidePojo.spaceDocument.lastName");
        addDynamicIndex("documentInsideDocument.spaceDocument.booleanProperty");        
        addDynamicIndex("pojoInsidePojoInsideDocument.myCassandraPojo3.cassandraPojo4_1.dateProperty");
        
        // Test queries
        assertValidQuery("pojoInsidePojo.name = ?", _pojoInsidePojo.getName());
        assertValidQuery("pojoInsidePojo.age = ?", _pojoInsidePojo.getAge());
        assertValidQuery("pojoInsidePojo.cassandraPojo4_1.dateProperty = ?", _pojoInsidePojo.getCassandraPojo4_1().getDateProperty());
        
        assertValidQuery("pojoInsideDocument.intProperty = ?", _pojoInsideDocument.getProperty("intProperty"));
        assertValidQuery("pojoInsideDocument.myCassandraPojo4.longProperty = ?", 
             ((MyCassandraPojo4)_pojoInsideDocument.getProperty("myCassandraPojo4")).getLongProperty());
        assertValidQuery("pojoInsideDocument.myCassandraPojo4.dateProperty = ?", 
                         ((MyCassandraPojo4)_pojoInsideDocument.getProperty("myCassandraPojo4")).getDateProperty());
        
        assertValidQuery("documentInsidePojo.str = ?", _documentInsidePojo.getStr());
        assertValidQuery("documentInsidePojo.spaceDocument.firstName = ?", _documentInsidePojo.getSpaceDocument().getProperty("firstName"));
        assertValidQuery("documentInsidePojo.spaceDocument.lastName = ?", _documentInsidePojo.getSpaceDocument().getProperty("lastName"));
        
        assertValidQuery("documentInsideDocument.intProperty = ?", _documentInsideDocument.getProperty("intProperty"));
        assertValidQuery("documentInsideDocument.spaceDocument.intProperty = ?", 
                         ((SpaceDocument)_documentInsideDocument.getProperty("spaceDocument")).getProperty("intProperty"));
        assertValidQuery("documentInsideDocument.spaceDocument.longProperty = ?", 
                         ((SpaceDocument)_documentInsideDocument.getProperty("spaceDocument")).getProperty("longProperty"));
        assertValidQuery("documentInsideDocument.spaceDocument.booleanProperty = ?", 
                         ((SpaceDocument)_documentInsideDocument.getProperty("spaceDocument")).getProperty("booleanProperty"));

        assertValidQuery("pojoInsidePojoInsideDocument.myCassandraPojo3.name = ?", _pojoInsidePojo.getName());
        assertValidQuery("pojoInsidePojoInsideDocument.myCassandraPojo3.age = ?", _pojoInsidePojo.getAge());
        assertValidQuery("pojoInsidePojoInsideDocument.myCassandraPojo3.cassandraPojo4_1.dateProperty = ?", _pojoInsidePojo.getCassandraPojo4_1().getDateProperty());
        
    }
    
    private void assertValidQuery(String query, Object ... params)
    {
        MockDataSourceSqlQuery sqlQuery = new MockDataSourceSqlQuery(query, params);
        MockDataSourceQuery sourceQuery = new MockDataSourceQuery(_typeDescriptor, sqlQuery, Integer.MAX_VALUE);
        DataIterator<Object> iterator = _dataSource.getDataIterator(sourceQuery);
        Assert.assertTrue("Missing result", iterator.hasNext());
        SpaceDocument result = (SpaceDocument) iterator.next();
        iterator.close();
        
        Assert.assertEquals(_pojoInsidePojo, result.getProperty("pojoInsidePojo"));
        Assert.assertEquals(_documentInsidePojo, result.getProperty("documentInsidePojo"));
        Assert.assertEquals(_documentInsideDocument, result.getProperty("documentInsideDocument"));
        Assert.assertEquals(_pojoInsideDocument, result.getProperty("pojoInsideDocument"));
        Assert.assertEquals(_pojoInsidePojoInsideDocument, result.getProperty("pojoInsidePojoInsideDocument"));

        // uncomment if we decide pojo will be restored as documents no matter what
//        SpaceDocument cassandraPojoInsideDocument = result.getProperty("pojoInsideDocument");
//        Assert.assertEquals(_pojoInsideDocument.getProperty("intProperty"), cassandraPojoInsideDocument.getProperty("intProperty"));
//        MyCassandraPojo4 originalMyCassandraPojo4 = _pojoInsideDocument.getProperty("myCassandraPojo4");
//        SpaceDocument myCassandraPojo4AsDocument = cassandraPojoInsideDocument.getProperty("myCassandraPojo4");
//        Assert.assertEquals(originalMyCassandraPojo4.getDateProperty(), myCassandraPojo4AsDocument.getProperty("dateProperty"));
//        Assert.assertEquals(originalMyCassandraPojo4.getLongProperty(), myCassandraPojo4AsDocument.getProperty("longProperty"));
//        
//        SpaceDocument cassnadraPojoInsidePojoInsideDocument = result.getProperty("pojoInsidePojoInsideDocument");
//        SpaceDocument myCassandraPojo3AsDocument = cassnadraPojoInsidePojoInsideDocument.getProperty("myCassandraPojo3");
//        SpaceDocument cassandraPojo4_1AsDocument = myCassandraPojo3AsDocument.getProperty("cassandraPojo4_1");
//        Assert.assertEquals(_pojoInsidePojo.getName(), myCassandraPojo3AsDocument.getProperty("name"));
//        Assert.assertEquals(_pojoInsidePojo.getAge(), myCassandraPojo3AsDocument.getProperty("age"));
//        Assert.assertEquals(_pojoInsidePojo.getCassandraPojo4_1().getDateProperty(), 
//                            cassandraPojo4_1AsDocument.getProperty("dateProperty"));
        
    }
    
    private void addDynamicIndex(String name)
    {
        _syncInterceptor.onAddIndex(new MockAddIndexData(_topLevelSpaceDocument.getTypeName(),
                                                         new SpaceIndex[] { new MockSpaceIndex(name,
                                                                                               SpaceIndexType.BASIC) }));
    }
    
    private SpaceDocument createTopLevelDocument()
    {
        return new SpaceDocument("TopLevelDocument")
            .setProperty("key", random.nextLong())
            .setProperty("pojoInsidePojo", _pojoInsidePojo)
            .setProperty("pojoInsideDocument", _pojoInsideDocument)
            .setProperty("documentInsidePojo", _documentInsidePojo)
            .setProperty("documentInsideDocument", _documentInsideDocument);
    }
    
    private MyCassandraPojo3 createPojoInsidePojo()
    {
        MyCassandraPojo3 pojo3 = new MyCassandraPojo3();
        pojo3.setAge(15);
        pojo3.setName("dank");
        MyCassandraPojo4 cassandraPojo4_1 = new MyCassandraPojo4();
        cassandraPojo4_1.setDateProperty(new Date(123));
        cassandraPojo4_1.setLongProperty(null);
        pojo3.setCassandraPojo4_1(cassandraPojo4_1);
        pojo3.setCassandraPojo4_2(null);
        return pojo3;
    }

    private SpaceDocument createDocumentInsideDocument()
    {
        return MyCassandraSpaceDocumentFactory.getMyCassandraDocument5(
           15,
           MyCassandraSpaceDocumentFactory.getMyCassandraDocument3(3, 
                                                                   5l, 
                                                                   false));
    }

    private MyCassandraPojo1 createDocumentInsidePojo()
    {
        MyCassandraPojo1 pojo1 = new MyCassandraPojo1();
        pojo1.setStr("this is a string");
        pojo1.setSpaceDocument(
            MyCassandraSpaceDocumentFactory.getMyCassandraDocument1("dan dan", "kilman kilman"));
        return pojo1;
    }

    private SpaceDocument createPojoInsideDocument()
    {
        MyCassandraPojo4 pojo4 = new MyCassandraPojo4();
        pojo4.setDateProperty(new Date(123123));
        pojo4.setLongProperty(1555l);
        return MyCassandraSpaceDocumentFactory.getMyCassandraDocument4(89, pojo4);
                                                                       
    }
    
    private SpaceDocument createPojoInsidePojoInsideDocument(
            MyCassandraPojo3 pojoInsidePojo)
    {
        return MyCassandraSpaceDocumentFactory.getMyCassandraDocument6(pojoInsidePojo);
    }
    
}
