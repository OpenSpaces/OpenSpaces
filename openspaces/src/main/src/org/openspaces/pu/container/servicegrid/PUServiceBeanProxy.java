package org.openspaces.pu.container.servicegrid;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.gigaspaces.lrmi.nio.async.FutureContext;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.RuntimeHolder;
import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.core.filters.StatisticsHolder;
import net.jini.core.lookup.ServiceID;
import net.jini.id.Uuid;
import org.jini.rio.resources.servicecore.AbstractProxy;
import org.openspaces.core.cluster.ClusterInfo;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author kimchy
 */
public class PUServiceBeanProxy extends AbstractProxy implements PUServiceBean {

    private static final long serialVersionUID = 1L;

    static PUServiceBeanProxy getInstance(PUServiceBean puServiceBean, Uuid id) {
        return (new PUServiceBeanProxy(puServiceBean, id));
    }

    /**
     * Private constructor
     */
    protected PUServiceBeanProxy(PUServiceBean gsc, Uuid id) {
        super(gsc, id);
    }

    public boolean isMemberAliveEnabled() throws RemoteException {
        return ((PUServiceBean) server).isMemberAliveEnabled();
    }

    public boolean isAlive() throws RemoteException, Exception {
        return ((PUServiceBean) server).isAlive();
    }

    public Object[] listServiceDetails() throws RemoteException {
        return ((PUServiceBean) server).listServiceDetails();
    }

    private transient ClusterInfo clusterInfo;

    public ClusterInfo getClusterInfo() throws RemoteException {
        if (clusterInfo != null) {
            return this.clusterInfo;
        }
        clusterInfo = ((PUServiceBean) server).getClusterInfo();
        return clusterInfo;
    }

    private transient String presentationName;

    public String getPresentationName() throws RemoteException {
        if (presentationName != null) {
            return this.presentationName;
        }
        presentationName = ((PUServiceBean) server).getPresentationName();
        return presentationName;
    }

    private transient PUDetails puDetails;

    public PUDetails getPUDetails() throws RemoteException {
        if (puDetails != null) {
            return puDetails;
        }
        puDetails = ((PUServiceBean) server).getPUDetails();
        return puDetails;
    }

    public PUMonitors getPUMonitors() throws RemoteException {
        return ((PUServiceBean) server).getPUMonitors();
    }

    public IJSpace getSpaceDirect(ServiceID serviceID) throws RemoteException {
        return ((PUServiceBean) server).getSpaceDirect(serviceID);
    }

    public RuntimeHolder getSpaceRuntimeHolder(ServiceID serviceID) throws RemoteException {
        return ((PUServiceBean) server).getSpaceRuntimeHolder(serviceID);
    }

    public StatisticsHolder getSpaceStatisticsHolder(ServiceID serviceID) throws RemoteException {
        return ((PUServiceBean) server).getSpaceStatisticsHolder(serviceID);
    }

    private transient SpaceURL[] spaceURLs;

    public SpaceURL[] listSpacesURLs() throws RemoteException {
        if (spaceURLs != null) {
            return spaceURLs;
        }
        spaceURLs = ((PUServiceBean) server).listSpacesURLs();
        return spaceURLs;
    }

    public SpaceMode[] listSpacesModes() throws RemoteException {
        return ((PUServiceBean) server).listSpacesModes();
    }

    public int getState() throws RemoteException {
        return ((PUServiceBean) server).getState();
    }

    public Object getServiceProxy() throws RemoteException {
        return ((PUServiceBean) server).getServiceProxy();
    }

    public boolean isStopping() throws RemoteException {
        return ((PUServiceBean) server).isStopping();
    }

    public Future<Object> invoke(String serviceBeanName, Map<String, Object> namedArgs) throws RemoteException {
        ((PUServiceBean) server).invoke(serviceBeanName, namedArgs);
        return FutureContext.getFutureResult();

    }
}
