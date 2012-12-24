/*******************************************************************************
 * 
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
 *  
 ******************************************************************************/
package org.openspaces.grid.gsm;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;

import com.j_spaces.kernel.time.SystemTime;

/**
 * A log that silently discards some log entries if they have been printed
 * in the past minute already.
 * 
 * This class is not thread safe and is intended for single threaded polling code
 * that keeps printing the same log entries over and over again, and that is not performance critical.
 *  
 * @author itaif
 *
 */
public class SingleThreadedPollingLog implements Log {
   
    private final static long DEFAULT_DISCARD_MILLISECONDS = 60 * 1000; // 1 minute
    
    long lastFlushMilliseconds;
    Set<String> traceHistory = new HashSet<String>();
    Set<String> debugHistory = new HashSet<String>();
    Set<String> infoHistory = new HashSet<String>();
    Set<String> warnHistory = new HashSet<String>();
    Set<String> errorHistory = new HashSet<String>();
    Set<String> fatalHistory = new HashSet<String>();
    
    private final long discardPeriodMilliseconds;
    private final Log logger;

    public SingleThreadedPollingLog(Log logger) {
        this(logger, DEFAULT_DISCARD_MILLISECONDS, TimeUnit.MILLISECONDS);
    }
    
    public SingleThreadedPollingLog(Log logger, long discardSameLogEntriesPeriod , TimeUnit unit) {
        this.discardPeriodMilliseconds=TimeUnit.MILLISECONDS.convert(discardSameLogEntriesPeriod, unit);
        this.logger = logger;
    }

    public void debug(Object message) {
        flushHistory();
        String string = message.toString();
        if (string == null || string.length() == 0 || !debugHistory.contains(string)) {
            debugHistory.add(string);
            logger.debug(message);
        }
    }

    public void debug(Object message, Throwable t) {
        flushHistory();
        String string = message.toString();
        if (string == null || string.length() == 0 || !debugHistory.contains(string)) {
            debugHistory.add(string);
            logger.debug(message,t);
        }
    }

    public void error(Object message) {
        flushHistory();
        String string = message.toString();
        if (string == null || string.length() == 0 || !errorHistory.contains(string)) {
            errorHistory.add(string);
            logger.error(message);
        }
    }

    public void error(Object message, Throwable t) {
        flushHistory();
        String string = message.toString();
        if (string == null || string.length() == 0 || !errorHistory.contains(string)) {
            errorHistory.add(string);
            logger.error(message,t);
        }
    }

    public void fatal(Object message) {
        flushHistory();
        String string = message.toString();
        if (string == null || string.length() == 0 || !fatalHistory.contains(string)) {
            fatalHistory.add(string);
            logger.fatal(message);
        }
    }

    public void fatal(Object message, Throwable t) {
        flushHistory();
        String string = message.toString();
        if (string == null || string.length() == 0 || !fatalHistory.contains(string)) {
            fatalHistory.add(string);
            logger.fatal(message,t);
        }
    }

    public void info(Object message) {
        flushHistory();
        String string = message.toString();
        if (string == null || string.length() == 0 || !infoHistory.contains(string)) {
            infoHistory.add(string);
            logger.info(message);
        }
    }

    public void info(Object message, Throwable t) {
        flushHistory();
        String string = message.toString();
        if (string == null || string.length() == 0 || !infoHistory.contains(string)) {
            infoHistory.add(string);
            logger.info(message,t);
        }
    }

    public void trace(Object message) {
        flushHistory();
        String string = message.toString();
        if (string == null || string.length() == 0 || !traceHistory.contains(string)) {
            traceHistory.add(string);
            logger.trace(message);
        }
    }

    public void trace(Object message, Throwable t) {
        flushHistory();
        String string = message.toString();
        if (string == null || string.length() == 0 || !traceHistory.contains(string)) {
            traceHistory.add(string);
            logger.trace(message,t);
        }
    }

    public void warn(Object message) {
        flushHistory();
        String string = message.toString();
        if (string == null || string.length() == 0 || !warnHistory.contains(string)) {
            warnHistory.add(string);
            logger.warn(message);
        }
    }

    public void warn(Object message, Throwable t) {
        flushHistory();
        String string = message.toString();
        if (string == null || string.length() == 0 || !warnHistory.contains(string)) {
            warnHistory.add(string);
            logger.warn(message,t);
        }
    }

    private void flushHistory() {
        long now = SystemTime.timeMillis();
        if (lastFlushMilliseconds + discardPeriodMilliseconds <= now) {
            lastFlushMilliseconds = now;
            traceHistory.clear();
            debugHistory.clear();
            infoHistory.clear();
            warnHistory.clear();
            errorHistory.clear();
            fatalHistory.clear();
        }
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
