package org.openspaces.itest.persistency.cassandra.mock;

import java.util.List;

import com.gigaspaces.sync.DataSyncOperation;
import com.gigaspaces.sync.OperationsBatchData;
import com.gigaspaces.sync.SynchronizationSourceDetails;

public class MockOperationsBatchData
        implements OperationsBatchData
{
    private final DataSyncOperation[] _items;

    public MockOperationsBatchData(List<DataSyncOperation> items)
    {
        _items = items.toArray(new DataSyncOperation[items.size()]);
    }
    
    public MockOperationsBatchData(DataSyncOperation ... items)
    {
        _items = items;
    }
    
    public DataSyncOperation[] getBatchDataItems()
    {
        return _items;
    }

    public SynchronizationSourceDetails getSourceDetails()
    {
        return null;
    }

}
