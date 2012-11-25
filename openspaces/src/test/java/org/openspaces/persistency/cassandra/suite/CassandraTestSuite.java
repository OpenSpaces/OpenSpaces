package org.openspaces.persistency.cassandra.suite;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.openspaces.persistency.cassandra.BasicCQLQueriesCassandraTest;
import org.openspaces.persistency.cassandra.BasicCassandraTest;
import org.openspaces.persistency.cassandra.ColumnFamilyMetadataSpaceTypeDescriptorConversionTest;
import org.openspaces.persistency.cassandra.CustomSerializersCassandraTest;
import org.openspaces.persistency.cassandra.CyclicReferencePropertyCassandraTest;
import org.openspaces.persistency.cassandra.DataIteratorWithPropertyAddedLaterCassandraTest;
import org.openspaces.persistency.cassandra.DifferentTypesQueryCassandraTest;
import org.openspaces.persistency.cassandra.InitialDataLoadCassandraTest;
import org.openspaces.persistency.cassandra.MaxResultsCassandraTest;
import org.openspaces.persistency.cassandra.MultiTypeCassandraTest;
import org.openspaces.persistency.cassandra.MultiTypeNestedPropertiesCassandraTest;
import org.openspaces.persistency.cassandra.PojoWithPrimitiveTypesCassandraTest;
import org.openspaces.persistency.cassandra.ReadByIdWithPropertyAddedLaterCassandraTest;
import org.openspaces.persistency.cassandra.VeryLongTypeNameCassandraTest;
import org.openspaces.persistency.cassandra.WriteAndRemoveCassandraTest;
import org.openspaces.persistency.cassandra.helper.EmbeddedCassandraController;
import org.openspaces.persistency.cassandra.helper.TestLoggingHelper;


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
        TestLoggingHelper.init();
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
