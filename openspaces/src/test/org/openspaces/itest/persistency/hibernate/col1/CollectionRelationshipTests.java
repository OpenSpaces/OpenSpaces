/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.itest.persistency.hibernate.col1;

import com.gigaspaces.datasource.BulkDataPersister;
import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.SQLDataProvider;
import junit.framework.TestCase;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.openspaces.persistency.hibernate.DefaultHibernateExternalDataSource;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author kimchy
 */
public class CollectionRelationshipTests extends TestCase {

    protected SessionFactory sessionFactory;

    protected BulkDataPersister bulkDataPersister;

    protected SQLDataProvider sqlDataProvider;

    protected HibernateTemplate hibernateTemplate;

    protected void setUp() throws Exception {
        Configuration conf = new Configuration().configure("org/openspaces/itest/persistency/hibernate/col1/hibernate.cfg.xml")
                .setProperty(Environment.HBM2DDL_AUTO, "create");
        sessionFactory = conf.buildSessionFactory();
        hibernateTemplate = new HibernateTemplate(sessionFactory);
        deleteContent();
        DefaultHibernateExternalDataSource dataSource = new DefaultHibernateExternalDataSource();
//        dataSource.setInitialLoadChunkSize(-1);
//        dataSource.setPerformOrderById(false);
        dataSource.setSessionFactory(sessionFactory);
        dataSource.init(new Properties());
        this.bulkDataPersister = dataSource;
        this.sqlDataProvider = dataSource;
    }

    protected void tearDown() throws Exception {
        deleteContent();
        sqlDataProvider.shutdown();
    }

    public void testSimpleLoad() throws Exception {
        insertTestData();
        DataIterator iterator = sqlDataProvider.initialLoad();
        int count = 0;
        while (iterator.hasNext()) {
            Object val = iterator.next();
            count++;
            if (val instanceof Parent) {
                Parent parent = (Parent) val;
                assertEquals(3, parent.getChildren().size());
            }
        }
        assertEquals(8, count);
    }

    private void deleteContent() {
        Session s = sessionFactory.openSession();
        Transaction t = s.beginTransaction();
        List list = s.createQuery("from Parent").list();
        for (Iterator i = list.iterator(); i.hasNext();) {
            s.delete( i.next());
        }
        t.commit();
        s.close();
    }

    private void insertTestData() {
        Session s = sessionFactory.openSession();
        Transaction t = s.beginTransaction();
        s.save(makeParent("parent1", "child1-1", "child1-2", "child1-3"));
        s.save(makeParent("parent2", "child2-1", "child2-2", "child2-3"));
        t.commit();
        s.close();
    }

    protected Object makeParent(String name, String child1, String child2, String child3) {
        Parent parent = new Parent(name);
        parent.addChild(new Child(child1));
        parent.addChild(new Child(child2));
        parent.addChild(new Child(child3));
        return parent;
    }
}
