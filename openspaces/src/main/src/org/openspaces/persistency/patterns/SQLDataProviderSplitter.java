package org.openspaces.persistency.patterns;

import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.DataSourceException;
import com.gigaspaces.datasource.ManagedDataSource;
import com.gigaspaces.datasource.SQLDataProvider;
import com.j_spaces.core.client.SQLQuery;

/**
 * A sql data provider that redirects the sql basded operatinos to the given data source that
 * can handle the given type.
 *
 * @author kimchy
 */
public class SQLDataProviderSplitter extends BulkDataPersisterSplitter implements SQLDataProvider {

    public SQLDataProviderSplitter(ManagedDataSourceEntriesProvider[] dataSources) {
        super(dataSources);
        for (ManagedDataSource dataSource : dataSources) {
            if (!(dataSource instanceof SQLDataProvider)) {
                throw new IllegalArgumentException("data source [" + dataSource + "] must implement SQLDataProvider");
            }
        }
    }

    public DataIterator iterator(SQLQuery sqlQuery) throws DataSourceException {
        return ((SQLDataProvider) getDataSource(sqlQuery.getTypeName())).iterator(sqlQuery);
    }
}
