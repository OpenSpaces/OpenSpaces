package org.openspaces.persistency.patterns;

import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.DataSourceException;
import com.gigaspaces.datasource.ManagedDataSource;
import com.gigaspaces.datasource.SQLDataProvider;
import com.j_spaces.core.client.SQLQuery;

/**
 * @author kimchy
 */
public class SQLDataProviderExceptionHandler extends BulkDataPersisterExceptionHandler implements SQLDataProvider {

    public SQLDataProviderExceptionHandler(ManagedDataSource dataSource, ExceptionHandler exceptionHandler) {
        super(dataSource, exceptionHandler);
        if (!(dataSource instanceof SQLDataProvider)) {
            throw new IllegalArgumentException("data source [" + dataSource + "] must implement SQLDataProvider");
        }
    }

    public DataIterator iterator(SQLQuery sqlQuery) throws DataSourceException {
        try {
            return ((SQLDataProvider) dataSource).iterator(sqlQuery);
        } catch (Exception e) {
            exceptionHandler.onException(e, sqlQuery);
            return null;
        }
    }
}
