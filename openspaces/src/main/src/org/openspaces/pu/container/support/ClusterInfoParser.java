package org.openspaces.pu.container.support;

import org.openspaces.core.cluster.ClusterInfo;

/**
 * @author kimchy
 */
public abstract class ClusterInfoParser {

    public static final String CLUSTER_PARAMETER_TOTALMEMBERS = "totalMembers";
    public static final String CLUSTER_PARAMETER_INSTANCEID = "instanceId";
    public static final String CLUSTER_PARAMETER_BACKUPID = "backupId";
    public static final String CLUSTER_PARAMETER_CLUSTERSCHEMA = "schema";

    public static ClusterInfo parse(CommandLineParser.Parameter[] params) throws IllegalArgumentException {
        ClusterInfo clusterInfo = null;
        for (int i = 0; i < params.length; i++) {
            if (!params[i].getName().equalsIgnoreCase("cluster")) {
                continue;
            }

            if (clusterInfo == null) {
                clusterInfo = new ClusterInfo();
            }

            if (params[i].getArguments().length != 2) {
                throw new IllegalArgumentException("deploy parameter should have two parameres, the deploy property name and its value");
            }

            String name = params[i].getArguments()[0];
            String value = params[i].getArguments()[1];

            if (CLUSTER_PARAMETER_TOTALMEMBERS.equalsIgnoreCase(name)) {
                int commaIndex = value.indexOf(',');
                if (commaIndex == -1) {
                    clusterInfo.setNumberOfInstances(Integer.valueOf(value));
                } else {
                    String numberOfInstances = value.substring(0, commaIndex);
                    String numberOfBackups = value.substring(commaIndex + 1);
                    clusterInfo.setNumberOfInstances(Integer.valueOf(numberOfInstances));
                    clusterInfo.setNumberOfBackups(Integer.valueOf(numberOfBackups));
                }
            } else if (CLUSTER_PARAMETER_INSTANCEID.equalsIgnoreCase(name)) {
                clusterInfo.setInstanceId(Integer.valueOf(value));
            } else if (CLUSTER_PARAMETER_BACKUPID.equalsIgnoreCase(name)) {
                clusterInfo.setBackupId(Integer.valueOf(value));
            } else if (CLUSTER_PARAMETER_CLUSTERSCHEMA.equalsIgnoreCase(name)) {
                clusterInfo.setSchema(value);
            } else {
                throw new IllegalArgumentException("deploy parameter property name [" + name + "] is invalid");
            }
        }
        return clusterInfo;
    }
}
