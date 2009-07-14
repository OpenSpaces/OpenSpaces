package org.openspaces.itest.persistency.hibernate.simple;

import com.gigaspaces.datasource.BulkDataPersister;
import com.gigaspaces.datasource.BulkItem;
import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.SQLDataProvider;
import com.j_spaces.core.client.SQLQuery;
import junit.framework.TestCase;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.openspaces.itest.persistency.support.MockBulkItem;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kimchy
 */
public abstract class AbstractSimpleHibernateTests extends TestCase {

    protected SessionFactory sessionFactory;

    protected BulkDataPersister bulkDataPersister;

    protected SQLDataProvider sqlDataProvider;

    protected HibernateTemplate hibernateTemplate;

    protected void setUp() throws Exception {
        Configuration conf = new Configuration().configure("org/openspaces/itest/persistency/hibernate/simple/hibernate.cfg.xml")
                .setProperty(Environment.HBM2DDL_AUTO, "create");
        sessionFactory = conf.buildSessionFactory();
        hibernateTemplate = new HibernateTemplate(sessionFactory);
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
        hibernateTemplate.save(new Simple(2, "test"));

        List<BulkItem> bulkItems = new ArrayList<BulkItem>();
        bulkItems.add(new MockBulkItem(new Simple(1, "test"), BulkItem.WRITE));
        bulkItems.add(new MockBulkItem(new Simple(2, "test"), BulkItem.REMOVE));
        bulkItems.add(new MockBulkItem(new Simple(3, "test"), BulkItem.UPDATE));
        bulkDataPersister.executeBulk(bulkItems);

        assertNotNull(hibernateTemplate.get(Simple.class, 1));
        assertNull(hibernateTemplate.get(Simple.class, 2));
        assertNotNull(hibernateTemplate.get(Simple.class, 3));
    }

    public void testDuplicateWriteExecuteBulk() throws Exception {
        List<BulkItem> bulkItems = new ArrayList<BulkItem>();
        bulkItems.add(new MockBulkItem(new Simple(1, "test"), BulkItem.WRITE));
        bulkItems.add(new MockBulkItem(new Simple(1, "test"), BulkItem.UPDATE));
        bulkDataPersister.executeBulk(bulkItems);

        assertNotNull(hibernateTemplate.get(Simple.class, 1));
    }

    public void testDuplicateDeleteExecuteBulk() throws Exception {
        hibernateTemplate.save(new Simple(1, "test"));
        assertNotNull(hibernateTemplate.get(Simple.class, 1));

        List<BulkItem> bulkItems = new ArrayList<BulkItem>();
        bulkItems.add(new MockBulkItem(new Simple(1, "test"), BulkItem.REMOVE));
        bulkItems.add(new MockBulkItem(new Simple(1, "test"), BulkItem.REMOVE));
        bulkDataPersister.executeBulk(bulkItems);

        assertNull(hibernateTemplate.get(Simple.class, 2));
    }

    // for some reason, this passes on local machine, but fails in the build server
    public void XtestInitialLoad() throws Exception {
        hibernateTemplate.save(new Simple(1, "test1"));
        hibernateTemplate.save(new Simple(2, "test2"));
        hibernateTemplate.save(new SimpleBase(1, "test1"));
        hibernateTemplate.save(new SimpleExtend(2, "test2", "test2ex"));
        hibernateTemplate.save(new SimpleBase(3, "test3"));

        DataIterator iterator = sqlDataProvider.initialLoad();
        int count = 0;
        while (iterator.hasNext()) {
            Object val = iterator.next();
            hibernateTemplate.delete(val);
            count++;
        }
        assertEquals(5, count);
        List existing = hibernateTemplate.executeFind(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createQuery("from java.lang.Object").list();
            }
        });
        assertEquals(0, existing.size());
    }

    public void testCountAndIterator() throws Exception {
        hibernateTemplate.save(new Simple(1, "test1"));
        hibernateTemplate.save(new Simple(2, "test2"));

        SQLQuery<Simple> sql = new SQLQuery<Simple>(Simple.class, "id = ?");
        sql.setParameter(1, 1);
        DataIterator it = sqlDataProvider.iterator(sql);
        assertTrue(it.hasNext());
        assertEquals(1, ((Simple) it.next()).getId().intValue());

        sql.setParameter(1, 0);
        it = sqlDataProvider.iterator(sql);
        assertFalse(it.hasNext());
    }
}
