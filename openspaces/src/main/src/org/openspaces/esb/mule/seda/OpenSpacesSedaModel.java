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

import org.mule.impl.model.seda.SedaModel;
import org.mule.umo.lifecycle.InitialisationException;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author kimchy
 */
public class OpenSpacesSedaModel extends SedaModel implements ApplicationContextAware {

    private String gigaSpaceRef;

    private boolean fifo = false;

    private boolean persistent = false;

    private ApplicationContext applicationContext;

    private GigaSpace gigaSpace;


    /**
     * Returns the model type name. This is a friendly identifier that is used to
     * look up the SPI class for the model
     *
     * @return the model type
     */
    public String getType() {
        return "os-seda";
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setGigaSpace(String gigaSpaceRef) {
        this.gigaSpaceRef = gigaSpaceRef;
    }

    public String getGigaSpace() {
        return gigaSpaceRef;
    }

    public boolean isFifo() {
        return fifo;
    }

    public void setFifo(boolean fifo) {
        this.fifo = fifo;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    public GigaSpace getGigaSpaceObj() {
        return this.gigaSpace;
    }

    public void initialise() throws InitialisationException {
        super.initialise();
        if (gigaSpaceRef == null) {
            String[] beansNames = applicationContext.getBeanNamesForType(GigaSpace.class);
            if (beansNames != null && beansNames.length == 1) {
                gigaSpace = (GigaSpace) applicationContext.getBean(beansNames[0]);
            } else {
                throw new RuntimeException("No GigaSpace ref is configured, and more than one GigaSpace bean is configured");
            }
        } else {
            gigaSpace = (GigaSpace) applicationContext.getBean(gigaSpaceRef);
        }
    }
}
