package org.openspaces.grid.gsm.strategy;

import org.openspaces.admin.internal.pu.elastic.events.AbstractElasticProcessingUnitFailureEvent;
import org.openspaces.admin.internal.pu.elastic.events.AbstractElasticProcessingUnitProgressChangedEvent;
import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitProgressChangedEvent;
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
    private final ElasticScaleStrategyEventStorage eventStorage;
    private final boolean isUndeploying;
    private final String processingUnitName;
    private Class<? extends AbstractElasticProcessingUnitProgressChangedEvent> progressChangedEventClass;
    private Class<? extends AbstractElasticProcessingUnitFailureEvent> failureEventClass;
    
    public ScaleStrategyProgressEventState(
            ElasticScaleStrategyEventStorage eventStorage, 
            boolean isUndeploying, 
            String processingUnitName, 
            Class<? extends AbstractElasticProcessingUnitProgressChangedEvent> progressChangedEventClass,
            Class<? extends AbstractElasticProcessingUnitFailureEvent> failureEventClass) {
        
        this.eventStorage = eventStorage;
        this.isUndeploying = isUndeploying;
        this.processingUnitName = processingUnitName;
        this.progressChangedEventClass = progressChangedEventClass;
        this.failureEventClass = failureEventClass;
    }
    
    public void enqueuProvisioningInProgressEvent(SlaEnforcementInProgressException e) {
        if (!this.inProgressEventRaised) {
            this.completedEventRaised = false;
            this.inProgressEventRaised = true;
            final boolean isComplete = false;
            
            AbstractElasticProcessingUnitProgressChangedEvent event = createProgressChangedEvent(isComplete);
            eventStorage.enqueu(event);
        }
        if (e instanceof SlaEnforcementFailure) {
            if (this.lastFailure == null || !this.lastFailure.equals(e)) {
                this.lastFailure = (SlaEnforcementFailure)e;
                
                AbstractElasticProcessingUnitFailureEvent event = createFailureEvent(e);
                eventStorage.enqueu(event);
            }
        }
    }

    public void enqueuProvisioningCompletedEvent() {
        if (!this.completedEventRaised) {
            this.completedEventRaised = true;
            this.inProgressEventRaised = false;
            this.lastFailure = null;
            
            boolean isComplete = true;
            ElasticProcessingUnitProgressChangedEvent event = createProgressChangedEvent(isComplete);
            eventStorage.enqueu(event);
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
