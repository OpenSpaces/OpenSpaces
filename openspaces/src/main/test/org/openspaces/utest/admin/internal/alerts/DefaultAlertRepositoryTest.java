package org.openspaces.utest.admin.internal.alerts;

import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.internal.alert.DefaultAlertRepository;

import junit.framework.TestCase;



/**
 * Unit Tests the {@link DefaultAlertRepository}
 * @author Moran Avigdor
 */
public class DefaultAlertRepositoryTest extends TestCase {

    final DefaultAlertRepository repository = new DefaultAlertRepository();

    /** adding an {@link AlertStatus#RESOLVED} alert with a non-existing group UID should open an entry in the repository */
    public void test1() {
        Alert alert = new AlertFactory()
        .severity(AlertSeverity.WARNING)
        .status(AlertStatus.RESOLVED)
        .groupUid("group1")
        .toAlert();

        repository.addAlert(alert);
        assertNotNull(repository.getAlertByAlertUid(alert.getAlertUid()));
        Alert[] alertsByGroupUid = repository.getAlertsByGroupUid(alert.getGroupUid());
        assertNotNull(alertsByGroupUid);
        assertEquals(1, alertsByGroupUid.length);
        assertEquals(alert, alertsByGroupUid[0]);
    }
    
    /** adding an {@link AlertStatus#NA} alert with a non-existing group UID should open an entry in the repository */
    public void test2() {
        Alert alert = new AlertFactory()
        .severity(AlertSeverity.WARNING)
        .status(AlertStatus.NA)
        .groupUid("group2")
        .toAlert();

        repository.addAlert(alert);
        assertNotNull(repository.getAlertByAlertUid(alert.getAlertUid()));
        Alert[] alertsByGroupUid = repository.getAlertsByGroupUid(alert.getGroupUid());
        assertNotNull(alertsByGroupUid);
        assertEquals(1, alertsByGroupUid.length);
        assertEquals(alert, alertsByGroupUid[0]);
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

            repository.addAlert(alert);
            assertNotNull(repository.getAlertByAlertUid(alert.getAlertUid()));
            Alert[] alertsByGroupUid = repository.getAlertsByGroupUid(alert.getGroupUid());
            assertNotNull(alertsByGroupUid);
            assertFalse(alertsByGroupUid[0].getStatus().isResolved());
            assertEquals(i+1, alertsByGroupUid.length);
            assertEquals(alert, alertsByGroupUid[0]);
        }

        Alert alert = new AlertFactory()
        .severity(AlertSeverity.WARNING)
        .status(AlertStatus.RESOLVED)
        .groupUid("group3")
        .description("alert#4")
        .toAlert();

        repository.addAlert(alert);
        assertNotNull(repository.getAlertByAlertUid(alert.getAlertUid()));
        Alert[] alertsByGroupUid = repository.getAlertsByGroupUid(alert.getGroupUid());
        assertNotNull(alertsByGroupUid);
        assertTrue(alertsByGroupUid[0].getStatus().isResolved());
        assertEquals(4, alertsByGroupUid.length);
        assertEquals(alert, alertsByGroupUid[0]);
        
