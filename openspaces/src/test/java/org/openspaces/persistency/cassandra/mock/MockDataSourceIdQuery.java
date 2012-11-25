package org.openspaces.persistency.cassandra.mock;

import com.gigaspaces.datasource.DataSourceIdQuery;
import com.gigaspaces.metadata.SpaceTypeDescriptor;

public class MockDataSourceIdQuery
        implements DataSourceIdQuery
{
    private final SpaceTypeDescriptor _typeDescriptor;
    private final Object              _id;

    public MockDataSourceIdQuery(SpaceTypeDescriptor typeDescriptor, Object id)
    {
        _typeDescriptor = typeDescriptor;
        _id = id;
    }
    
    @Override
    public SpaceTypeDescriptor getTypeDescriptor()
    {
        return _typeDescriptor;
    }

    @Override
    public Object getId()
    {
        return _id;
    }

    @Override
    public int getVersion()
    {
        return 0;
    }

}
