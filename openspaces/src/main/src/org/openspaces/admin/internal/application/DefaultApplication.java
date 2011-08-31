package org.openspaces.admin.internal.application;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.DefaultProcessingUnits;
import org.openspaces.admin.internal.pu.InternalProcessingUnits;
import org.openspaces.admin.pu.ProcessingUnits;

public class DefaultApplication implements InternalApplication {

    private final InternalProcessingUnits processingUnits;
    private final String name;
    
    public DefaultApplication(InternalAdmin admin, String name) {
        this.processingUnits = new DefaultProcessingUnits(admin);
        this.name = name;
    }

    @Override
    public ProcessingUnits getProcessingUnits() {
        return processingUnits;
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultApplication that = (DefaultApplication) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
