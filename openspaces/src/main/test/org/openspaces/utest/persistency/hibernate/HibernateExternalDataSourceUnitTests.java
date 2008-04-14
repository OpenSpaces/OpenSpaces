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
import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.ExternalDataSource;
import com.j_spaces.core.client.SQLQuery;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Tests the {@link org.openspaces.persistency.hibernate.HibernateExternalDataSource} class
 */
public class HibernateExternalDataSourceUnitTests extends AbstractDependencyInjectionSpringContextTests {

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
        template.execute("DELETE FROM PERSON");
        sessionFactory.evict(Person.class);
    }

    /**
     * Test the initial load functionality outside of the space
     */
    public void testInitialLoad() throws Exception{
        DataIterator<Person> dataIterator = externalDataSource.initialLoad();
        Set<Person> persons = new HashSet<Person>(numberOfEntriesToCreate);
        while (dataIterator.hasNext()) {
            Person person = dataIterator.next();
            persons.add(person);
        }
        dataIterator.close();
        assertEquals("Number of unique persons is wrong", numberOfEntriesToCreate, persons.size());
    }

    public void testCountWithId() throws Exception {
        Person template = new Person();
        template.setId(10);
        int count = externalDataSource.count(template);
        assertEquals("count provided wrong result", 1, count);
    }

    public void testCountWithSQLQuery() throws Exception {
        SQLQuery<Person> query = new SQLQuery<Person>(Person.class, "id > 990");
        int count = externalDataSource.count((SQLQuery)query);
        assertEquals("count provided wrong result", 9, count);
    }

    public void testExecuteBulk() throws Exception {
        final int numPersonsInDb = ((Number)hibernateTemplate.execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createQuery("select count(*) from Person").uniqueResult();
            }
        })).intValue();
        List<BulkItem> bulkItems = new LinkedList<BulkItem>();
        final String updatedUsername = "updated-username";
        final String newUserName = "new-user-name";
        bulkItems.add(new BulkItem() {
                public Object getItem() {
                    return new Person(10, updatedUsername, "updated first name", "updated last name");
                }
                public short getOperation() {
                    return BulkItem.UPDATE;
                }
            });
        bulkItems.add(new BulkItem() {
                public Object getItem() {
                    return new Person(11, updatedUsername, "updated first name", "updated last name");
                }
                public short getOperation() {
                    return BulkItem.UPDATE;
                }
            });
        bulkItems.add(new BulkItem() {
                public Object getItem() {
                    return new Person(12);
                }
                public short getOperation() {
                    return BulkItem.REMOVE;
                }
            });
        bulkItems.add(new BulkItem() {
                public Object getItem() {
                    return new Person(numPersonsInDb, newUserName, "new first name", "new last name");
                }
                public short getOperation() {
                    return BulkItem.WRITE;
                }
            });
        externalDataSource.executeBulk(bulkItems);

        //check that the db was updated properly
        int result = ((Number)hibernateTemplate.execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createQuery("select count(*) from Person where username=?")
                               .setParameter(0, updatedUsername).uniqueResult();
            }
        })).intValue();
        assertEquals("Updates using executeBulk failed", 2, result);

        //check write and update        
        result = ((Number)hibernateTemplate.execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createQuery("select count(*) from Person")
                               .uniqueResult();
            }
        })).intValue();
        //we removed an object and added one, so the number of persons in the db should remain the same
        assertEquals("Remove or write using executeBulk failed", numPersonsInDb, result);

        //check that the write was successful
        result = ((Number)hibernateTemplate.execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createQuery("select count(*) from Person where username=?")
                               .setParameter(0, newUserName)
                               .uniqueResult();
            }
        })).intValue();
        //we removed an object and added one, so the number of persons in the db should remain the same
        assertEquals("Write using executeBulk failed", 1, result);
    }

    public void testSqlQueryIterator() throws Exception{
        int numPersons = 200;
        SQLQuery<Person> personSQLQuery = new SQLQuery<Person>(Person.class, "id < ?");
        personSQLQuery.setParameter(1, numPersons);
        DataIterator<Person> dataIterator = externalDataSource.iterator(personSQLQuery);
        Set<Person> persons = new HashSet<Person>(numPersons);
        while (dataIterator.hasNext()) {
            Person person = dataIterator.next();
            persons.add(person);
        }
        dataIterator.close();
        assertEquals("Number of unique persons is wrong", numPersons, persons.size());
    }

    public void testTemplateIterator() throws Exception{
        DataIterator<Person> dataIterator = externalDataSource.iterator(new Person(null, null, null, "Last name"));
        Set<Person> persons = new HashSet<Person>(numberOfEntriesToCreate);
        while (dataIterator.hasNext()) {
            Person person = dataIterator.next();
            persons.add(person);
        }
        dataIterator.close();
        assertEquals("Number of unique persons is wrong", numberOfEntriesToCreate, persons.size());
    }

    public void testRead() throws Exception {
        Person person = (Person) externalDataSource.read(new Person(20));
        assertNotNull(person);
        assertEquals("Read first name is wrong", "user20 First", person.getFirstName());
    }

    public void testRemove() throws Exception {
        assertNotNull(hibernateTemplate.load(Person.class, 1));
        externalDataSource.remove(new Person(1));
        assertEquals("Remove operation didn't remove object", 0, hibernateTemplate.find("from Person where id=1").size());
    }

    public void testRemoveBatch() throws Exception {
        assertEquals(hibernateTemplate.find("from Person where id < 3").size(), 3);
        externalDataSource.removeBatch(Arrays.asList(new Person(0), new Person(1), new Person(2)));
        assertEquals("removeBatch failed", hibernateTemplate.find("from Person where id < 3").size(), 0);
    }

    public void testUpdate() throws Exception {
        String updateUsername = "updatedUsername";
        String updatedFirstName = "updatedFirstName";
        String updatePassword = "updatedLastName";
        externalDataSource.update(new Person(1, updateUsername, updatedFirstName, updatePassword));
        assertNotNull("update failed", hibernateTemplate.find("from Person where username='updatedUsername'"));
    }

    public void testUpdateBatch() throws Exception {
        assertEquals(numberOfEntriesToCreate, hibernateTemplate.find("from Person where lastName = 'Last name'").size());
        externalDataSource.updateBatch(Arrays.asList(new Person(0, "new username", "new first name", "new last name"),
                                                     new Person(1, "new username", "new first name", "new last name")));
        assertEquals("updateBatch failed", numberOfEntriesToCreate-2, hibernateTemplate.find("from Person where lastName = 'Last name'").size());
        assertEquals("updateBatch failed", 2, hibernateTemplate.find("from Person where username = 'new username'").size());
    }

    public void testWrite() throws Exception {
        assertEquals(numberOfEntriesToCreate, hibernateTemplate.find("from Person").size());
        externalDataSource.write(new Person(1000, "new username", "new first name", "new last name"));
        assertEquals(numberOfEntriesToCreate+1, hibernateTemplate.find("from Person").size());
    }


    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/utest/persistency/hibernate/hibernate-data-source-context.xml"};
        
    }
}
