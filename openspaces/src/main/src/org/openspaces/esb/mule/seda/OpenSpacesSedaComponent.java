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

import org.mule.config.i18n.CoreMessages;
import org.mule.umo.UMOEvent;
import org.mule.umo.lifecycle.InitialisationException;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkListener;

/**
 * A SEDA component using the Space as the queue.  The queue is a virtualized queue represented
 * by the {@link org.openspaces.esb.mule.seda.InternalEventEntry} with the service name set (and
 * not the actual event). This allows for simple failover support inherited by the Space.
 *
 * @author kimchy
 */
public class OpenSpacesSedaComponent extends SpaceAwareSedaComponent implements Work, WorkListener {

    private Object template;

    /**
     * For Spring only
     */
    public OpenSpacesSedaComponent() {
        super();
    }

    public synchronized void doInitialise() throws InitialisationException {
        super.doInitialise();

        InternalEventEntry internalTemplate = new InternalEventEntry();
        internalTemplate.name = name;
        internalTemplate.setFifo(sedaModel.isFifo());
        if (sedaModel.isPersistent()) {
            internalTemplate.makePersistent();
        } else {
            internalTemplate.makeTransient();
        }
        template = gigaSpace.snapshot(internalTemplate);
    }

    protected void enqueue(UMOEvent event) throws Exception {
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

    protected UMOEvent dequeue() throws Exception {
        if (logger.isDebugEnabled()) {
            //logger.debug("Component " + name + " polling queue " + name + ", timeout = " + queueTimeout);
        }
        if (getQueueTimeout() == null) {
            throw new InitialisationException(CoreMessages.noComponentQueueTimeoutSet(this), this);
        } else {
            InternalEventEntry entry = (InternalEventEntry) gigaSpace.take(template, getQueueTimeout());
            if (entry == null) {
                return null;
            }
            return entry.event;
        }
    }

    public int getQueueSize() {
        return gigaSpace.count(template);
    }
}
