/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.openspaces.grid.gsm;

import org.apache.commons.logging.Log;

/**
 * @author itaif
 * @since 9.0.1
 */
public class PrefixLog implements Log {

    protected final String prefix;
    protected final Log logger;

    public PrefixLog(String prefix, Log logger) {
        this.prefix = prefix;
        this.logger = logger;
    }

    public void debug(Object message) {
        logger.debug(prefix+message);
    }

    public void debug(Object message, Throwable t) {
        logger.debug(prefix+message,t);
    }

    public void error(Object message) {
        logger.error(prefix+message);        
    }

    public void error(Object message, Throwable t) {
        logger.error(prefix+message,t);
    }

    public void fatal(Object message) {
        logger.fatal(prefix+message);
    }

    public void fatal(Object message, Throwable t) {
        logger.fatal(prefix+message,t);
    }

    public void info(Object message) {
        logger.info(prefix+message);
    }

    public void info(Object message, Throwable t) {
        logger.info(prefix+message,t);
    }

    public void trace(Object message) {
        logger.trace(prefix+message);
    }

    public void trace(Object message, Throwable t) {
        logger.trace(prefix+message,t);
    }

    public void warn(Object message) {
        logger.warn(prefix+message);
    }

    public void warn(Object message, Throwable t) {
        logger.warn(prefix+message,t);
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    public boolean isFatalEnabled() {
        return logger.isFatalEnabled();
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

}