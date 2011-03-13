package org.openspaces.utest.admin.internal.alerts;

import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.internal.alert.AlertHistory;
import org.openspaces.admin.internal.alert.DefaultAlertRepository;

import junit.framework.TestCase;



/**
 * Unit Tests the {@link DefaultAlertRepository}
 * @author Moran Avigdor
 */
public class DefaultAlertRepositoryTest extends TestCase {

    final DefaultAlertRepository repository = new DefaultAlertRepository();

    /** adding an {@link AlertStatus#RESOLVED} alert with a non-existing group UID should not open an entry in the repository */
    public void test1() {
        Alert alert = new AlertFactory()
        .severity(AlertSeverity.WARNING)
        .status(AlertStatus.RESOLVED)
        .groupUid("group1")
        .toAlert();

        assertFalse(repository.addAlert(alert));
        assertNull(repository.getAlertByUid(alert.getAlertUid()));
        AlertHistory alertHistoryByGroupUid = repository.getAlertHistoryByGroupUid(alert.getGroupUid());
        assertNotNull(alertHistoryByGroupUid);
        assertNull(alertHistoryByGroupUid.getDetails());
        assertNotNull(alertHistoryByGroupUid.getAlerts());
        assertEquals(0, alertHistoryByGroupUid.getAlerts().length);
    }
    
