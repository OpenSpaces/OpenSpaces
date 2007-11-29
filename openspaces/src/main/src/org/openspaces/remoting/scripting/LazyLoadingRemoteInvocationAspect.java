package org.openspaces.remoting.scripting;

import org.aopalliance.intercept.MethodInvocation;
import org.openspaces.remoting.RemoteInvocationAspect;
import org.openspaces.remoting.RemotingInvoker;

/**
 * The lazy loading remote invocation aspect wraps the actual remote invocation and adds support
 * for {@link org.openspaces.remoting.scripting.LazyLoadingScript}s.
 *
 * <p>If the scipt being executed is a lazy loading script, and it is cacheable, the actual script
 * content will not be sent to the remote service. If the script is already complied and cached, there
 * is no need to get the actual script content, and it will be executed. If the script if not loaded
 * (i.e. not compiled and cached), the scripting executor will throw a {@link org.openspaces.remoting.scripting.ScriptNotLoadedException}
 * exception, which then the script will be loaded and executed again. 
 *
 * @author kimchy
 */
public class LazyLoadingRemoteInvocationAspect implements RemoteInvocationAspect {

    /**
     * If the scipt being executed is a lazy loading script, and it is cacheable, the actual script
     * content will not be sent to the remote service. If the script is already complied and cached, there
     * is no need to get the actual script content, and it will be executed. If the script if not loaded
     * (i.e. not compiled and cached), the scripting executor will throw a {@link org.openspaces.remoting.scripting.ScriptNotLoadedException}
     * exception, which then the script will be loaded and executed again.
     */
    public Object invoke(MethodInvocation methodInvocation, RemotingInvoker remotingInvoker) throws Throwable {
        if (methodInvocation.getArguments().length == 0) {
            throw new ScriptingException("Lazy loading aspect only supports ScriptingExecution");
        }
        Script script = (Script) methodInvocation.getArguments()[0];
        if (!(script instanceof LazyLoadingScript)) {
            return remotingInvoker.invokeRemote(methodInvocation);
        }
        LazyLoadingScript lazyLoadingScript = (LazyLoadingScript) script;
        try {
            // if we don't do caching, load it here since lazy loading makes no sense
            if (!lazyLoadingScript.shouldCache()) {
                lazyLoadingScript.loadScript();
            }
            return remotingInvoker.invokeRemote(methodInvocation);
        } catch (ScriptNotLoadedException e) {
            // the script was not loaded, load it and try to inovke it again
            lazyLoadingScript.loadScript();
            return remotingInvoker.invokeRemote(methodInvocation);
        }
    }
}
