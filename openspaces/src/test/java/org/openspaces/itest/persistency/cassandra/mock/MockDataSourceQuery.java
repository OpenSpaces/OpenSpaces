package org.openspaces.itest.persistency.cassandra.mock;

import com.gigaspaces.datasource.DataSourceQuery;
import com.gigaspaces.datasource.DataSourceSQLQuery;
import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.metadata.SpaceTypeDescriptor;

public class MockDataSourceQuery implements DataSourceQuery
{
    private final SpaceDocument _query;
    private final SpaceTypeDescriptor _typeDescriptor;
    private final int _maxResults;
    private final DataSourceSQLQuery<Object> _sqlQuery;
    
    public MockDataSourceQuery(SpaceTypeDescriptor typeDescriptor, SpaceDocument query, int maxResults)
    {
        _typeDescriptor = typeDescriptor;
        _maxResults = maxResults;
        _query = query;
        _sqlQuery = null;
    }

    public MockDataSourceQuery(SpaceTypeDescriptor typeDescriptor, DataSourceSQLQuery<Object> sqlQuery, int maxResults)
    {
        _typeDescriptor = typeDescriptor;
        _maxResults = maxResults;
        _query = null;
        _sqlQuery = sqlQuery;
    }
    
    @Override
    public DataSourceSQLQuery<Object> getAsSQLQuery()
    {
        return _sqlQuery;
    }

    @Override
    public Object getTemplateAsObject()
    {
        return null;
    }

    @Override
    public SpaceDocument getTemplateAsDocument()
    {
        return _query;
    }

    @Override
    public boolean supportsAsSQLQuery()
    {
        return _sqlQuery != null;
    }

    @Override
    public boolean supportsTemplateAsObject()
    {
        return false;
    }
    
    @Override
    public boolean supportsTemplateAsDocument()
    {
        return _query != null;
    }

    @Override
    public SpaceTypeDescriptor getTypeDescriptor()
    {
        return _typeDescriptor;
    }

    @Override
    public int getBatchSize()
    {
        return _maxResults;
    }

}
