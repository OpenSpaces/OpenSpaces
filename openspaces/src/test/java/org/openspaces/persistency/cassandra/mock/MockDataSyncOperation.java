package org.openspaces.persistency.cassandra.mock;

import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.sync.DataSyncOperation;
import com.gigaspaces.sync.DataSyncOperationType;

public class MockDataSyncOperation
        implements DataSyncOperation
{
    private final SpaceDocument         _spaceDoc;
    private final DataSyncOperationType _type;
    private final SpaceTypeDescriptor   _spaceTypeDesc;

    public MockDataSyncOperation(SpaceTypeDescriptor spaceTypeDesc, SpaceDocument spaceDoc, DataSyncOperationType type)
    {
        _spaceTypeDesc = spaceTypeDesc;
        _spaceDoc = spaceDoc;
        _type = type;
    }
    
    public String getUid()
    {
        return null;
    }

    public DataSyncOperationType getDataSyncOperationType()
    {
        return _type;
    }

    public Object getDataAsObject()
    {
        return null;
    }

    public SpaceDocument getDataAsDocument()
    {
        return _spaceDoc;
    }

    public SpaceTypeDescriptor getTypeDescriptor()
    {
        return _spaceTypeDesc;
    }

    public boolean supportsGetTypeDescriptor()
    {
        return true;
    }

    public boolean supportsDataAsObject()
    {
        return false;
    }

    public boolean supportsDataAsDocument()
    {
        return true;
    }

}
