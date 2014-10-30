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
package org.openspaces.itest.persistency.hibernate.simple;

import com.gigaspaces.datasource.BulkDataPersister;
import com.gigaspaces.datasource.BulkItem;
import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.SQLDataProvider;
import com.j_spaces.core.client.SQLQuery;
import junit.framework.TestCase;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.openspaces.itest.persistency.support.MockBulkItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kimchy
 */
public abstract class AbstractSimpleHibernateTests extends TestCase {

    protected SessionFactory sessionFactory;

    protected BulkDataPersister bulkDataPersister;

    protected SQLDataProvider sqlDataProvider;

    protected Session session;

    protected void setUp() throws Exception {
        Configuration conf = new Configuration().configure("org/openspaces/itest/persistency/hibernate/simple/hibernate.cfg.xml")
                .setProperty(Environment.HBM2DDL_AUTO, "create");
        sessionFactory = conf.buildSessionFactory();
        session = sessionFactory.openSession();
        deleteContent();

    }

    protected void tearDown() throws Exception {
        deleteContent();
        sqlDataProvider.shutdown();
    }

    private void deleteContent() {
        Session session = sessionFactory.openSession();
        Transaction tr = session.beginTransaction();
        Query query = session.createQuery("delete from Simple");
        query.executeUpdate();
        query = session.createQuery("delete from SimpleBase");
        query.executeUpdate();
        tr.commit();
        session.close();
    }

    public void testSimpleExecuteBulk() throws Exception {
        Transaction tx = session.beginTransaction();
        session.save(new Simple(2, "test"));
        tx.commit();
        session.close();

        session.flush();
        session.close();

        List<BulkItem> bulkItems = new ArrayList<BulkItem>();
        bulkItems.add(new MockBulkItem(new Simple(1, "test"), BulkItem.WRITE));
        bulkItems.add(new MockBulkItem(new Simple(2, "test"), BulkItem.REMOVE));
        bulkItems.add(new MockBulkItem(new Simple(3, "test"), BulkItem.UPDATE));
        bulkDataPersister.executeBulk(bulkItems);

        session = sessionFactory.openSession();
        assertNotNull(session.get(Simple.class, 1));
        assertNull(session.get(Simple.class, 2));
        assertNotNull(session.get(Simple.class, 3));
        session.close();

    }

    public void XtestDuplicateWriteExecuteBulk() throws Exception {
        List<BulkItem> bulkItems = new ArrayList<BulkItem>();
        bulkItems.add(new MockBulkItem(new Simple(1, "test"), BulkItem.WRITE));
        bulkItems.add(new MockBulkItem(new Simple(1, "test"), BulkItem.UPDATE));
        bulkDataPersister.executeBulk(bulkItems);

        assertNotNull(session.get(Simple.class, 1));
    }

    public void XtestDuplicateDeleteExecuteBulk() throws Exception {
        session.save(new Simple(1, "test"));
        assertNotNull(session.get(Simple.class, 1));

        List<BulkItem> bulkItems = new ArrayList<BulkItem>();
        bulkItems.add(new MockBulkItem(new Simple(1, "test"), BulkItem.REMOVE));
        bulkItems.add(new MockBulkItem(new Simple(1, "test"), BulkItem.REMOVE));
        bulkDataPersister.executeBulk(bulkItems);

        assertNull(session.get(Simple.class, 2));
    }

    // for some reason, this passes on local machine, but fails in the build server
    public void XtestInitialLoad() throws Exception {
        session.save(new Simple(1, "test1"));
        session.save(new Simple(2, "test2"));
        session.save(new SimpleBase(1, "test1"));
        session.save(new SimpleExtend(2, "test2", "test2ex"));
        session.save(new SimpleBase(3, "test3"));

        DataIterator iterator = sqlDataProvider.initialLoad();
        int count = 0;
        while (iterator.hasNext()) {
            Object val = iterator.next();
            session.delete(val);
            count++;
        }
        assertEquals(5, count);
        List existing = session.createQuery("from java.lang.Object").list();
        assertEquals(0, existing.size());
    }

    public void XtestCountAndIterator() throws Exception {
        Transaction tx = session.beginTransaction();
        session.save(new Simple(1, "test1"));
        session.save(new Simple(2, "test2"));
        tx.commit();

        session.flush();
        session.close();
        SQLQuery<Simple> sql = new SQLQuery<Simple>(Simple.class, "id = ?");
        sql.setParameter(1, 1);
        DataIterator it = sqlDataProvider.iterator(sql);
        assertTrue(it.hasNext());
        assertEquals(1, ((Simple) it.next()).getId().intValue());

        it.close();
        sql.setParameter(1, 0);
        it = sqlDataProvider.iterator(sql);
        assertFalse(it.hasNext());
        it.close();
    }
}
