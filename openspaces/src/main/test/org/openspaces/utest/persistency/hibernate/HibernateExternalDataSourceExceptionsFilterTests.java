/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openspaces.utest.persistency.hibernate;

import com.gigaspaces.datasource.BulkItem;
import com.gigaspaces.datasource.DataSourceException;
import com.gigaspaces.datasource.ExternalDataSource;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openspaces.persistency.BulkDataPersisterExceptionFilter;
import org.openspaces.persistency.hibernate.HibernateExternalDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Tests the {@link org.openspaces.persistency.hibernate.HibernateExternalDataSource} class
 */
public class HibernateExternalDataSourceExceptionsFilterTests extends AbstractDependencyInjectionSpringContextTests {

    private SessionFactory sessionFactory;
    private HibernateTemplate hibernateTemplate;
    private ExternalDataSource externalDataSource;
    private DataSource dataSource;
    private int numberOfEntriesToCreate = 1000;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.hibernateTemplate = new HibernateTemplate(sessionFactory);
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setExternalDataSource(ExternalDataSource externalDataSource) {
        this.externalDataSource = externalDataSource;
    }

    public void setNumberOfEntriesToCreate(int numberOfEntriesToCreate) {
        this.numberOfEntriesToCreate = numberOfEntriesToCreate;
    }



    @Override
    protected void onSetUp() throws Exception {
        hibernateTemplate.execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                //insert objects into the database
                for (int i=0; i< numberOfEntriesToCreate; i++) {
                    String username = "user" + i;
                    Person person = new Person(i, username, username+" First", "Last name");
                    session.save(person);
                }
                return null;
            }
        });
    }

    @Override
    protected void onTearDown() throws Exception {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        template.execute("SHUTDOWN");
        sessionFactory.evict(Person.class);
    }


    public void testExceptionFilter() throws Exception {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        template.execute("drop table PERSON");
        List<BulkItem> bulkItems = new LinkedList<BulkItem>();
        bulkItems.add(new BulkItem() {
                public Object getItem() {
                    return new Person(1);
                }
                public short getOperation() {
                    return BulkItem.WRITE;
                }
            });
        try {
            externalDataSource.executeBulk(bulkItems);
            fail("No exception thrown");
        } catch (DataSourceException e) {

        }
        ((HibernateExternalDataSource)externalDataSource).setExceptionFilter(new BulkDataPersisterExceptionFilter() {
            public boolean shouldFilter(Exception exception) {
                return true;
            }
        });
        try {
            externalDataSource.executeBulk(bulkItems);
        } catch (DataSourceException e) {
            fail("exception thrown");
        }

    }


    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/utest/persistency/hibernate/hibernate-data-source-context.xml"};

    }
}