/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.pu.container.jee.jetty.holder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.thread.ThreadPool;

/**
 * A Jetty {@link ThreadPool} that shares a single instance of a thread pool. The first
 * thread pool passed will win and be used.
 *
 * @author kimchy
 */
public class SharedThreadPool implements ThreadPool, LifeCycle {

    private static final Log logger = LogFactory.getLog(SharedThreadPool.class);

    private static volatile ThreadPool threadPool;

    private static volatile int threadPoolCount;

    private static final Object threadPoolLock = new Object();

    public SharedThreadPool(ThreadPool delegate) {
        synchronized (threadPoolLock) {
            if (threadPool == null) {
                threadPool = delegate;
                logger.debug("Using new thread pool [" + delegate + "]");
            } else {
                logger.debug("Using existing thread pool [" + threadPool + "]");
            }
        }
    }

    public boolean dispatch(Runnable runnable) {
        return threadPool.dispatch(runnable);
    }

    public void join() throws InterruptedException {
        threadPool.join();
    }

    public int getThreads() {
        return threadPool.getThreads();
    }

    public int getIdleThreads() {
        return threadPool.getIdleThreads();
    }

    public boolean isLowOnThreads() {
        return threadPool.isLowOnThreads();
    }

    public void start() throws Exception {
        synchronized (threadPoolLock) {
            // start the first one
            if (++threadPoolCount == 1) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Starting thread pool [" + threadPool + "]");
                }
                ((LifeCycle) threadPool).start();
            }
        }
    }

    public void stop() throws Exception {
        synchronized (threadPoolLock) {
            // start the first one
            if (--threadPoolCount == 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Stopping thread pool [" + threadPool + "]");
                }
                ((LifeCycle) threadPool).stop();
            }
        }
    }

    public boolean isRunning() {
        return ((LifeCycle) threadPool).isRunning();
    }

    public boolean isStarted() {
        return ((LifeCycle) threadPool).isStarted();
    }

    public boolean isStarting() {
        return ((LifeCycle) threadPool).isStarting();
    }

    public boolean isStopping() {
        return ((LifeCycle) threadPool).isStopping();
    }

    public boolean isStopped() {
        return ((LifeCycle) threadPool).isStopped();
    }

    public boolean isFailed() {
        return ((LifeCycle) threadPool).isFailed();
    }

    public void addLifeCycleListener(Listener listener) {
        ((LifeCycle) threadPool).addLifeCycleListener(listener);
    }

    public void removeLifeCycleListener(Listener listener) {
        ((LifeCycle) threadPool).removeLifeCycleListener(listener);
    }

    public String toString() {
        return "Shared(" + threadPoolCount + ") [" + threadPool + "]";
    }
}
