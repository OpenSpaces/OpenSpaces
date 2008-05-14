package org.openspaces.itest.persistency.hibernate.simple;

import org.openspaces.persistency.hibernate.StatelessHibernateExternalDataSource;

import java.util.Properties;

/**
 * @author kimchy
 */
public class StatelessSimpleLowFetchSizeHibernateTests extends AbstractSimpleHibernateTests {

    protected void setUp() throws Exception {
        super.setUp();
        StatelessHibernateExternalDataSource dataSource = new StatelessHibernateExternalDataSource();
        dataSource.setInitalLoadChunkSize(2);
        dataSource.setFetchSize(1);
        dataSource.setSessionFactory(sessionFactory);
        dataSource.init(new Properties());
        this.bulkDataPersister = dataSource;
        this.sqlDataProvider = dataSource;
    }
}