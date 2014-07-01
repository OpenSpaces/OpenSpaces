package org.openspaces.grid.esm;

public class EsmSystemProperties {

	public static final String ESM_MACHINE_PROVISIONING_MAX_THREADS = "org.openspaces.esm.machine_provisioning_max_threads";
    public static final int ESM_MACHINE_PROVISIONING_MAX_THREADS_DEFAULT = 64;

    public static final String ESM_USERNAME = "com.gs.esm.username";
    public static final String ESM_PASSWORD = "com.gs.esm.password";

    public static final String ESM_PROPERTIES_USERNAME = "esm-username";
    public static final String ESM_PROPERTIES_PASSWORD = "esm-password";

    public static final String ESM_INIT_POLLING_INTERVAL_SECONDS = "com.gs.esm.discovery_polling_interval_seconds";
    public static final long ESM_INIT_POLLING_INTERVAL_SECONDS_DEFAULT = 20L;

    public static final String ESM_INIT_TIMEOUT_SECONDS = "org.openspaces.grid.initialization-timeout-seconds";
    public static final long ESM_INIT_TIMEOUT_SECONDS_DEFAULT = 3 * 60L;

    public static final String ESM_INIT_EVENTLOOP_KEEPALIVE_ERROR_SECONDS = "org.openspaces.grid.internal-eventloop-keepalive-error-seconds";
    public static final long ESM_INIT_EVENTLOOP_KEEPALIVE_ERROR_SECONDS_DEFAULT = 1*60L;

    public static final String ESM_INIT_WAITFOR_GSM_UPTIME_SECONDS = "com.gs.esm.wait_for_gsm_uptime_seconds";
    public static final long ESM_INIT_WAITFOR_GSM_UPTIME_SECONDS_DEFAULT = 1*60L;

    public static final String ESM_INIT_WAITFOR_LUS_UPTIME_SECONDS = "com.gs.esm.wait_for_lus_uptime_seconds";
    public static final long ESM_INIT_WAITFOR_LUS_UPTIME_SECONDS_DEFAULT = 1*60L;

    public static final String ESM_STATEFUL_DEPLOYMENT_TIMEOUT_SECONDS = "org.openspaces.grid.stateful_deployment_timeout_seconds";
    public static final long ESM_STATEFUL_DEPLOYMENT_TIMEOUT_SECONDS_DEFAULT = 60*60L;

    public static final String ESM_STATELESS_DEPLOYMENT_TIMEOUT_SECONDS = "org.openspaces.grid.stateless_deployment_timeout_seconds";
    public static final long ESM_STATELESS_DEPLOYMENT_TIMEOUT_SECONDS_DEFAULT = 5*60L;

    public static final String ESM_STATEFUL_DEPLOYMENT_FAILURE_FORGET_SECONDS = "org.openspaces.grid.stateful_deployment_failure_forget_seconds";
    public static final long ESM_STATEFUL_DEPLOYMENT_FAILURE_FORGET_SECONDS_DEFAULT = 60*60L;

    public static final String ESM_STATELESS_DEPLOYMENT_FAILURE_FORGET_SECONDS = "org.openspaces.grid.stateless_deployment_failure_forget_seconds";
    public static final long ESM_STATELESS_DEPLOYMENT_FAILURE_FORGET_SECONDS_DEFAULT = 5*60L;
    
    public static final String ESM_START_CONTAINER_TIMEOUT_FAILURE_SECONDS = "org.openspaces.grid.start-container-timeout-seconds";
    public static final Long ESM_START_CONTAINER_TIMEOUT_FAILURE_SECONDS_DEFAULT = 2*60L;

    public static final String ESM_WAIT_BEFORE_START_CONTAINER_AGAIN_SECONDS = "org.openspaces.grid.wait-before-start-container-again-seconds";
    public static final Long ESM_WAIT_BEFORE_START_CONTAINER_AGAIN_SECONDS_DEFAULT = 1*60L;

    public static final String ESM_START_AGENT_TIMEOUT_SECONDS = "org.openspaces.grid.start-agent-timeout-seconds";
    public static final long ESM_START_AGENT_TIMEOUT_SECONDS_DEFAULT = 30*60L;

    public static final String ESM_STOP_AGENT_TIMEOUT_SECONDS = "org.openspaces.grid.stop-agent-timeout-seconds";
    public static final long ESM_STOP_AGENT_TIMEOUT_SECONDS_DEFAULT = 10*60L;
    
    public static final String ESM_CLEANUP_CLOUD_TIMEOUT_SECONDS = "org.openspaces.grid.cleanup-cloud-timeout-seconds";
    public static final long ESM_CLEANUP_CLOUD_TIMEOUT_SECONDS_DEFAULT = 10*60L;
    
    public static final String ESM_BACKUP_INTERVAL_MILLISECONDS = "org.openspaces.grid.state-backup-to-space-interval-milliseconds";
    public static final Long ESM_BACKUP_INTERVAL_MILLISECONDS_DEFAULT = 1000L;
    
    public static final String ESM_BACKUP_MACHINES_STATE_TO_SPACE_FLAG = "org.openspaces.grid.backup-machines-state-to-cloudify-management-space"; //default is false
}
