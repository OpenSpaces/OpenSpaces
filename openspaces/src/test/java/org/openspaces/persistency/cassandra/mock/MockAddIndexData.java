package org.openspaces.persistency.cassandra.mock;

import com.gigaspaces.metadata.index.SpaceIndex;
import com.gigaspaces.sync.AddIndexData;

public class MockAddIndexData
        implements AddIndexData
{
    private final String       _typeName;
    private final SpaceIndex[] _indexes;

    public MockAddIndexData(String typeName, SpaceIndex[] indexes)
    {
        _typeName = typeName;
        _indexes = indexes;
    }
    
    @Override
    public String getTypeName()
    {
        return _typeName;
    }

    @Override
    public SpaceIndex[] getIndexes()
    {
        return _indexes;
    }

}
