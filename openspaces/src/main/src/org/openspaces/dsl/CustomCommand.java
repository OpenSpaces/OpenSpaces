package org.openspaces.dsl;

import java.util.concurrent.Callable;

public class CustomCommand {

    private String name;
    private Callable<Object> executeOnce;
    private Callable<Object> executeOnAllInstances;
    
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Callable<Object> getExecuteOnce() {
        return executeOnce;
    }
    public void setExecuteOnce(Callable<Object> executeOnce) {
        this.executeOnce = executeOnce;
    }
    public Callable<Object> getExecuteOnAllInstances() {
        return executeOnAllInstances;
    }
    public void setExecuteOnAllInstances(Callable<Object> executeOnAllInstances) {
        this.executeOnAllInstances = executeOnAllInstances;
    }

}