    /** adding an {@link AlertStatus#NA} alert with a non-existing group UID should not open an entry in the repository */
    public void test2() {
        Alert alert = new AlertFactory()
        .severity(AlertSeverity.WARNING)
        .status(AlertStatus.NA)
        .groupUid("group2")
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
     * adding an {@link AlertStatus#RAISED} alert should open an entry in the repository.
     * adding an {@link AlertStatus#RESOLVED} alert should resolve the alert group. 
     */
    public void test3() {
        
        for (int i=0; i<3; ++i) {
            Alert alert = new AlertFactory()
            .severity(AlertSeverity.WARNING)
            .status(AlertStatus.RAISED)
            .groupUid("group3")
            .description("alert#"+i)
            .toAlert();

            assertTrue(repository.addAlert(alert));
            assertNotNull(repository.getAlertByUid(alert.getAlertUid()));
            AlertHistory alertHistoryByGroupUid = repository.getAlertHistoryByGroupUid(alert.getGroupUid());
            assertNotNull(alertHistoryByGroupUid);
            assertNotNull(alertHistoryByGroupUid.getDetails());
            assertFalse(alertHistoryByGroupUid.getDetails().getLastAlertStatus().isResolved());
            assertNotNull(alertHistoryByGroupUid.getAlerts());
            assertEquals(i+1, alertHistoryByGroupUid.getAlerts().length);
        }

        Alert alert = new AlertFactory()
        .severity(AlertSeverity.WARNING)
        .status(AlertStatus.RESOLVED)
        .groupUid("group3")
        .description("alert#4")
        .toAlert();

        assertTrue(repository.addAlert(alert));
        assertNotNull(repository.getAlertByUid(alert.getAlertUid()));
        AlertHistory alertHistoryByGroupUid = repository.getAlertHistoryByGroupUid(alert.getGroupUid());
        assertNotNull(alertHistoryByGroupUid);
        assertNotNull(alertHistoryByGroupUid.getDetails());
        assertTrue(alertHistoryByGroupUid.getDetails().getLastAlertStatus().isResolved());
        assertNotNull(alertHistoryByGroupUid.getAlerts());
        assertEquals(4, alertHistoryByGroupUid.getAlerts().length);
    }
    
    /** 
     * adding an {@link AlertStatus#RAISED} alert should open an entry in the repository.
     * adding an {@link AlertStatus#NA} alert should 'close' the alert group.
     */
    public void test4() {
        
        for (int i=0; i<3; ++i) {
            Alert alert = new AlertFactory()
            .severity(AlertSeverity.WARNING)
            .status(AlertStatus.RAISED)
            .groupUid("group4")
            .description("alert#"+i)
            .toAlert();

            assertTrue(repository.addAlert(alert));
            assertNotNull(repository.getAlertByUid(alert.getAlertUid()));
            AlertHistory alertHistoryByGroupUid = repository.getAlertHistoryByGroupUid(alert.getGroupUid());
            assertNotNull(alertHistoryByGroupUid);
            assertNotNull(alertHistoryByGroupUid.getDetails());
            assertFalse(alertHistoryByGroupUid.getDetails().getLastAlertStatus().isResolved());
            assertNotNull(alertHistoryByGroupUid.getAlerts());
            assertEquals(i+1, alertHistoryByGroupUid.getAlerts().length);
        }

        Alert alert = new AlertFactory()
        .severity(AlertSeverity.WARNING)
        .status(AlertStatus.NA)
        .groupUid("group4")
        .description("alert#4")
        .toAlert();

        assertTrue(repository.addAlert(alert));
        assertNotNull(repository.getAlertByUid(alert.getAlertUid()));
        AlertHistory alertHistoryByGroupUid = repository.getAlertHistoryByGroupUid(alert.getGroupUid());
        assertNotNull(alertHistoryByGroupUid);
        assertNotNull(alertHistoryByGroupUid.getDetails());
        assertFalse(alertHistoryByGroupUid.getDetails().getLastAlertStatus().isResolved());
        assertNotNull(alertHistoryByGroupUid.getAlerts());
        assertEquals(4, alertHistoryByGroupUid.getAlerts().length);
    }
    
    /** 
     * adding an {@link AlertStatus#RAISED} alert should open an entry in the repository.
     * adding an {@link AlertStatus#RESOLVED} alert should resolve the alert group.
     * adding another {@link AlertStatus#RESOLVED} alert should not affect the resolved alert group.
     */
    public void test5() {
        
        for (int i=0; i<3; ++i) {
            Alert alert = new AlertFactory()
            .severity(AlertSeverity.WARNING)
            .status(AlertStatus.RAISED)
            .groupUid("group5")
            .description("alert#"+i)
            .toAlert();

            assertTrue(repository.addAlert(alert));
        }

        for (int i=0; i<3; ++i) {
            Alert alert = new AlertFactory()
            .severity(AlertSeverity.WARNING)
            .status(AlertStatus.RESOLVED)
            .groupUid("group5")
            .description("alert#4")
            .toAlert();

            if (i==0) {
                assertTrue(repository.addAlert(alert));
                assertNotNull(repository.getAlertByUid(alert.getAlertUid()));
                AlertHistory alertHistoryByGroupUid = repository.getAlertHistoryByGroupUid(alert.getGroupUid());
                assertNotNull(alertHistoryByGroupUid);
                assertNotNull(alertHistoryByGroupUid.getDetails());
                assertTrue(alertHistoryByGroupUid.getDetails().getLastAlertStatus().isResolved());
                assertNotNull(alertHistoryByGroupUid.getAlerts());
                assertEquals(4, alertHistoryByGroupUid.getAlerts().length);
            }
            else {
                assertFalse(repository.addAlert(alert));
                assertNull(repository.getAlertByUid(alert.getAlertUid()));
                AlertHistory alertHistoryByGroupUid = repository.getAlertHistoryByGroupUid(alert.getGroupUid());
                assertNotNull(alertHistoryByGroupUid);
                assertNotNull(alertHistoryByGroupUid.getDetails());
                assertTrue(alertHistoryByGroupUid.getDetails().getLastAlertStatus().isResolved());
                assertNotNull(alertHistoryByGroupUid.getAlerts());
                assertEquals(4, alertHistoryByGroupUid.getAlerts().length);
            }
        }
    }
    
    /** 
     * adding an {@link AlertStatus#RAISED} alert should open an entry in the repository.
     * adding an {@link AlertStatus#RESOLVED} alert should resolve the alert group.
     * repeat, should always add the new raised alert belonging to the same group.
     */
    public void test55() {
        
        for (int repeat=0; repeat<2; ++repeat) {
            for (int i=0; i<3; ++i) {
                Alert alert = new AlertFactory()
                .severity(AlertSeverity.WARNING)
                .status(AlertStatus.RAISED)
                .groupUid("group55")
                .description("alert#"+i)
                .toAlert();

                assertTrue(repository.addAlert(alert));
            }

            Alert alert = new AlertFactory()
            .severity(AlertSeverity.WARNING)
            .status(AlertStatus.RESOLVED)
            .groupUid("group55")
            .description("alert#4")
            .toAlert();

            assertTrue(repository.addAlert(alert));
            assertNotNull(repository.getAlertByUid(alert.getAlertUid()));
            AlertHistory alertHistoryByGroupUid = repository.getAlertHistoryByGroupUid(alert.getGroupUid());
            assertNotNull(alertHistoryByGroupUid);
            assertNotNull(alertHistoryByGroupUid.getDetails());
            assertTrue(alertHistoryByGroupUid.getDetails().getLastAlertStatus().isResolved());
            assertNotNull(alertHistoryByGroupUid.getAlerts());
            assertEquals(4, alertHistoryByGroupUid.getAlerts().length);
        }
        
    }
    
    /** resolved history size */
    public void test6() {

        repository.setResolvedAlertHistorySize(5);
        
        for (int i=0; i<10; ++i) {
            Alert alert = new AlertFactory()
            .severity(AlertSeverity.WARNING)
            .status(AlertStatus.RAISED)
            .groupUid("group"+i)
            .description("alert#"+i)
            .toAlert();

            assertTrue(repository.addAlert(alert));
        }
        
        assertEquals(10, repository.getAlertHistory().length);
        
        for (int i=0; i<10; ++i) {
            Alert alert = new AlertFactory()
            .severity(AlertSeverity.WARNING)
            .status(AlertStatus.RESOLVED)
            .groupUid("group"+i)
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
            assertTrue(alertHistoryByGroupUid.getDetails().getLastAlertStatus().isResolved());
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
            .status(AlertStatus.RAISED)
            .groupUid("group7")
            .description("alert#"+i)
            .toAlert();

            assertTrue(repository.addAlert(alert));
        }
        
        AlertHistory alertHistoryByGroupUid = repository.getAlertHistoryByGroupUid("group7");
        assertNotNull(alertHistoryByGroupUid);
        assertNotNull(alertHistoryByGroupUid.getDetails());
        assertFalse(alertHistoryByGroupUid.getDetails().getLastAlertStatus().isResolved());
        assertNotNull(alertHistoryByGroupUid.getAlerts());
        assertEquals(5+1, alertHistoryByGroupUid.getAlerts().length); //1 - is the first alert triggered, 5 - the rest of the history
        
        
        Alert alert = new AlertFactory()
        .severity(AlertSeverity.WARNING)
        .status(AlertStatus.RESOLVED)
        .groupUid("group7")
        .description("alert#11")
        .toAlert();

        assertTrue(repository.addAlert(alert));
        AlertHistory alertHistoryByGroupUid7 = repository.getAlertHistoryByGroupUid("group7");
        assertNotNull(alertHistoryByGroupUid7);
        assertNotNull(alertHistoryByGroupUid7.getDetails());
        assertTrue(alertHistoryByGroupUid7.getDetails().getLastAlertStatus().isResolved());
        assertNotNull(alertHistoryByGroupUid7.getAlerts());
        assertEquals(5+2, alertHistoryByGroupUid7.getAlerts().length); //2 - is the first&last alert triggered, 5 - the rest of the history
    }
    
    /** history is ordered by timestamp of last alert */
    public void test8() {
        
        /* 
         * Create 3 groups of alerts with increasing timestamp.
         * Create the following sequence:
         * group 0: [1, 4, 7]
         * group 1: [2, 5, 8]
         * group 2: [3, 6, 9]
         */
        long timestamp = 1;
        for (int j=0; j<3; ++j) {
            for (int i=0; i<3; ++i) {
                Alert alert = new AlertFactory()
                .severity(AlertSeverity.WARNING)
                .status(AlertStatus.RAISED)
                .groupUid("group"+i)
                .description("alert#"+timestamp)
                .timestamp(timestamp++)
                .toAlert();

                assertTrue(repository.addAlert(alert));
            }
        }
        
        AlertHistory[] alertHistory = repository.getAlertHistory();
        
        /*
         * Assert the following order:
         * group 2: [3, 6, 9] (latest alert#9 in group)
         * group 1: [2, 5, 8]
         * group 0: [1, 4, 7] (earliest alert#7 in group)
         */
        long expectedTimestamp;
        for (int j=0; j<3; ++j) {
            expectedTimestamp = 3 - j;
            for (int i=0; i<3; ++i) {
                assertEquals(expectedTimestamp, alertHistory[j].getAlerts()[i].getTimestamp());
                expectedTimestamp+=3;
            }
        }
        
        /* 
         * resolve group 0
         * assert the following order:
         * group 0: [1, 4, 7, 10]
         * group 2: [3, 6, 9]
         * group 1: [2, 5, 8]
         */
        Alert alert = new AlertFactory()
        .severity(AlertSeverity.WARNING)
        .status(AlertStatus.RESOLVED)
        .groupUid("group"+0)
        .description("alert#"+timestamp)
        .timestamp(timestamp++)
        .toAlert();

        assertTrue(repository.addAlert(alert));
        alertHistory = repository.getAlertHistory();
        assertEquals(10, alertHistory[0].getAlerts()[3].getTimestamp());
        assertEquals(1, alertHistory[0].getAlerts()[0].getTimestamp());
        assertEquals(3, alertHistory[1].getAlerts()[0].getTimestamp());
        assertEquals(2, alertHistory[2].getAlerts()[0].getTimestamp());
        
        /* 
         * resolve group 1
         * assert the following order:
         * group 1: [2, 5, 8, 11]
         * group 0: [1, 4, 7, 10]
         * group 2: [3, 6, 9]
         */
        alert = new AlertFactory()
        .severity(AlertSeverity.WARNING)
        .status(AlertStatus.RESOLVED)
        .groupUid("group"+1)
        .description("alert#"+timestamp)
        .timestamp(timestamp++)
        .toAlert();

        assertTrue(repository.addAlert(alert));
        alertHistory = repository.getAlertHistory();
        assertEquals(11, alertHistory[0].getAlerts()[3].getTimestamp());
        assertEquals(2, alertHistory[0].getAlerts()[0].getTimestamp());
        assertEquals(1, alertHistory[1].getAlerts()[0].getTimestamp());
        assertEquals(3, alertHistory[2].getAlerts()[0].getTimestamp());
    }
}
