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

package org.openspaces.pu.container.jee.jetty.support;

import org.eclipse.jetty.util.log.Logger;

import java.util.logging.Level;

/**
 * A JDK logging implemenation of jetty logger.
 *
 * @author kimchy
 */
public class JdkLogger implements Logger {

    private java.util.logging.Logger log;

    public JdkLogger() {
        this("org.eclipse.jetty");
    }

    public JdkLogger(String s) {
        this.log = java.util.logging.Logger.getLogger(s);
    }

    public boolean isDebugEnabled() {
        return log.isLoggable(Level.FINE);
    }

    public void setDebugEnabled(boolean b) {
        // do nothing here
    }

    public Logger getLogger(String s) {
        return new JdkLogger(s);
    }

    public void info(String s) {
        log.info(s);
    }

    public void debug(String s) {
        log.fine(s);
    }

    public void warn(String s) {
        log.warning(s);
    }

    public String getName() {
        return log.getName();
    }

    public void info(String msg, Object arg0, Object arg1) {
        log.log(Level.INFO, msg, new Object[]{arg0, arg1});
    }

    public void debug(String msg, Throwable th) {
        log.log(Level.FINE, msg, th);
    }

    public void debug(String msg, Object arg0, Object arg1) {
        log.log(Level.FINE, msg, new Object[]{arg0, arg1});
    }

    public void warn(String msg, Object arg0, Object arg1) {
        log.log(Level.WARNING, msg, new Object[]{arg0, arg1});
    }

    public void warn(String msg, Throwable th) {
        log.log(Level.WARNING, msg, th);
    }
}
