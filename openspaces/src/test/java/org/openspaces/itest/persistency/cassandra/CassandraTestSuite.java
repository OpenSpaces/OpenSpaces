package org.openspaces.itest.persistency.cassandra;

import com.gigaspaces.logger.GSLogConfigLoader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.openspaces.itest.persistency.cassandra.helper.EmbeddedCassandraController;

import java.util.concurrent.atomic.AtomicInteger;


@RunWith(Suite.class)
@SuiteClasses(value= 
{
    ConcurrentColumnFamilyCreationCassandraTest.class,
    DataIteratorWithPropertyAddedLaterCassandraTest.class,
    ReadByIdWithPropertyAddedLaterCassandraTest.class,
    ReadByIdsCassandraTest.class,
    PojoWithPrimitiveTypesCassandraTest.class,
    DifferentTypesQueryCassandraTest.class,
    BasicCQLQueriesCassandraTest.class,
    VeryLongTypeNameCassandraTest.class,
    WriteAndRemoveCassandraTest.class,
    CyclicReferencePropertyCassandraTest.class,
    ColumnFamilyMetadataSpaceTypeDescriptorConversionTest.class,
    BasicCassandraTest.class,
    CustomSerializersCassandraTest.class,
    MultiTypeCassandraTest.class,
    MultiTypeNestedPropertiesCassandraTest.class,
    DifferentConsistencyLevelsCassandraTest.class,
    InitialDataLoadCassandraTest.class
})
public class CassandraTestSuite 
{ 
    private static final AtomicInteger runningNumber = new AtomicInteger(0);
    private static volatile boolean isSuiteMode = false;

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
        isSuiteMode = false;
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
