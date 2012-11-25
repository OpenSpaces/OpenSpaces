package org.openspaces.persistency.cassandra;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openspaces.persistency.cassandra.data.MyCassandraCyclicPojoButtom;
import org.openspaces.persistency.cassandra.data.MyCassandraCyclicPojoTop;
import org.openspaces.persistency.cassandra.mock.MockDataSourceQuery;
import org.openspaces.persistency.cassandra.mock.MockDataSourceSqlQuery;
import org.openspaces.persistency.cassandra.mock.MockIntroduceTypeData;
import org.openspaces.persistency.cassandra.mock.MockOperationsBatchDataBuilder;

import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.metadata.SpaceTypeDescriptor;

public class CyclicReferencePropertyCassandraTest extends AbstractCassandraTest
{

    private SpaceDocument _topLevelSpaceDocument;
    private SpaceTypeDescriptor _typeDescriptor;

    @Before
    public void before()
    {
        _topLevelSpaceDocument = createTopLevelDocument();
        
        SpaceDocument indexes = new SpaceDocument("indexes")
            .setProperty("top.number", null)
            .setProperty("top.buttom.number", null)
            .setProperty("top.buttom.top.number", null)
            .setProperty("top.buttom.top.buttom.number", null)
            .setProperty("top.buttom.top.buttom.top.number", null)
            .setProperty("top.buttom.top.buttom.top.buttom.number", null)
            .setProperty("top.buttom.top.buttom.top.buttom.top.number", null)
            .setProperty("top.buttom.top.buttom.top.buttom.top.buttom.number", null)
            .setProperty("top.buttom.top.buttom.top.buttom.top.buttom.top.number", null)
            .setProperty("top.buttom.top.buttom.top.buttom.top.buttom.top.buttom.number", null)
            .setProperty("top.buttom.top.buttom.top.buttom.top.buttom.top.buttom.top.number", null);
        
        MockIntroduceTypeData typeData = createIntroduceTypeDataFromSpaceDocument(_topLevelSpaceDocument, "key", indexes.getProperties().keySet());
        _typeDescriptor = typeData.getTypeDescriptor();
        
        _syncInterceptor.onIntroduceType(typeData);
        
        MockOperationsBatchDataBuilder builder = new MockOperationsBatchDataBuilder();
        builder.write(_topLevelSpaceDocument, "key");
        _syncInterceptor.onOperationsBatchSynchronization(builder.build());
        
        _dataSource.initialMetadataLoad();
    }
    
    @Test
    public void test()
    {
        assertNestingLevel ("");
        assertNestingLevel ("top.number = ?");
        assertNestingLevel ("top.buttom.number = ?");
        assertNestingLevel ("top.buttom.top.number = ?");
        assertNestingLevel ("top.buttom.top.buttom.number = ?");
        assertNestingLevel ("top.buttom.top.buttom.top.number = ?");
        assertNestingLevel ("top.buttom.top.buttom.top.buttom.number = ?");
        assertNestingLevel ("top.buttom.top.buttom.top.buttom.top.number = ?");
        assertNestingLevel ("top.buttom.top.buttom.top.buttom.top.buttom.number = ?");
        assertNestingLevel ("top.buttom.top.buttom.top.buttom.top.buttom.top.number = ?");
        assertNestingLevel ("top.buttom.top.buttom.top.buttom.top.buttom.top.buttom.number = ?");
        assertEntryNotFound("top.buttom.top.buttom.top.buttom.top.buttom.top.buttom.top.number = ?");
    }
    
    private void assertEntryNotFound(String query)
    {
        DataIterator<Object> iterator = getDataIterator(query);
        Assert.assertFalse("Unexpected result", iterator.hasNext());
    }

    @SuppressWarnings("all")
    private void assertNestingLevel(String query)
    {
        DataIterator<Object> iterator = getDataIterator(query);
        Assert.assertTrue("Missing result", iterator.hasNext());
        SpaceDocument result = (SpaceDocument) iterator.next();
        MyCassandraCyclicPojoTop top = result.getProperty("top");
        MyCassandraCyclicPojoButtom buttom = null;
        
        // top is no longer cyclic
        Set<MyCassandraCyclicPojoTop> tops = new HashSet<MyCassandraCyclicPojoTop>();
        Set<MyCassandraCyclicPojoButtom> buttoms = new HashSet<MyCassandraCyclicPojoButtom>();
        
        boolean currentlyTop = true;
        while (true)
        {
            if (currentlyTop)
            {
                if (!tops.add(top))
                    break;
                currentlyTop = false;
                buttom = top.getButtom();
            }
            else
            {
                if (!buttoms.add(buttom))
                    break;
                currentlyTop = true;
                top = buttom.getTop();
            }
        }
        
        Assert.assertEquals("tops count", 6, tops.size());
        Assert.assertEquals("buttoms count", 6, buttoms.size());
        
        iterator.close();
    }

    private DataIterator<Object> getDataIterator(String query)
    {
        MockDataSourceSqlQuery sqlQuery = new MockDataSourceSqlQuery(query, new Object[] { 1 });
        MockDataSourceQuery sourceQuery = new MockDataSourceQuery(_typeDescriptor, sqlQuery, Integer.MAX_VALUE);
        DataIterator<Object> iterator = _dataSource.getDataIterator(sourceQuery);
        return iterator;
    }
    
    private SpaceDocument createTopLevelDocument()
    {
        MyCassandraCyclicPojoTop top = new MyCassandraCyclicPojoTop();
        MyCassandraCyclicPojoButtom buttom = new MyCassandraCyclicPojoButtom();
        top.setButtom(buttom);
        buttom.setTop(top);
        return new SpaceDocument("CyclicRefernceType")
            .setProperty("key", random.nextLong())
            .setProperty("top", top);
    }
    
}
