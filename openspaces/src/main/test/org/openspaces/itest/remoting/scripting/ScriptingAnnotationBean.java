package org.openspaces.itest.remoting.scripting;

import org.openspaces.remoting.scripting.*;

/**
 * @author kimchy
 */
public class ScriptingAnnotationBean {

    @EventDrivenScriptingExecutor
    private ScriptingExecutor eventScriptingExecutor;

    @ExecutorScriptingExecutor
    private ScriptingExecutor executorScriptingExecutor;

    public Integer executeEventScriptThatReturns2() {
        return (Integer) eventScriptingExecutor.execute(new StaticScript("executeAsyncScriptThatReturns2", "groovy", "return 2"));
    }

    public Integer executeExecutorScriptThatReturns2() {
        return (Integer) executorScriptingExecutor.execute(new StaticScript("executeAsyncScriptThatReturns2", "groovy", "return 2"));
    }
}
