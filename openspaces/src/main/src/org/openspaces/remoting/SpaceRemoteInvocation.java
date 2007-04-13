package org.openspaces.remoting;

import com.j_spaces.core.client.MetaDataEntry;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Represnts a Space remoting invocation, holding all the required information of the required
 * invocable service.
 *
 * @author kimchy
 */
public class SpaceRemoteInvocation extends MetaDataEntry implements Externalizable {

    private static final long serialVersionUID = 5027397265383711691L;

    public String lookupName;

    public String methodName;

    public Object[] arguments;

    public Integer routing;

    public Boolean oneWay;

    /**
     * Constructs a new remote invocation.
     */
    public SpaceRemoteInvocation() {
        setNOWriteLeaseMode(true);
    }

    /**
     * Constructs a new remote invocation.
     *
     * @param lookupName The service name (usually its the its interface FQN).
     * @param methodName The method name to invoke within the service
     * @param arguments  The arguments to pass to the service
     */
    public SpaceRemoteInvocation(String lookupName, String methodName, Object[] arguments) {
        setNOWriteLeaseMode(true);
        this.lookupName = lookupName;
        this.methodName = methodName;
        this.arguments = arguments;
    }

    /**
     * Returns the lookup name of the service. This usually would be the service exposed interface
     * FQN.
     */
    public String getLookupName() {
        return lookupName;
    }

    /**
     * Returns the method name to invoke within the service.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Returns the arguments that this service method will be invoked with.
     */
    public Object[] getArguments() {
        return arguments;
    }

    /**
     * Returns the routing index of this invocation. This field is used when working with a
     * partitioned space.
     */
    public Integer getRouting() {
        return routing;
    }

    /**
     * Sets the routing index of this invocation. This field is used when working with a partitioned
     * space.
     */
    public void setRouting(Integer routing) {
        this.routing = routing;
    }

    public static String[] __getSpaceIndexedFields() {
        return new String[]{"routing"};
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super._writeExternal(out);
        out.writeUTF(lookupName);
        out.writeUTF(methodName);
        out.writeInt(routing);
        if (oneWay != null && oneWay) {
            out.writeBoolean(true);
        } else {
            out.writeBoolean(false);
        }
        if (arguments == null || arguments.length == 0) {
            out.writeInt(0);
        } else {
            out.writeInt(arguments.length);
        }
        for (Object argument : arguments) {
            out.writeObject(argument);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super._readExternal(in);
        lookupName = in.readUTF();
        methodName = in.readUTF();
        routing = in.readInt();
        oneWay = in.readBoolean();
        int argumentNumber = in.readInt();
        if (argumentNumber > 0) {
            arguments = new Object[argumentNumber];
            for (int i = 0; i < argumentNumber; i++) {
                arguments[i] = in.readObject();
            }
        }
    }
}
