package org.openspaces.itest.persistency.cassandra.mock;

import com.gigaspaces.datasource.DataSourceSQLQuery;

public class MockDataSourceSqlQuery
        implements DataSourceSQLQuery
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

    /* (non-Javadoc)
     * @see com.gigaspaces.datasource.DataSourceSQLQuery#getFromQuery()
     */
    @Override
    public String getFromQuery() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.gigaspaces.datasource.DataSourceSQLQuery#getSelectAllQuery()
     */
    @Override
    public String getSelectAllQuery() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.gigaspaces.datasource.DataSourceSQLQuery#getSelectCountQuery()
     */
    @Override
    public String getSelectCountQuery() {
        // TODO Auto-generated method stub
        return null;
    }
}
