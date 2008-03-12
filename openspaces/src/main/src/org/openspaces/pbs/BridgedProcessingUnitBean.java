package org.openspaces.pbs;

import org.jini.rio.boot.ServiceClassLoader;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import com.gigaspaces.serialization.pbs.openspaces.ProcessingUnitProxy; 

/**
 * Bridged processing unit implementation
 * 
 * @author eitany
 * @since 6.5
 */
public class BridgedProcessingUnitBean implements InitializingBean, DisposableBean, ClusterInfoAware {
    private ProcessingUnitProxy _proxy;  
    private String _assemblyFullPath;
    private String _implementationName;
    private String[] _dependencies;
    private ClusterInfo _clusterInfo;

    /**
     * Injects the bridged processing unit implementation's assembly full path
     * 
     * @param assemblyFullPath
     */
    public void setAssemblyFullPath(String assemblyFullPath) {
        _assemblyFullPath = assemblyFullPath;
    }

    /**
     * Injects the bridged processing unit implementation's full type name
     * 
     * @param implementationName
     */
    public void setImplementationName(String implementationName) {
        _implementationName = implementationName;
    }

    /**
     * Injects the bridged processing unit implementation's dependencies
     * 
     * @param dependencies
     */
    public void setDependencies(String[] dependencies) {
        _dependencies = dependencies;
    }
	/**
	 * {@inheritDoc}
	 */
    public void afterPropertiesSet() throws Exception {  
        
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            if (classLoader instanceof ServiceClassLoader) {
                Thread.currentThread().setContextClassLoader(classLoader.getParent());
            }
            _proxy = new ProcessingUnitProxy(_assemblyFullPath, _implementationName, _dependencies);
            if (_clusterInfo == null) {
                _proxy.init();
            } else {
                _proxy.init(_clusterInfo.getBackupId(), _clusterInfo.getInstanceId(), _clusterInfo.getNumberOfBackups(), _clusterInfo.getNumberOfInstances(), _clusterInfo.getSchema());
            }
            _proxy.start();
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }
	/**
	 * {@inheritDoc}
	 */
    public void destroy() throws Exception {
        _proxy.stop();
        _proxy.destruct();
        _proxy = null;
    }
	/**
	 * {@inheritDoc}
	 */
    public void setClusterInfo(ClusterInfo clusterInfo) {
        _clusterInfo = clusterInfo;
    }

}
