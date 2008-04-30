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

package org.openspaces.maven.plugin;

import java.lang.reflect.InvocationTargetException;

public class ContainerRunnable implements Runnable {


    private volatile Throwable exception;


    private volatile boolean started = false;


    private String[] args;


    private String containerClassName;


    public ContainerRunnable(String containerClassName, String[] arguments) {
        this.containerClassName = containerClassName;
        this.args = arguments;
    }


    public void run() {
        try {
            // TODO Maybe call this one 
            // GSLogConfigLoader.getLoader();

            Class containerClass = Class.forName(containerClassName, true, Thread.currentThread().getContextClassLoader());
            Object container = containerClass.getMethod("createContainer", new Class[] {String[].class}).invoke(null, new Object[]{args});
            started = true;
            final Thread mainThread = Thread.currentThread();
            while (!mainThread.isInterrupted()) {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException e) {
                    // do nothing, simply exit
                }
            }
            Class puContainerClass = Class.forName("org.openspaces.pu.container.ProcessingUnitContainer", true, Thread.currentThread().getContextClassLoader());
            puContainerClass.getMethod("close", new Class[0]).invoke(container, new Object[0]);
        } catch (InvocationTargetException e) {
            exception = e.getTargetException();
        } catch (Exception e) {
            exception = e;
        } finally {
            started = true;
        }
    }


    public boolean hasStarted() {
        return this.started;
    }


    public Throwable getException() {
        return this.exception;
    }
}