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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jini.rio.monitor.event.EventsStore;
import org.openspaces.admin.internal.pu.elastic.events.InternalElasticProcessingUnitDecisionEvent;
import org.openspaces.admin.internal.pu.elastic.events.InternalElasticProcessingUnitFailureEvent;
import org.openspaces.admin.internal.pu.elastic.events.InternalElasticProcessingUnitProgressChangedEvent;
import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitDecisionEvent;
import org.openspaces.admin.zone.config.ZonesConfig;
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
    
    public void enqueuProvisioningInProgressEvent(SlaEnforcementInProgressException e, ZonesConfig zones) {
        
        if (e instanceof SlaEnforcementDecision) {
            
            if (this.lastInProgressException == null || !this.lastInProgressException.equals(e)) {
                this.lastInProgressException = e;
            
                InternalElasticProcessingUnitDecisionEvent event = createDecisionEvent((SlaEnforcementDecision)e, zones);
                enqueuProvisioningInProgressEvent(event,e);
            }
            else {
                enqueuDefaultProvisioningInProgressEvent(e, zones);
            }
        }
        else {
            enqueuDefaultProvisioningInProgressEvent(e, zones);
            if (e instanceof SlaEnforcementFailure) {
                if (this.lastInProgressException == null || !this.lastInProgressException.equals(e)) {
                    this.lastInProgressException = e;
                
                    InternalElasticProcessingUnitFailureEvent event = createFailureEvent((SlaEnforcementFailure)e, zones);
                    
                    if (logger.isWarnEnabled()) {
                        StringBuilder message = new StringBuilder();
                        appendPuPrefix(message, event.getProcessingUnitName());
                        String failureDescription = event.getFailureDescription();
                        if (failureDescription == null) {
                            throw new IllegalStateException("event " + event.getClass() + " failure description cannot be null");
                        }
                        message.append(failureDescription);
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
                appendPuPrefix(message, e.getProcessingUnitName());
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
            Throwable t) {
        
        boolean logged = false;
        if (event instanceof ElasticProcessingUnitDecisionEvent) {
            if (logger.isInfoEnabled()) {
                StringBuilder message = new StringBuilder();
                appendPuPrefix(message, event.getProcessingUnitName());
                String decisionDescription = ((ElasticProcessingUnitDecisionEvent)event).getDecisionDescription();
                if (decisionDescription == null) {
                    throw new IllegalStateException("event " + event.getClass() + " decision description cannot be null");
                }
                message.append(decisionDescription);
                if (t == null) {
                    logger.info(message);
                }
                else if (t instanceof SlaEnforcementLogStackTrace) {
                    appendStackTrace(message, t);
                    logger.info(message);
                }
                else {
                    logger.info(message,t);
                }
                logged = true;
            }
        }
        
        if (!this.inProgressEventRaised) {
            this.completedEventRaised = false;
            this.inProgressEventRaised = true;
            
            eventStore.addEvent(event);
            
            if (!logged && logger.isInfoEnabled()) {
                StringBuilder message = new StringBuilder(event.toString());
                if (t instanceof SlaEnforcementLogStackTrace) {
                    appendStackTrace(message, t);
                    logger.info(message);
                }
                else {
                    logger.info(message,t);
                }
            }
        }
        else {
            if (logger.isTraceEnabled()) {
                logger.trace("Ignoring event:" + event.toString(),t);
            }
        }
    }

    public void enqueuDefaultProvisioningInProgressEvent(Throwable t, ZonesConfig zones) {
        
        final boolean isComplete = false;
        InternalElasticProcessingUnitProgressChangedEvent event = createProgressChangedEvent(isComplete, zones);
        enqueuProvisioningInProgressEvent(event, t);
    }
    
    public void enqueuProvisioningInProgressEvent(InternalElasticProcessingUnitProgressChangedEvent event) {
        enqueuProvisioningInProgressEvent(event,null);
    }

    public void enqueuProvisioningCompletedEvent(ZonesConfig zones) {
        
        boolean isComplete = true;
        InternalElasticProcessingUnitProgressChangedEvent event = createProgressChangedEvent(isComplete, zones);
        
        if (!this.completedEventRaised) {
            this.completedEventRaised = true;
            this.inProgressEventRaised = false;
            this.lastInProgressException = null;
            
            eventStore.addEvent(event);
            if (logger.isInfoEnabled()) {
                logger.info(event.toString());
            }
        }
        else {
            if (logger.isTraceEnabled()) {
                logger.trace("Ignoring event: " + event.toString());
            }
        }
    }

    private InternalElasticProcessingUnitProgressChangedEvent createProgressChangedEvent(
            boolean isComplete,
            ZonesConfig zones) {
        
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
        if (zones != null) {
            newEvent.setGridServiceAgentZones(zones);
        }
        return newEvent;
    }
    
    private InternalElasticProcessingUnitFailureEvent createFailureEvent(SlaEnforcementFailure e, ZonesConfig zones) {
        InternalElasticProcessingUnitFailureEvent event = e.toEvent();
        event.setGridServiceAgentZones(zones);
        return event;
    }
    
    private InternalElasticProcessingUnitDecisionEvent createDecisionEvent(SlaEnforcementDecision e, ZonesConfig zones) {
        InternalElasticProcessingUnitDecisionEvent newEvent=  e.toEvent();
        newEvent.setComplete(false);
        newEvent.setUndeploying(isUndeploying);
        newEvent.setProcessingUnitName(processingUnitName);
        if (zones != null) {
            newEvent.setGridServiceAgentZones(zones);
        }
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
    
    private void appendPuPrefix(StringBuilder message, String puName) {
        message.append("["+puName+"]").append(" ");
    }
}
