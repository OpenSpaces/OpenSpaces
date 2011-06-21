package org.openspaces.pu.service;

public class InvocationResult {

    private Integer instanceId;
    private boolean executeOnce;
    private Object executeOnceResult;
    private Object executeOnAllResult;
   
    public Integer getInstanceId() {
        return instanceId;
    }
    
    public void setInstanceId(Integer instanceId) {
        this.instanceId = instanceId;
    }
   
    public boolean isExecuteOnce() {
        return executeOnce;
    }
    
    public void setExecuteOnce(boolean executeOnce) {
        this.executeOnce = executeOnce;
    }
  
    public Object getExecuteOnceResult() {
        return executeOnceResult;
    }
   
    public void setExecuteOnceResult(Object executeOnceResult) {
        this.executeOnceResult = executeOnceResult;
    }
   
    public Object getExecuteOnAllResult() {
        return executeOnAllResult;
    }
   
    public void setExecuteOnAllResult(Object executeOnAllResult) {
        this.executeOnAllResult = executeOnAllResult;
    }
    
}
