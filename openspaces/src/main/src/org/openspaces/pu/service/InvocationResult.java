package org.openspaces.pu.service;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class InvocationResult implements Externalizable{

    private int instanceId;
    private boolean executeOnce;
    private boolean executeOnceThrowedException;
    private boolean executeOnAllThrowedException;
    private Object executeOnceResult;
    private Object executeOnAllResult;
   
    public int getInstanceId() {
        return instanceId;
    }
    
    public void setInstanceId(int instanceId) {
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
    
    public boolean isExecuteOnceThrowedException() {
        return executeOnceThrowedException;
    }

    public void setExecuteOnceThrowedException(boolean executeOnceThrowedException) {
        this.executeOnceThrowedException = executeOnceThrowedException;
    }

    public boolean isExecuteOnAllThrowedException() {
        return executeOnAllThrowedException;
    }

    public void setExecuteOnAllThrowedException(boolean executeOnAllThrowedException) {
        this.executeOnAllThrowedException = executeOnAllThrowedException;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(instanceId);
        out.writeBoolean(executeOnce);
        out.writeBoolean(executeOnceThrowedException);
        out.writeBoolean(executeOnAllThrowedException);
        out.writeObject(executeOnceResult);
        out.writeObject(executeOnAllResult);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        instanceId = in.readInt();
        executeOnce = in.readBoolean();
        executeOnceThrowedException = in.readBoolean();
        executeOnAllThrowedException = in.readBoolean();
        executeOnceResult = in.readObject();
        executeOnAllResult = in.readObject();
    }
    
}
