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

import com.gigaspaces.datasource.DataIterator;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openspaces.persistency.hibernate.HibernateExternalDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.util.StopWatch;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Performance tests the {@link org.openspaces.persistency.hibernate.HibernateExternalDataSource} class.
 * Make sure you have an instance of h2 server named 'xdb' up and running. 
 */
public class HibernateExternalDataPerfomanceTester extends AbstractDependencyInjectionSpringContextTests {

    private SessionFactory sessionFactory;
    private HibernateTemplate hibernateTemplate;
    private HibernateExternalDataSource externalDataSource;
    private DataSource dataSource;
    private int numberOfEntries = 100000;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.hibernateTemplate = new HibernateTemplate(sessionFactory);
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setExternalDataSource(HibernateExternalDataSource externalDataSource) {
        this.externalDataSource = externalDataSource;
    }

    public void setNumberOfEntries(int numberOfEntries) {
        this.numberOfEntries = numberOfEntries;
    }



    @Override
    protected void onSetUp() throws Exception {
        hibernateTemplate.execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                //insert objects into the database
                for (int i=0; i< numberOfEntries; i++) {
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
        template.execute("DELETE FROM PERSON");
        sessionFactory.evict(Person.class);
        sessionFactory.close();
    }

    /**
     * Test the initial load functionality outside of the space
     */
    public void testInitialLoad() throws Exception{
        StopWatch stopWatch = new StopWatch("HibernateDataSource");
        stopWatch.start();
        DataIterator dataIterator = externalDataSource.initialLoad();
        int count = 0;
        while (dataIterator.hasNext()) {
            Person person = (Person) dataIterator.next();
            count++; 
            
        }
        dataIterator.close();
        stopWatch.stop();
        System.out.println(stopWatch.shortSummary());
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/utest/persistency/hibernate/hibernate-data-source-perf-context.xml"};
    }
}