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
package org.openspaces.grid.gsm.strategy;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jini.rio.monitor.event.EventsStore;
import org.openspaces.admin.internal.pu.elastic.events.InternalElasticProcessingUnitDecisionEvent;
import org.openspaces.admin.internal.pu.elastic.events.InternalElasticProcessingUnitFailureEvent;
import org.openspaces.admin.internal.pu.elastic.events.InternalElasticProcessingUnitProgressChangedEvent;
import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitDecisionEvent;
import org.openspaces.grid.gsm.SingleThreadedPollingLog;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementDecision;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementInProgressException;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementLogStackTrace;

import com.gigaspaces.logger.GSSimpleFormatter;

/**
 * Triggers events based on exceptions raised by {@link AbstractScaleStrategyBean}
 * @author itaif
 */

public class ScaleStrategyProgressEventState {
    
    private boolean inProgressEventRaised;
    private boolean completedEventRaised;
    private SlaEnforcementInProgressException lastInProgressException;
    private final EventsStore eventStore;
    private final boolean isUndeploying;
    private final String processingUnitName;

    private final Class<? extends InternalElasticProcessingUnitProgressChangedEvent> progressChangedEventClass;
    
    private final Log logger = LogFactory.getLog(ScaleStrategyProgressEventState.class);
    private final Log filteredLogger= new SingleThreadedPollingLog(logger);
    
    public ScaleStrategyProgressEventState(
            EventsStore eventStore, 
            boolean isUndeploying, 
            String processingUnitName,
            Class<? extends InternalElasticProcessingUnitProgressChangedEvent> progressChangedEventClass) {
        
        this.eventStore = eventStore;
        this.isUndeploying = isUndeploying;
        this.processingUnitName = processingUnitName;
        this.progressChangedEventClass = progressChangedEventClass;
    }
    
    public void enqueuProvisioningInProgressEvent(SlaEnforcementInProgressException e) {
        
        if (e instanceof SlaEnforcementDecision) {
            
            if (this.lastInProgressException == null || !this.lastInProgressException.equals(e)) {
                this.lastInProgressException = e;
            
                InternalElasticProcessingUnitDecisionEvent event = createDecisionEvent((SlaEnforcementDecision)e);
                enqueuProvisioningInProgressEvent(event,e);
            }
            else {
                enqueuProvisioningInProgressEvent();
            }
        }
        else {
            enqueuProvisioningInProgressEvent();
            if (e instanceof SlaEnforcementFailure) {
                if (this.lastInProgressException == null || !this.lastInProgressException.equals(e)) {
                    this.lastInProgressException = e;
                
                    InternalElasticProcessingUnitFailureEvent event = createFailureEvent((SlaEnforcementFailure)e);
                    
                    if (logger.isWarnEnabled()) {
                        StringBuilder message = new StringBuilder();
                        appendPuPrefix(message, event.getProcessingUnitNames());
                        message.append(event.getFailureDescription());
                        if (e instanceof SlaEnforcementLogStackTrace) {
                            appendStackTrace(message, e);
                            logger.warn(message);
                        }
                        else {
                            logger.warn(message,e);
                        }
                    }
                    eventStore.addEvent(event);
                }
            }
            else if (logger.isDebugEnabled()) {
                StringBuilder message = new StringBuilder();
                appendPuPrefix(message, e.getAffectedProcessingUnits());
                message.append("SLA in progress");
                if (e instanceof SlaEnforcementLogStackTrace) {
                    appendStackTrace(message, e);
                    filteredLogger.debug(message);
                }
                else {
                    filteredLogger.debug(message, e);
                }
            }
        }
    }

