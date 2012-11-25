package org.openspaces.persistency.cassandra.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.openspaces.persistency.cassandra.cache.NamedLockProviderTest;
import org.openspaces.persistency.cassandra.meta.mapping.SpaceTypeDescriptorDataHolderTest;
import org.openspaces.persistency.cassandra.meta.mapping.TypeHierarcyTopologySorterTest;
import org.openspaces.persistency.cassandra.meta.types.DynamicPropertyByteBufferConverterTest;


@RunWith(Suite.class)
@SuiteClasses(value= 
{
    NamedLockProviderTest.class,
    SpaceTypeDescriptorDataHolderTest.class,
    TypeHierarcyTopologySorterTest.class,
    DynamicPropertyByteBufferConverterTest.class,
    CassandraTestSuite.class
})
public class AllTestsSuite { }
