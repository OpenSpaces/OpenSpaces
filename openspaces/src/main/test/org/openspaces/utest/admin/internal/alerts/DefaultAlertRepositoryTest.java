package org.openspaces.utest.admin.internal.alerts;

import org.openspaces.admin.alerts.Alert;
import org.openspaces.admin.alerts.AlertFactory;
import org.openspaces.admin.alerts.AlertSeverity;
import org.openspaces.admin.alerts.config.AlertBeanConfig;
import org.openspaces.admin.internal.alerts.AlertHistory;
import org.openspaces.admin.internal.alerts.DefaultAlertRepository;

import junit.framework.TestCase;



/**
 * Unit Tests the {@link DefaultAlertRepository}
 * @author Moran Avigdor
 */
public class DefaultAlertRepositoryTest extends TestCase {

    final DefaultAlertRepository repository = new DefaultAlertRepository();

    /** adding an {@link AlertSeverity#OK} alert with a non-existing group UID should not open an entry in the repository */
    public void test1() {
        Alert alert = new AlertFactory()
        .severity(AlertSeverity.OK)
        .groupUid("group1")
        .beanConfigClass(AlertBeanConfig.class)
        .toAlert();

        assertFalse(repository.addAlert(alert));
        assertNull(repository.getAlertByUid(alert.getAlertUid()));
        AlertHistory alertHistoryByGroupUid = repository.getAlertHistoryByGroupUid(alert.getGroupUid());
        assertNotNull(alertHistoryByGroupUid);
        assertNull(alertHistoryByGroupUid.getDetails());
        assertNotNull(alertHistoryByGroupUid.getAlerts());
        assertEquals(0, alertHistoryByGroupUid.getAlerts().length);
    }
    
    /** adding an {@link AlertSeverity#NA} alert with a non-existing group UID should not open an entry in the repository */
    public void test2() {
        Alert alert = new AlertFactory()
        .severity(AlertSeverity.NA)
        .groupUid("group2")
        .beanConfigClass(AlertBeanConfig.class)
        .toAlert();

        assertFalse(repository.addAlert(alert));
        assertNull(repository.getAlertByUid(alert.getAlertUid()));
        AlertHistory alertHistoryByGroupUid = repository.getAlertHistoryByGroupUid(alert.getGroupUid());
        assertNotNull(alertHistoryByGroupUid);
        assertNull(alertHistoryByGroupUid.getDetails());
        assertNotNull(alertHistoryByGroupUid.getAlerts());
        assertEquals(0, alertHistoryByGroupUid.getAlerts().length);
    }
    
    /** 
     * adding an {@link AlertSeverity#WARNING} alert should open an entry in the repository.
     * adding an {@link AlertSeverity#OK} alert should resolve the alert group. 
     */
    public void test3() {
        
        for (int i=0; i<3; ++i) {
            Alert alert = new AlertFactory()
            .severity(AlertSeverity.WARNING)
            .groupUid("group3")
            .beanConfigClass(AlertBeanConfig.class)
            .description("alert#"+i)
            .toAlert();

            assertTrue(repository.addAlert(alert));
            assertNotNull(repository.getAlertByUid(alert.getAlertUid()));
            AlertHistory alertHistoryByGroupUid = repository.getAlertHistoryByGroupUid(alert.getGroupUid());
            assertNotNull(alertHistoryByGroupUid);
            assertNotNull(alertHistoryByGroupUid.getDetails());
            assertFalse(alertHistoryByGroupUid.getDetails().isResolved());
            assertNotNull(alertHistoryByGroupUid.getAlerts());
            assertEquals(i+1, alertHistoryByGroupUid.getAlerts().length);
        }

        Alert alert = new AlertFactory()
        .severity(AlertSeverity.OK)
        .groupUid("group3")
        .beanConfigClass(AlertBeanConfig.class)
        .description("alert#4")
        .toAlert();

        assertTrue(repository.addAlert(alert));
        assertNotNull(repository.getAlertByUid(alert.getAlertUid()));
        AlertHistory alertHistoryByGroupUid = repository.getAlertHistoryByGroupUid(alert.getGroupUid());
        assertNotNull(alertHistoryByGroupUid);
        assertNotNull(alertHistoryByGroupUid.getDetails());
        assertTrue(alertHistoryByGroupUid.getDetails().isResolved());
        assertNotNull(alertHistoryByGroupUid.getAlerts());
        assertEquals(4, alertHistoryByGroupUid.getAlerts().length);
    }
    
