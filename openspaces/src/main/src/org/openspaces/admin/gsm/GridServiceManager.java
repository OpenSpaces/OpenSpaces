package org.openspaces.admin.gsm;

import org.openspaces.admin.GridComponent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.space.SpaceDeployment;

/**
 * @author kimchy
 */
public interface GridServiceManager extends GridComponent {

    ProcessingUnit deploy(ProcessingUnitDeployment deployment);

    ProcessingUnit deploy(SpaceDeployment deployment);
}
