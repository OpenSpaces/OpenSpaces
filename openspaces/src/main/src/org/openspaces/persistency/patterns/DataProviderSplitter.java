package org.openspaces.persistency.patterns;

import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.DataProvider;
import com.gigaspaces.datasource.DataSourceException;
import com.gigaspaces.datasource.ManagedDataSource;

/**
 * A data provider that redirects template based operations to the given data source that can
 * handle its type.
 *
 * @author kimchy
 */
public class DataProviderSplitter extends BulkDataPersisterSplitter implements DataProvider {

    public DataProviderSplitter(ManagedDataSourceEntriesProvider[] dataSources) {
        super(dataSources);
        for (ManagedDataSource dataSource : dataSources) {
            if (!(dataSource instanceof DataProvider)) {
                throw new IllegalArgumentException("data source [" + dataSource + "] must implement DataProvider");
            }
        }
    }

    public Object read(Object o) throws DataSourceException {
        return ((DataProvider) getDataSource(o.getClass().getName())).read(o);
    }

    public DataIterator iterator(Object o) throws DataSourceException {
        return ((DataProvider) getDataSource(o.getClass().getName())).iterator(o);
    }

    public int count(Object o) throws DataSourceException {
        return ((DataProvider) getDataSource(o.getClass().getName())).count(o);
    }
}