package org.openspaces.persistency.cassandra.mock;

import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.sync.IntroduceTypeData;

public class MockIntroduceTypeData
        implements IntroduceTypeData
{
    private final SpaceTypeDescriptor _typeDescriptor;

    public MockIntroduceTypeData(SpaceTypeDescriptor typeDescriptor) 
    {
        _typeDescriptor = typeDescriptor;
    }
    
    @Override
    public SpaceTypeDescriptor getTypeDescriptor()
    {
        return _typeDescriptor;
    }

}
