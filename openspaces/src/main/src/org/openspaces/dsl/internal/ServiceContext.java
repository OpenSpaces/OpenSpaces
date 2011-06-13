package org.openspaces.dsl.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.dsl.Service;

public class ServiceContext {

    private Service service;
    private Admin admin;
    private String dir;

    public ServiceContext(final Service service, final Admin admin, final String dir) {
        super();
        this.service = service;
        this.admin = admin;
        this.dir = dir;

    }

    private String text = "Some Text";

    public List<ProcessingUnitInstance> getServiceInstances() {
        final ProcessingUnit pu = getProcessingUnitFromAdmin();

        return Arrays.asList(pu.getInstances());
    }

    public List<GridServiceAgent> getAgents() {

        final List<ProcessingUnitInstance> puis = getServiceInstances();
        final List<GridServiceAgent> gsas = new ArrayList<GridServiceAgent>(puis.size());
        for (final ProcessingUnitInstance pui : puis) {
            final GridServiceAgent agent = pui.getGridServiceContainer().getGridServiceAgent();
            gsas.add(agent);
        }

        return gsas;

    }

    public List<String> getIPs() {
        final List<GridServiceAgent> agents = getAgents();

        final List<String> ips = new ArrayList<String>(agents.size());
        for (final GridServiceAgent agent : agents) {
            ips.add(agent.getMachine().getHostAddress());
        }

        return ips;
    }

    private ProcessingUnit getProcessingUnitFromAdmin() {
        final String name = this.service.getName();
        final ProcessingUnit pu = admin.getProcessingUnits().getProcessingUnit(name);
        if (pu == null) {
            throw new IllegalStateException("Processing unit with name: " + name
                    + " was not found in the cluster. That should not be possible");
        }

        return pu;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public Service getService() {
        return service;
    }

    public void setService(final Service service) {
        this.service = service;
    }

    public String getDir() {
        return dir;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

}
