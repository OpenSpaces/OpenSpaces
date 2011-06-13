package org.openspaces.dsl;

public class CustomCommand {

    private String name;
    private Runnable executeOnce;
    private Runnable executeOnAllInstances;
    
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Runnable getExecuteOnce() {
        return executeOnce;
    }
    public void setExecuteOnce(Runnable executeOnce) {
        this.executeOnce = executeOnce;
    }
    public Runnable getExecuteOnAllInstances() {
        return executeOnAllInstances;
    }
    public void setExecuteOnAllInstances(Runnable executeOnAllInstances) {
        this.executeOnAllInstances = executeOnAllInstances;
    }

}
