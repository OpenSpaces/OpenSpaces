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
package org.openspaces.pu.container.servicegrid.jmxs;

import java.util.Date;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.openspaces.pu.container.servicegrid.PUServiceBeanImpl;

/**
 * Secured PU JMX MBean implementation
 * 
 * @author Moran Avigdor
 */
public class SecuredPUExtension implements MBeanRegistration, NotificationEmitter, SecuredPUExtensionMBean {

    final PUServiceBeanImpl mbean;

    public SecuredPUExtension(Object mbean) {
        this.mbean = (PUServiceBeanImpl)mbean;
    }

    /*
     * @see javax.management.MBeanRegistration#postDeregister()
     */
    public void postDeregister() {
        mbean.postDeregister();
    }

    /*
     * @see javax.management.MBeanRegistration#postRegister(java.lang.Boolean)
     */
    public void postRegister(Boolean registrationDone) {
        mbean.postRegister(registrationDone);
    }

    /*
     * @see javax.management.MBeanRegistration#preDeregister()
     */
    public void preDeregister() throws Exception {
        mbean.preDeregister();
    }

    /*
     * @see javax.management.MBeanRegistration#preRegister(javax.management.MBeanServer, javax.management.ObjectName)
     */
    public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
        return mbean.preRegister(server, name);
    }

    /*
     * @see javax.management.NotificationBroadcaster#addNotificationListener(javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
     */
    public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
    throws IllegalArgumentException {
        mbean.addNotificationListener(listener, filter, handback);
    }

    /*
     * @see javax.management.NotificationBroadcaster#getNotificationInfo()
     */
    public MBeanNotificationInfo[] getNotificationInfo() {
        return mbean.getNotificationInfo();
    }

    /*
     * @see javax.management.NotificationBroadcaster#removeNotificationListener(javax.management.NotificationListener)
     */
    public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
        mbean.removeNotificationListener(listener);
    }


    /*
     * @see javax.management.NotificationEmitter#removeNotificationListener(javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
     */
    public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
    throws ListenerNotFoundException {
        mbean.removeNotificationListener(listener, filter, handback);
    }

    /*
     * @see com.gigaspaces.grid.security.gsa.SecuredGSAExtensionMBean#getLookupGroups()
     */
    public String[] getLookupGroups() {
        return mbean.getLookupGroups();
    }

    /*
     * @see com.gigaspaces.grid.security.gsa.SecuredGSAExtensionMBean#getStarted()
     */
    public Date getStarted() {
        return mbean.getStarted();
    }
}
