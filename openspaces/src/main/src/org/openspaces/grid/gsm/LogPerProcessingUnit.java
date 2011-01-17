package org.openspaces.grid.gsm;

import org.apache.commons.logging.Log;
import org.openspaces.admin.pu.ProcessingUnit;

public class LogPerProcessingUnit implements Log {

    private final String prefix;
    private final Log logger;

    public LogPerProcessingUnit(Log logger, ProcessingUnit pu) {
        this.prefix="[" + pu.getName()+"] ";
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
        logger.info(message,t);
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
