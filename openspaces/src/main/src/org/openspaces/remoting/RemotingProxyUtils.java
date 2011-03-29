package org.openspaces.remoting;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.MethodInvoker;
import org.springframework.util.StringUtils;

import com.gigaspaces.document.SpaceDocument;

import java.lang.annotation.Annotation;

/**
 * A set of common code shared between different remoting proxies.
 *
 * @author kimchy
 */
public abstract class RemotingProxyUtils {

    private RemotingProxyUtils() {

    }

    /**
     * Computes the routing index for a given remote invocation.
     */
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
                            Object parameter = methodInvocation.getArguments()[i];
                            if (parameter instanceof SpaceDocument){
                                routing = ((SpaceDocument)parameter).getProperty(routingAnnotation.value());
                            } else {
                                MethodInvoker methodInvoker = new MethodInvoker();
                                methodInvoker.setTargetObject(parameter);
                                methodInvoker.setTargetMethod(routingAnnotation.value());
                                methodInvoker.prepare();
                                routing = methodInvoker.invoke();
                            }
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
