package org.openspaces.remoting;

import com.j_spaces.core.client.MetaDataEntry;

/**
 * @author kimchy
 */
public class SpaceRemoteInvocation extends MetaDataEntry {

    public String lookupName;

    public String methodName;

    public Object[] arguments;

    public Integer routing;

    public SpaceRemoteInvocation() {

    }

    public SpaceRemoteInvocation(String lookupName, String methodName, Object[] arguments) {
        this.lookupName = lookupName;
        this.methodName = methodName;
        this.arguments = arguments;
    }

    public String getLookupName() {
        return lookupName;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public Integer getRouting() {
        return routing;
    }

    public void setRouting(Integer routing) {
        this.routing = routing;
    }

    public static String[] __getSpaceIndexedFields() {
        return new String[]{"routing"};
    }
}