    /** 
     * adding an {@link AlertSeverity#WARNING} alert should open an entry in the repository.
     * adding an {@link AlertSeverity#NA} alert should 'close' the alert group.
     */
    public void test4() {
        
        for (int i=0; i<3; ++i) {
            Alert alert = new AlertFactory()
            .severity(AlertSeverity.WARNING)
            .groupUid("group4")
            .beanConfigClass(AlertBeanConfig.class)
            .description("alert#"+i)
            .toAlert();

            assertTrue(repository.addAlert(alert));
            assertNotNull(repository.getAlertByUid(alert.getAlertUid()));
            AlertHistory alertHistoryByGroupUid = repository.getAlertHistoryByGroupUid(alert.getGroupUid());
            assertNotNull(alertHistoryByGroupUid);
            assertNotNull(alertHistoryByGroupUid.getDetails());
            assertFalse(alertHistoryByGroupUid.getDetails().isResolved());
            assertNotNull(alertHistoryByGroupUid.getAlerts());
            assertEquals(i+1, alertHistoryByGroupUid.getAlerts().length);
        }

        Alert alert = new AlertFactory()
        .severity(AlertSeverity.NA)
        .groupUid("group4")
        .beanConfigClass(AlertBeanConfig.class)
        .description("alert#4")
        .toAlert();

        assertTrue(repository.addAlert(alert));
        assertNotNull(repository.getAlertByUid(alert.getAlertUid()));
        AlertHistory alertHistoryByGroupUid = repository.getAlertHistoryByGroupUid(alert.getGroupUid());
        assertNotNull(alertHistoryByGroupUid);
        assertNotNull(alertHistoryByGroupUid.getDetails());
        assertFalse(alertHistoryByGroupUid.getDetails().isResolved());
        assertNotNull(alertHistoryByGroupUid.getAlerts());
        assertEquals(4, alertHistoryByGroupUid.getAlerts().length);
    }
    
    /** 
     * adding an {@link AlertSeverity#WARNING} alert should open an entry in the repository.
     * adding an {@link AlertSeverity#OK} alert should resolve the alert group.
     * adding another {@link AlertSeverity#OK} alert should not affect the resolved alert group.
     */
    public void test5() {
        
        for (int i=0; i<3; ++i) {
            Alert alert = new AlertFactory()
            .severity(AlertSeverity.WARNING)
            .groupUid("group5")
            .beanConfigClass(AlertBeanConfig.class)
            .description("alert#"+i)
            .toAlert();

            assertTrue(repository.addAlert(alert));
        }

        for (int i=0; i<3; ++i) {
            Alert alert = new AlertFactory()
            .severity(AlertSeverity.OK)
            .groupUid("group5")
            .beanConfigClass(AlertBeanConfig.class)
            .description("alert#4")
            .toAlert();

            if (i==0) {
                assertTrue(repository.addAlert(alert));
                assertNotNull(repository.getAlertByUid(alert.getAlertUid()));
                AlertHistory alertHistoryByGroupUid = repository.getAlertHistoryByGroupUid(alert.getGroupUid());
                assertNotNull(alertHistoryByGroupUid);
                assertNotNull(alertHistoryByGroupUid.getDetails());
                assertTrue(alertHistoryByGroupUid.getDetails().isResolved());
                assertNotNull(alertHistoryByGroupUid.getAlerts());
                assertEquals(4, alertHistoryByGroupUid.getAlerts().length);
            }
            else {
                assertFalse(repository.addAlert(alert));
                assertNull(repository.getAlertByUid(alert.getAlertUid()));
                AlertHistory alertHistoryByGroupUid = repository.getAlertHistoryByGroupUid(alert.getGroupUid());
                assertNotNull(alertHistoryByGroupUid);
                assertNotNull(alertHistoryByGroupUid.getDetails());
                assertTrue(alertHistoryByGroupUid.getDetails().isResolved());
                assertNotNull(alertHistoryByGroupUid.getAlerts());
                assertEquals(4, alertHistoryByGroupUid.getAlerts().length);
            }
        }
    }
    
