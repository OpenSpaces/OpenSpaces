package org.openspaces.persistency.cassandra.mock;

import com.gigaspaces.datasource.DataSourceSQLQuery;

public class MockDataSourceSqlQuery
        implements DataSourceSQLQuery<Object>
{
    private final String _query;
    private final Object[] _parameters;

    public MockDataSourceSqlQuery(String query, Object[] parameters)
    {
        _query = query;
        _parameters = parameters;
    }
    
    @Override
    public String getQuery()
    {
        return _query;
    }

    @Override
    public Object[] getQueryParameters()
    {
        return _parameters;
    }
}
