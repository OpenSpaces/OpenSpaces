package org.openspaces.admin.alert.alerts;


import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.events.AlertTriggeredEventListener;
import org.openspaces.admin.space.ReplicationStatus;
import org.openspaces.admin.space.SpaceInstance;

/**
 * A Mirror persistency failure alert, triggered upon a failed replication from primary Space to
 * Mirror Space, due to an error (e.g. a DB error). The alert is raised when the replication channel
 * has encountered an exception reported by the Mirror target. The alert is resolved when the Mirror
 * manages to persist to the DB for first time after the alert has been triggered.
 * <p>
 * This alert will be received on the call to
 * {@link AlertTriggeredEventListener#alertTriggered(Alert)} for registered listeners.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class MirrorPersistenceFailureAlert extends AbstractAlert {

    private static final long serialVersionUID = 1L;
    
    public static final String INCONSISTENCY_REASON = "inconsistency-reason";
    public static final String ROOT_CAUSE_MESSAGE = "root-cause-message";
    public static final String ROOT_CAUSE_TRACE = "root-cause-trace";
    public static final String REPLICATION_STATUS = "replication-status";
    public static final String REDO_LOG_SIZE = "redo-log-size";
    public static final String REDO_LOG_RETAINED_SIZE = "redo-log-retained-size";

    public static final String FAILED_OPERATION_COUNT = "failed-operation-count";
    public static final String IN_PROGRESS_OPERATION_COUNT = "in-progress-operation-count";
    public static final String DISCARDED_OPERATION_COUNT = "discarded-operation-count";
    
    /** required by java.io.Externalizable */
    public MirrorPersistenceFailureAlert() {
    }
    
    public MirrorPersistenceFailureAlert(Alert alert) {
        super(alert);
    }
    
    /**
     * {@inheritDoc}
     * The component UID is equivalent to {@link SpaceInstance#getUid()}
     */
    @Override
    public String getComponentUid() {
        return super.getComponentUid();
    }

    /**
     * The reason why a source (primary Space) could not replicate to a target (Mirror Space). This
     * reason could be failure to persist to a database, a failure to replicate to due memory
     * shortage, failure to connect to target, etc. The failure is given as a full stack trace of
     * the original exception.
     * 
     * @see #getRootCauseMessage()
     * @see #getRootCauseTrace()
     * @return the inconsistency reason; may be <code>null</code>.
     */
    public String getInconsistencyReason() {
        return getProperties().get(INCONSISTENCY_REASON);
    }
    
    /**
     * The last cause message in the reason given by {@link #getInconsistencyReason()}. 
     * 
     * @return the root cause message; may be <code>null</code>.
     */
    public String getRootCauseMessage() {
        return getProperties().get(ROOT_CAUSE_MESSAGE);
    }
    
    /**
     * The last cause stack trace in the reason given by {@link #getInconsistencyReason()}. 
     * 
     * @return the root cause trace; may be <code>null</code>.
     */
    public String getRootCauseTrace() {
        return getProperties().get(ROOT_CAUSE_TRACE);
    }
    
    /**
     * The {@link ReplicationStatus replication status} from source to target Space.
     * @return the replication status; may be <code>null</code>.
     */
    public String getReplicationStatus() {
        return getProperties().get(REPLICATION_STATUS);
    }
    
    /**
     * The total redo-log size (for all channels) for both memory and swap.
     * @return the redo-log size; may be <code>null</code>.
     */
    public Integer getRedoLogSize() {
        String value = getProperties().get(REDO_LOG_SIZE);
        if (value == null) return null;
        return Integer.valueOf(value);
    }
    
    /**
     * The redo-log size for replication packets outgoing to the Mirror (both memory and swap).
     * @return the redo-log size; may be <code>null</code>.
     */
    public Integer getRedoLogRetainedSize() {
        String value = getProperties().get(REDO_LOG_SIZE);
        if (value == null) return null;
        return Integer.valueOf(value);
    }
    
    /**
     * The failed operation count reported by the Mirror.
     * @return the failed operation count; may be <code>null</code>.
     */
    public Integer getFailedOperationCount() {
        String value = getProperties().get(FAILED_OPERATION_COUNT);
        if (value == null) return null;
        return Integer.valueOf(value);
    }
    
    /**
     * The in-progress operation count reported by the Mirror.
     * @return the in-progress operation count; may be <code>null</code>.
     */
    public Integer getInProgressOperationCount() {
        String value = getProperties().get(IN_PROGRESS_OPERATION_COUNT);
        if (value == null) return null;
        return Integer.valueOf(value);
    }
    
    /**
     * The discarded operation count reported by the Mirror.
     * @return the discarded operation count; may be <code>null</code>.
     */
    public Integer getDiscardedOperationCount() {
        String value = getProperties().get(DISCARDED_OPERATION_COUNT);
        if (value == null) return null;
        return Integer.valueOf(value);
    }
}