    private void enqueuProvisioningInProgressEvent(
            InternalElasticProcessingUnitProgressChangedEvent event,
            SlaEnforcementInProgressException e) {
        
        if (event instanceof ElasticProcessingUnitDecisionEvent) {
            if (logger.isInfoEnabled()) {
                StringBuilder message = new StringBuilder();
                appendPuPrefix(message, new String[] {event.getProcessingUnitName()});
                message.append(((ElasticProcessingUnitDecisionEvent)event).getDecisionDescription());
                if (e == null) {
                    logger.info(message);
                }
                else if (e instanceof SlaEnforcementLogStackTrace) {
                    appendStackTrace(message, e);
                    logger.info(message);
                }
                else {
                    logger.info(message,e);
                }
            }
        }
        
        if (!this.inProgressEventRaised) {
            this.completedEventRaised = false;
            this.inProgressEventRaised = true;
            
            eventStore.addEvent(event);
         
            if (logger.isDebugEnabled()) {
                logger.debug("Added event: " + event.getClass() + " processingUnit=" + processingUnitName + " isComplete=false isUndeploying="+isUndeploying);
            }
        }
        else {
            if (logger.isTraceEnabled()) {
                logger.trace("Ignoring event: " + progressChangedEventClass + " processingUnit=" + processingUnitName + " isComplete=false isUndeploying="+isUndeploying);
            }
        }
        
    }

    public void enqueuProvisioningInProgressEvent() {
        
        final boolean isComplete = false;
        InternalElasticProcessingUnitProgressChangedEvent event = createProgressChangedEvent(isComplete);
        enqueuProvisioningInProgressEvent(event);
    }
    
    public void enqueuProvisioningInProgressEvent(InternalElasticProcessingUnitProgressChangedEvent event) {
        enqueuProvisioningInProgressEvent(event,null);
    }

    public void enqueuProvisioningCompletedEvent() {
        if (!this.completedEventRaised) {
            this.completedEventRaised = true;
            this.inProgressEventRaised = false;
            this.lastInProgressException = null;
            
            boolean isComplete = true;
            InternalElasticProcessingUnitProgressChangedEvent event = createProgressChangedEvent(isComplete);
            eventStore.addEvent(event);
            if (logger.isDebugEnabled()) {
                logger.debug("Added event: " + event.getClass() + " processingUnit=" + processingUnitName + " isComplete=true isUndeploying="+isUndeploying);
            }
        }
        else {
            if (logger.isTraceEnabled()) {
                logger.trace("Ignoring event: " + progressChangedEventClass + " processingUnit=" + processingUnitName + " isComplete=true isUndeploying="+isUndeploying);
            }
        }
    }

    private InternalElasticProcessingUnitProgressChangedEvent createProgressChangedEvent(
            boolean isComplete) {
        
        InternalElasticProcessingUnitProgressChangedEvent newEvent;
        try {
            newEvent = progressChangedEventClass.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("failed creating new event",e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("failed creating new event",e);
        }
        newEvent.setComplete(isComplete);
        newEvent.setUndeploying(isUndeploying);
        newEvent.setProcessingUnitName(processingUnitName);
        return newEvent;
    }
    
    private InternalElasticProcessingUnitFailureEvent createFailureEvent(SlaEnforcementFailure e) {
        return e.toEvent();
    }
    
    private InternalElasticProcessingUnitDecisionEvent createDecisionEvent(SlaEnforcementDecision e) {
        InternalElasticProcessingUnitDecisionEvent newEvent=  e.toEvent();
        newEvent.setUndeploying(isUndeploying);
        return newEvent;
    }
    
    /**
     * copied from {@link GSSimpleFormatter#format(java.util.logging.LogRecord)}
     */
    private void appendStackTrace(StringBuilder message, Throwable e) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.close();
            message.append("; Caused by: " ).append(sw.toString());
        } catch (Exception ex)
        {
            message.append("; Caused by: " ).append( e.toString());
            message.append(" - Unable to parse stack trace; Caught: ").append( ex);
        }
    }
    
    private void appendPuPrefix(StringBuilder message, String[] puNames) {
        message.append(Arrays.asList(puNames).toString()).append(" ");
    }
}
