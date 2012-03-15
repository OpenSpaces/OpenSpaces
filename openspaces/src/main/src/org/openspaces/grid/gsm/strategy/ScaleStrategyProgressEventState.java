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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jini.rio.monitor.event.EventsStore;
import org.openspaces.admin.internal.pu.elastic.events.AbstractElasticProcessingUnitFailureEvent;
import org.openspaces.admin.internal.pu.elastic.events.AbstractElasticProcessingUnitProgressChangedEvent;
import org.openspaces.admin.internal.pu.elastic.events.DefaultElasticProcessingUnitScaleProgressChangedEvent;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementInProgressException;

/**
 * Triggers events based on exceptions raised by {@link AbstractScaleStrategyBean}
 * @author itaif
 */

public class ScaleStrategyProgressEventState {
    
    private boolean inProgressEventRaised;
    private boolean completedEventRaised;
    private SlaEnforcementFailure lastFailure;
    private final EventsStore eventStore;
    private final boolean isUndeploying;
    private final String processingUnitName;
    private Class<? extends AbstractElasticProcessingUnitProgressChangedEvent> progressChangedEventClass;
    private Class<? extends AbstractElasticProcessingUnitFailureEvent> failureEventClass;

    private final Log logger = LogFactory.getLog(ScaleStrategyProgressEventState.class);
    
    public ScaleStrategyProgressEventState(
            EventsStore eventStore, 
            boolean isUndeploying, 
            String processingUnitName, 
            Class<? extends AbstractElasticProcessingUnitProgressChangedEvent> progressChangedEventClass,
            Class<? extends AbstractElasticProcessingUnitFailureEvent> failureEventClass) {
        
        this.eventStore = eventStore;
        this.isUndeploying = isUndeploying;
        this.processingUnitName = processingUnitName;
        this.progressChangedEventClass = progressChangedEventClass;
        this.failureEventClass = failureEventClass;
    }
    
    public ScaleStrategyProgressEventState(EventsStore eventStore, boolean isUndeploying,
            String processingUnitName, Class<DefaultElasticProcessingUnitScaleProgressChangedEvent> progressChangedEventClass) {
        this(eventStore, isUndeploying, processingUnitName, progressChangedEventClass, null);
    }

    public void enqueuProvisioningInProgressEvent(SlaEnforcementInProgressException e) {
        enqueuProvisioningInProgressEvent();
        if (e instanceof SlaEnforcementFailure) {
            if (this.lastFailure == null || !this.lastFailure.equals(e)) {
                this.lastFailure = (SlaEnforcementFailure)e;
                
                AbstractElasticProcessingUnitFailureEvent event = createFailureEvent(e);
                eventStore.addEvent(event);
                if (logger.isDebugEnabled()) {
                    logger.debug("Added event: " + failureEventClass + " " + e.getMessage());
                }
            }
            else {
                if (logger.isTraceEnabled()) {
                    logger.trace("Ignoring event: " + failureEventClass + " " + e.getMessage());
                }
            }
        }
    }

    public void enqueuProvisioningInProgressEvent() {
        if (!this.inProgressEventRaised) {
            this.completedEventRaised = false;
            this.inProgressEventRaised = true;
            final boolean isComplete = false;
            
            AbstractElasticProcessingUnitProgressChangedEvent event = createProgressChangedEvent(isComplete);
            eventStore.addEvent(event);
         
            if (logger.isDebugEnabled()) {
                logger.debug("Added event: " + progressChangedEventClass + " processingUnit=" + processingUnitName + " isComplete=false isUndeploying="+isUndeploying);
            }
        }
        else {
            if (logger.isTraceEnabled()) {
                logger.trace("Ignoring event: " + progressChangedEventClass + " processingUnit=" + processingUnitName + " isComplete=false isUndeploying="+isUndeploying);
            }
        }
    }

    public void enqueuProvisioningCompletedEvent() {
        if (!this.completedEventRaised) {
            this.completedEventRaised = true;
            this.inProgressEventRaised = false;
            this.lastFailure = null;
            
            boolean isComplete = true;
            AbstractElasticProcessingUnitProgressChangedEvent event = createProgressChangedEvent(isComplete);
            eventStore.addEvent(event);
            if (logger.isDebugEnabled()) {
                logger.debug("Added event: " + progressChangedEventClass + " processingUnit=" + processingUnitName + " isComplete=true isUndeploying="+isUndeploying);
            }
        }
        else {
            if (logger.isTraceEnabled()) {
                logger.trace("Ignoring event: " + progressChangedEventClass + " processingUnit=" + processingUnitName + " isComplete=true isUndeploying="+isUndeploying);
            }
        }
    }

    private AbstractElasticProcessingUnitProgressChangedEvent createProgressChangedEvent(boolean isComplete) {
        AbstractElasticProcessingUnitProgressChangedEvent newEvent;
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
    
    private AbstractElasticProcessingUnitFailureEvent createFailureEvent(SlaEnforcementInProgressException ex) {
        AbstractElasticProcessingUnitFailureEvent newEvent; 
        try {
            newEvent = failureEventClass.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("failed creating new event",e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("failed creating new event",e);
        }
        newEvent.setFailureDescription(ex.getMessage());
        newEvent.setProcessingUnitNames(((SlaEnforcementFailure)ex).getAffectedProcessingUnits());
        return newEvent;
    }
}