        assertEquals(4, repository.size());
    }
    
    /** 
     * adding an {@link AlertStatus#RAISED} alert should open an entry in the repository.
     * adding an {@link AlertStatus#NA} alert should behave as any raised alert in the group.
     * adding another {@link AlertStatus#RAISED} alert should keep adding to the same group.
     */
    public void test4() {
        
        for (int i=0; i<7; ++i) {
            Alert alert = new AlertFactory()
            .severity(AlertSeverity.WARNING)
            .status( (i!=4 ? AlertStatus.RAISED : AlertStatus.NA) )
            .groupUid("group4")
            .description("alert#"+i)
            .toAlert();
            

            repository.addAlert(alert);
            assertNotNull(repository.getAlertByAlertUid(alert.getAlertUid()));
            Alert[] alertsByGroupUid = repository.getAlertsByGroupUid(alert.getGroupUid());
            assertNotNull(alertsByGroupUid);
            assertFalse(alertsByGroupUid[0].getStatus().isResolved());
            assertEquals(i+1, alertsByGroupUid.length);
            assertEquals(alert, alertsByGroupUid[0]);
        }

        Alert alert = new AlertFactory()
        .severity(AlertSeverity.WARNING)
        .status(AlertStatus.RESOLVED)
        .groupUid("group4")
        .description("alert#7")
        .toAlert();

        repository.addAlert(alert);
        assertNotNull(repository.getAlertByAlertUid(alert.getAlertUid()));
        Alert[] alertsByGroupUid = repository.getAlertsByGroupUid(alert.getGroupUid());
        assertNotNull(alertsByGroupUid);
        assertTrue(alertsByGroupUid[0].getStatus().isResolved());
        assertEquals(8, alertsByGroupUid.length);
        assertEquals(alert, alertsByGroupUid[0]);
        
        assertEquals(8, repository.size());
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

            repository.addAlert(alert);
        }

        for (int i=0; i<3; ++i) {
            Alert alert = new AlertFactory()
            .severity(AlertSeverity.WARNING)
            .status(AlertStatus.RESOLVED)
            .groupUid("group5")
            .description("alert#"+(i+4))
            .toAlert();

            if (i==0) {
                repository.addAlert(alert);
                assertNotNull(repository.getAlertByAlertUid(alert.getAlertUid()));
                Alert[] alertsByGroupUid = repository.getAlertsByGroupUid(alert.getGroupUid());
                assertNotNull(alertsByGroupUid);
                assertTrue(alertsByGroupUid[0].getStatus().isResolved());
                assertEquals(4, alertsByGroupUid.length);
                assertEquals(alert, alertsByGroupUid[0]);
            }
            else {
                repository.addAlert(alert);
                assertNotNull(repository.getAlertByAlertUid(alert.getAlertUid()));
                Alert[] alertsByGroupUid = repository.getAlertsByGroupUid(alert.getGroupUid());
                assertNotNull(alertsByGroupUid);
                assertTrue(alertsByGroupUid[0].getStatus().isResolved());
                assertEquals(1, alertsByGroupUid.length);
                assertEquals(alert, alertsByGroupUid[0]);
            }
        }
        
        assertEquals(6, repository.size());
    }
    
    /** 
     * adding an {@link AlertStatus#RAISED} alert should open an entry in the repository.
     * adding an {@link AlertStatus#RESOLVED} alert should resolve the alert group.
     * repeat, should always add the new raised alert belonging to the same group.
     */
    public void test6() {
        
        for (int repeat=0; repeat<2; ++repeat) {
            for (int i=0; i<3; ++i) {
                Alert alert = new AlertFactory()
                .severity(AlertSeverity.WARNING)
                .status(AlertStatus.RAISED)
                .groupUid("group6")
                .description("alert#"+i)
                .toAlert();

                repository.addAlert(alert);
            }

            Alert alert = new AlertFactory()
            .severity(AlertSeverity.WARNING)
            .status(AlertStatus.RESOLVED)
            .groupUid("group6")
            .description("alert#4")
            .toAlert();

            repository.addAlert(alert);
            assertNotNull(repository.getAlertByAlertUid(alert.getAlertUid()));
            Alert[] alertsByGroupUid = repository.getAlertsByGroupUid(alert.getGroupUid());
            assertNotNull(alertsByGroupUid);
            assertTrue(alertsByGroupUid[0].getStatus().isResolved());
            assertEquals(4, alertsByGroupUid.length);
            assertEquals(alert, alertsByGroupUid[0]);
        }
        
        assertEquals(2*4, repository.size());
    }
    
    
    
    /** limit repository size */
    public void test7() {

        final int LIMIT = 200;
        repository.setStoreLimit(LIMIT);
        
        for (int i=0; i<LIMIT+10; ++i) {
            for (int j=0; j<7; ++j) {
                Alert alert = new AlertFactory()
                .severity(AlertSeverity.WARNING)
                .status(AlertStatus.RAISED)
                .groupUid("group"+i)
                .description("alert#"+i+"#"+j)
                .toAlert();

                repository.addAlert(alert);
                Alert[] alertsByGroupUid = repository.getAlertsByGroupUid(alert.getGroupUid());
                assertNotNull(alertsByGroupUid);
                assertEquals(alert, alertsByGroupUid[0]);
            }
            
            //assert that the first and last alert are always kept
            Alert[] alertsByGroupUid = repository.getAlertsByGroupUid("group"+i);
            assertNotNull(alertsByGroupUid);
            assertTrue(alertsByGroupUid[0].getDescription().equals("alert#"+i+"#6"));
            assertTrue(alertsByGroupUid[alertsByGroupUid.length -1].getDescription().equals("alert#"+i+"#0"));
        }
        
        //limit is always kept - even at the risk of loosing unresolved alerts.
        assertEquals(LIMIT, repository.size());
        
        for (int i=0; i<10; ++i) {
            Alert alert = new AlertFactory()
            .severity(AlertSeverity.WARNING)
            .status(AlertStatus.RAISED)
            .groupUid("group"+i)
            .description("alert#"+i)
            .timestamp(i)
            .toAlert();

            repository.addAlert(alert);
        }
        
        //there should only be as much as declared history size
        assertEquals(LIMIT, repository.size());
        
        //check that last alerts are kept;
        for (int i=0; i<10; ++i) {
            Alert[] alertsByGroupUid = repository.getAlertsByGroupUid("group"+i);
            assertNotNull(alertsByGroupUid);
            assertEquals(i, alertsByGroupUid[0].getTimestamp());
            assertEquals(1, alertsByGroupUid.length);
        }
        
        //add some resolved alerts in between
        for (int i=5; i<15; ++i) {
            Alert alert = new AlertFactory()
            .severity(AlertSeverity.WARNING)
            .status(AlertStatus.RESOLVED)
            .groupUid("group"+i)
            .description("alert#"+i)
            .timestamp(i)
            .toAlert();

            repository.addAlert(alert);
        }
        
        //there should only be as much as declared history size
        assertEquals(LIMIT, repository.size());
        
        //check that last alerts are kept;
        for (int i=5; i<15; ++i) {
            Alert[] alertsByGroupUid = repository.getAlertsByGroupUid("group"+i);
            assertNotNull(alertsByGroupUid);
            assertTrue(alertsByGroupUid[0].getStatus().isResolved());
            assertEquals(i, alertsByGroupUid[0].getTimestamp());
            assertEquals((i<10?2:1), alertsByGroupUid.length);
        }
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

                repository.addAlert(alert);
            }
        }
        
        /*
         * Assert the following order:
         * group 2: [9, 6, 3] (latest alert#9 in group)
         * group 1: [8, 5, 2]
         * group 0: [7, 4, 1] (earliest alert#7 in group)
         */
        int[][] expected = new int[3][3];
        expected[0] = new int[]{9,6,3};
        expected[1] = new int[]{8,5,2};
        expected[2] = new int[]{7,4,1};
        
        int i=0;
        for (Iterable<Alert> iter : repository.list()) {
            int j=0;
            for (Alert alert : iter) {
                assertEquals(expected[i][j], alert.getTimestamp());
                j++;
            }
            i++;
        }
    }
}
