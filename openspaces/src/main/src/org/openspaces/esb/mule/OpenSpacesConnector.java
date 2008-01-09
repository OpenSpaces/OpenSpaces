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

package org.openspaces.esb.mule;

import org.mule.providers.AbstractConnector;
import org.mule.umo.UMOException;
import org.mule.umo.lifecycle.InitialisationException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * An OpenSpaces mule conntector holding Spring application context which is later used
 * by the receiver and the dispatcher to lookup regsitered beans within the application
 * context. For example, the dispatcher looks up a <code>GigaSpace</code> instance in order
 * to send the code using it.
 *
 * <p>Note, the conntector must be defined within mule configuration in order for it to be
 * injected with the application context.
 *
 * @author yitzhaki
 */
public class OpenSpacesConnector extends AbstractConnector implements ApplicationContextAware {

    public static final String OS = "os";
    
    private ApplicationContext applicationContext;


    /**
     * @return the openspaces protocol name.
     */
    public String getProtocol() {
        return OS;
    }


    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    protected void doInitialise() throws InitialisationException {
    }

    protected void doDispose() {
    }

    protected void doStart() throws UMOException {
    }

    protected void doStop() throws UMOException {
    }

    protected void doConnect() throws Exception {
    }

    protected void doDisconnect() throws Exception {
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
