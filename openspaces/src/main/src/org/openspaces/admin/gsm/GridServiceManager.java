package org.openspaces.admin.gsm;

import org.openspaces.admin.AgentGridComponent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.space.SpaceDeployment;

/**
 * @author kimchy
 */
public interface GridServiceManager extends AgentGridComponent {

    ProcessingUnit deploy(ProcessingUnitDeployment deployment);

    ProcessingUnit deploy(SpaceDeployment deployment);
}