    /** resolved history size */
    public void test6() {

        repository.setResolvedAlertHistorySize(5);
        
        for (int i=0; i<10; ++i) {
            Alert alert = new AlertFactory()
            .severity(AlertSeverity.WARNING)
            .groupUid("group"+i)
            .beanConfigClass(AlertBeanConfig.class)
            .description("alert#"+i)
            .toAlert();

            assertTrue(repository.addAlert(alert));
        }
        
        assertEquals(10, repository.getAlertHistory().length);
        
        for (int i=0; i<10; ++i) {
            Alert alert = new AlertFactory()
            .severity(AlertSeverity.OK)
            .groupUid("group"+i)
            .beanConfigClass(AlertBeanConfig.class)
            .description("alert#"+i)
            .timestamp(i) //timestamp of last resolved should be kept in repository if exceeds history size
            .toAlert();

            assertTrue(repository.addAlert(alert));
        }
        
        //there should only be as much as declared history size
        assertEquals(5, repository.getAlertHistory().length);
        
        //check that last resolved alerts are kept; FIFO order - first alert to be resolved is removed.
        for (int i=5; i<10; ++i) {
            AlertHistory alertHistoryByGroupUid = repository.getAlertHistoryByGroupUid("group"+i);
            assertNotNull(alertHistoryByGroupUid);
            assertNotNull(alertHistoryByGroupUid.getDetails());
            assertTrue(alertHistoryByGroupUid.getDetails().isResolved());
            assertNotNull(alertHistoryByGroupUid.getAlerts());
            assertEquals(2, alertHistoryByGroupUid.getAlerts().length);
        }
    }
    
    /** group history size */
    public void test7() {
        repository.setGroupAlertHistorySize(5);
        
        for (int i=0; i<10; ++i) {
            Alert alert = new AlertFactory()
            .severity(AlertSeverity.WARNING)
            .groupUid("group7")
            .beanConfigClass(AlertBeanConfig.class)
            .description("alert#"+i)
            .toAlert();

            assertTrue(repository.addAlert(alert));
        }
        
        AlertHistory alertHistoryByGroupUid = repository.getAlertHistoryByGroupUid("group7");
        assertNotNull(alertHistoryByGroupUid);
        assertNotNull(alertHistoryByGroupUid.getDetails());
        assertFalse(alertHistoryByGroupUid.getDetails().isResolved());
        assertNotNull(alertHistoryByGroupUid.getAlerts());
        assertEquals(5+1, alertHistoryByGroupUid.getAlerts().length); //1 - is the first alert triggered, 5 - the rest of the history
        
        
        Alert alert = new AlertFactory()
        .severity(AlertSeverity.OK)
        .groupUid("group7")
        .beanConfigClass(AlertBeanConfig.class)
        .description("alert#11")
        .toAlert();

        assertTrue(repository.addAlert(alert));
        AlertHistory alertHistoryByGroupUid7 = repository.getAlertHistoryByGroupUid("group7");
        assertNotNull(alertHistoryByGroupUid7);
        assertNotNull(alertHistoryByGroupUid7.getDetails());
        assertTrue(alertHistoryByGroupUid7.getDetails().isResolved());
        assertNotNull(alertHistoryByGroupUid7.getAlerts());
        assertEquals(5+2, alertHistoryByGroupUid7.getAlerts().length); //2 - is the first&last alert triggered, 5 - the rest of the history
    }
}
