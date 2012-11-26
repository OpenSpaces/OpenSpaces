package org.openspaces.itest.persistency.cassandra;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.openspaces.itest.persistency.cassandra.helper.EmbeddedCassandraController;

import com.gigaspaces.logger.GSLogConfigLoader;


@RunWith(Suite.class)
@SuiteClasses(value= 
{
    DataIteratorWithPropertyAddedLaterCassandraTest.class,
    ReadByIdWithPropertyAddedLaterCassandraTest.class,
    PojoWithPrimitiveTypesCassandraTest.class,
    DifferentTypesQueryCassandraTest.class,
    BasicCQLQueriesCassandraTest.class,
    VeryLongTypeNameCassandraTest.class,
    MaxResultsCassandraTest.class,
    WriteAndRemoveCassandraTest.class,
    CyclicReferencePropertyCassandraTest.class,
    ColumnFamilyMetadataSpaceTypeDescriptorConversionTest.class,
    BasicCassandraTest.class,
    CustomSerializersCassandraTest.class,
    MultiTypeCassandraTest.class,
    MultiTypeNestedPropertiesCassandraTest.class,
    InitialDataLoadCassandraTest.class
})
public class CassandraTestSuite 
{ 
    private static final AtomicInteger runningNumber = new AtomicInteger(0);
    private static boolean isSuiteMode = false;

    private static final EmbeddedCassandraController cassandraController = new EmbeddedCassandraController();
    
    @BeforeClass
    public static void beforeSuite()
    {
        GSLogConfigLoader.getLoader();
        isSuiteMode = true;
        cassandraController.initCassandra(false);
    }
    
    @AfterClass
    public static void afterSuite()
    {
        cassandraController.stopCassandra();
    }

    public static String createKeySpaceAndReturnItsName()
    {
        String keySpaceName = "space" + runningNumber.incrementAndGet();
        cassandraController.createKeySpace(keySpaceName);
        return keySpaceName;
    }

    public static void dropKeySpace(String keySpaceName)
    {
        cassandraController.dropKeySpace(keySpaceName);
    }
    
    public static boolean isSuiteMode()
    {
        return isSuiteMode;
    }

    public static int getRpcPort() 
    {
        return cassandraController.getRpcPort();
    }
    
}
