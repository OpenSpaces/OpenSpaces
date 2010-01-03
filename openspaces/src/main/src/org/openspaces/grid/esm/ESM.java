package org.openspaces.grid.esm;


import org.jini.rio.resources.servicecore.Service;
import org.openspaces.admin.esm.deployment.ElasticDataGridDeployment;

import com.gigaspaces.grid.gsa.AgentIdAware;
import com.gigaspaces.grid.zone.GridZoneProvider;
import com.gigaspaces.internal.dump.InternalDumpProvider;
import com.gigaspaces.internal.jvm.JVMInfoProvider;
import com.gigaspaces.internal.log.InternalLogProvider;
import com.gigaspaces.internal.os.OSInfoProvider;
import com.gigaspaces.lrmi.nio.info.NIOInfoProvider;


/**
 * The Elastic Service Manager (ESM) provides the support to deploy, manage and
 * monitor elastic services in the Grid/Cloud.
 */
public interface ESM extends Service, /* SecuredService, */AgentIdAware,
		NIOInfoProvider, OSInfoProvider, JVMInfoProvider, GridZoneProvider,
		InternalLogProvider, InternalDumpProvider {

    void deploy(ElasticDataGridDeployment deployment);
}
