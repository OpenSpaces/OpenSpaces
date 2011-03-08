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

package org.openspaces.esb.mule.seda;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkListener;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.config.i18n.CoreMessages;
import org.openspaces.core.SpaceClosedException;
import org.openspaces.core.SpaceInterruptedException;

import com.j_spaces.core.exception.SpaceUnavailableException;

/**
 * A SEDA component using the Space as the queue.  The queue is a virtualized queue represented
 * by the {@link org.openspaces.esb.mule.seda.InternalEventEntry} with the service name set (and
 * not the actual event). This allows for simple failover support inherited by the Space.
 *
 * @author kimchy
 */
public class OpenSpacesSedaService extends SpaceAwareSedaService implements Work, WorkListener {

    private Object template;

    /**
     * For Spring only
     */
    public OpenSpacesSedaService(MuleContext muleContext) {
        super(muleContext);
    }
    
    public synchronized void doInitialise() throws InitialisationException {
        super.doInitialise();

        InternalEventEntry internalTemplate = new InternalEventEntry();
        internalTemplate.setName(name);
        internalTemplate.setFifo(sedaModel.isFifo());
        if (sedaModel.isPersistent()) {
            internalTemplate.makePersistent();
        } else {
            internalTemplate.makeTransient();
        }
        template = gigaSpace.snapshot(internalTemplate);
    }

    protected void enqueue(MuleEvent event) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Component " + name + " putting event on queue " + name + ": " + event);
        }
        InternalEventEntry entry = new InternalEventEntry(event, name);
        entry.setFifo(sedaModel.isFifo());
        if (sedaModel.isPersistent()) {
            entry.makePersistent();
        } else {
            entry.makeTransient();
        }
        gigaSpace.write(entry);
    }

    protected MuleEvent dequeue() throws Exception {
        if (logger.isDebugEnabled()) {
            //logger.debug("Component " + name + " polling queue " + name + ", timeout = " + queueTimeout);
        }
        if (getQueueTimeout() == null) {
            throw new InitialisationException(CoreMessages.noServiceQueueTimeoutSet(this), this);
        } else {
            try {
                InternalEventEntry entry = (InternalEventEntry) gigaSpace.take(template, getQueueTimeout());
                if (entry == null) {
                    return null;
                }
                return entry.getEvent();
            } catch (SpaceInterruptedException e) {
                // the space is begin shutdown, simply return null
                return null;
            } catch (SpaceClosedException e) {
                // the space is begin shutdown, simply return null
                return null;
            } catch (SpaceUnavailableException e) {
                // the space is begin shutdown, simply return null
                return null;
            }
        }
    }

    public int getQueueSize() {
        return gigaSpace.count(template);
    }

    public void run() {
        // TODO Auto-generated method stub
        
    }

    public void workAccepted(WorkEvent arg0) {
        // TODO Auto-generated method stub
        
    }

    public void workCompleted(WorkEvent arg0) {
        // TODO Auto-generated method stub
        
    }

    public void workRejected(WorkEvent arg0) {
        // TODO Auto-generated method stub
        
    }

    public void workStarted(WorkEvent arg0) {
        // TODO Auto-generated method stub
        
    }

    public void release() {
        // TODO Auto-generated method stub
        
    }
}
