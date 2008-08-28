package org.openspaces.itest.persistency.hibernate.simple;

import org.openspaces.persistency.hibernate.DefaultHibernateExternalDataSource;

import java.util.Properties;

/**
 * @author kimchy
 */
public class NonScrollableDefaultSimpleHibernateTests extends AbstractSimpleHibernateTests {

    protected void setUp() throws Exception {
        super.setUp();
        DefaultHibernateExternalDataSource dataSource = new DefaultHibernateExternalDataSource();
        dataSource.setUseScrollableResultSet(false);
        dataSource.setInitialLoadChunkSize(2);
        dataSource.setSessionFactory(sessionFactory);
        dataSource.init(new Properties());
        this.bulkDataPersister = dataSource;
        this.sqlDataProvider = dataSource;
    }
}