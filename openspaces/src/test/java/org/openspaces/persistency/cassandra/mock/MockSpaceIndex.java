package org.openspaces.persistency.cassandra.mock;

import com.gigaspaces.metadata.index.SpaceIndex;
import com.gigaspaces.metadata.index.SpaceIndexType;

public class MockSpaceIndex
        implements SpaceIndex
{
    private final String         _name;
    private final SpaceIndexType _type;

    public MockSpaceIndex(String name, SpaceIndexType type)
    {
        _name = name;
        _type = type;
    }
    
    @Override
    public String getName()
    {
        return _name;
    }

    @Override
    public SpaceIndexType getIndexType()
    {
        return _type;
    }

}
