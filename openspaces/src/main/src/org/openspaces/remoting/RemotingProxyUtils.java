package org.openspaces.remoting;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.MethodInvoker;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;

/**
 * A set of common code shared between different remoting proxies.
 *
 * @author kimchy
 */
public abstract class RemotingProxyUtils {

    private RemotingProxyUtils() {

    }

    public static Object computeRouting(SpaceRemotingInvocation remotingEntry, RemoteRoutingHandler remoteRoutingHandler,
                                        MethodInvocation methodInvocation) throws Exception {
        Object routing = null;
        if (remoteRoutingHandler != null) {
            routing = remoteRoutingHandler.computeRouting(remotingEntry);
        }
        if (routing == null) {
            Annotation[][] parametersAnnotations = methodInvocation.getMethod().getParameterAnnotations();
            for (int i = 0; i < parametersAnnotations.length; i++) {
                Annotation[] parameterAnnotations = parametersAnnotations[i];
                for (Annotation parameterAnnotation : parameterAnnotations) {
                    if (parameterAnnotation instanceof Routing) {
                        Routing routingAnnotation = (Routing) parameterAnnotation;
                        if (StringUtils.hasLength(routingAnnotation.value())) {
                            MethodInvoker methodInvoker = new MethodInvoker();
                            methodInvoker.setTargetObject(methodInvocation.getArguments()[i]);
                            methodInvoker.setTargetMethod(routingAnnotation.value());
                            methodInvoker.prepare();
                            routing = methodInvoker.invoke();
                        } else {
                            routing = methodInvocation.getArguments()[i];
                        }
                        i = parametersAnnotations.length;
                        break;
                    }
                }
            }
        }
        if (routing == null) {
            routing = remotingEntry.hashCode();
        }
        return routing;
    }
}
