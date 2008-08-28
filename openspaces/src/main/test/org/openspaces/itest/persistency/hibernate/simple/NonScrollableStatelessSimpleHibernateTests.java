package org.openspaces.itest.persistency.hibernate.simple;

import org.openspaces.persistency.hibernate.StatelessHibernateExternalDataSource;

import java.util.Properties;

/**
 * @author kimchy
 */
public class NonScrollableStatelessSimpleHibernateTests extends AbstractSimpleHibernateTests {

    protected void setUp() throws Exception {
        super.setUp();
        StatelessHibernateExternalDataSource dataSource = new StatelessHibernateExternalDataSource();
        dataSource.setUseScrollableResultSet(false);
        dataSource.setInitialLoadChunkSize(2);
        dataSource.setSessionFactory(sessionFactory);
        dataSource.init(new Properties());
        this.bulkDataPersister = dataSource;
        this.sqlDataProvider = dataSource;
    }
}