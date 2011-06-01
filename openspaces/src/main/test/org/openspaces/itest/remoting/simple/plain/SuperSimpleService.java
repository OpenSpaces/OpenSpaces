package org.openspaces.itest.remoting.simple.plain;

import org.openspaces.remoting.Routing;


public interface SuperSimpleService{

     String superSay(@Routing String message);
}
