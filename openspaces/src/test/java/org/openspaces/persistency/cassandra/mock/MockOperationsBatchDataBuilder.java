package org.openspaces.persistency.cassandra.mock;

import java.util.LinkedList;
import java.util.List;

import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;
import com.gigaspaces.sync.DataSyncOperation;
import com.gigaspaces.sync.DataSyncOperationType;
import com.gigaspaces.sync.OperationsBatchData;

public class MockOperationsBatchDataBuilder
{
    private final List<DataSyncOperation> operations = new LinkedList<DataSyncOperation>();
    
    public OperationsBatchData build()
    {
        return new MockOperationsBatchData(operations);
    }
    
    public MockOperationsBatchDataBuilder clear()
    {
        operations.clear();
        return this;
    }
    
    public MockOperationsBatchDataBuilder write(SpaceDocument spaceDoc, String keyName)
    {
        operations.add(createMockDataSyncOperation(spaceDoc, keyName, DataSyncOperationType.WRITE));
        return this;
    }
    
    public MockOperationsBatchDataBuilder update(SpaceDocument spaceDoc, String keyName)
    {
        operations.add(createMockDataSyncOperation(spaceDoc, keyName, DataSyncOperationType.UPDATE));
        return this;
    }
    
    public MockOperationsBatchDataBuilder remove(SpaceDocument spaceDoc, String keyName)
    {
        operations.add(createMockDataSyncOperation(spaceDoc, keyName, DataSyncOperationType.REMOVE));
        return this;
    }
    
    private MockDataSyncOperation createMockDataSyncOperation(SpaceDocument spaceDoc,
            String keyName,
            DataSyncOperationType operationType)
    {
        return new MockDataSyncOperation(
            new SpaceTypeDescriptorBuilder(spaceDoc.getTypeName()).idProperty(keyName).create(),
            spaceDoc,
            operationType);
    }
    
}
