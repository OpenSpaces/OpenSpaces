package org.openspaces.dsl;

import java.util.List;

public class Application {

    private String name;
    
    private List<String> serviceNames;
    private List<Service> services;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getServiceNames() {
        return serviceNames;
    }

    public void setServiceNames(List<String> serviceNames) {
        this.serviceNames = serviceNames;
    }

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    @Override
    public String toString() {
        return "Application [name=" + name + ", serviceNames=" + serviceNames + "]";
    }

    
    
}
