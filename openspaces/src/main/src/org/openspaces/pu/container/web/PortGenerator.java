package org.openspaces.pu.container.web;

import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author kimchy
 */
public class PortGenerator implements FactoryBean, ClusterInfoAware {

    private ClusterInfo clusterInfo;

    private int basePort = -1;

    public void setClusterInfo(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    public void setBasePort(int basePort) {
        this.basePort = basePort;
    }

    public Object getObject() throws Exception {
        if (basePort == -1) {
            throw new IllegalArgumentException("Must set basePort property");
        }
        return basePort + clusterInfo.getRunningNumer();
    }

    public Class getObjectType() {
        return Integer.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
