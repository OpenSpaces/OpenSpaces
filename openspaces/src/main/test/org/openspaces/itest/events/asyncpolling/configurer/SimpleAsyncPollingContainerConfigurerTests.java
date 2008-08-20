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

package org.openspaces.itest.events.asyncpolling.configurer;

import junit.framework.TestCase;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.events.SpaceDataEventListener;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.asyncpolling.SimpleAsyncPollingContainerConfigurer;
import org.openspaces.events.asyncpolling.SimpleAsyncPollingEventListenerContainer;
import org.springframework.transaction.TransactionStatus;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author kimchy
 */
public class SimpleAsyncPollingContainerConfigurerTests extends TestCase {


    public void testSimpleConfigurer() throws Exception {
        UrlSpaceConfigurer urlSpaceConfigurerPrimary = new UrlSpaceConfigurer("/./space").lookupGroups(System.getProperty("user.name"));

        final AtomicBoolean eventCalled = new AtomicBoolean();

        GigaSpace gigaSpace = new GigaSpaceConfigurer(urlSpaceConfigurerPrimary.space()).gigaSpace();

        SimpleAsyncPollingEventListenerContainer pollingEventListenerContainer = new SimpleAsyncPollingContainerConfigurer(gigaSpace)
                .template(new TestMessage())
                .eventListenerAnnotation(new Object() {
                    @SpaceDataEvent
                    public void gotMeselfAnEvent() {
                        eventCalled.set(true);
                    }
                }).pollingContainer();
        gigaSpace.write(new TestMessage("test"));
        Thread.sleep(500);
        assertTrue(eventCalled.get());

        pollingEventListenerContainer.destroy();
        urlSpaceConfigurerPrimary.destroy();
    }

    public void testSimplePrimaryBackup() throws Exception {
        final AtomicBoolean primaryEventCalled = new AtomicBoolean();
        final AtomicBoolean backupEventCalled = new AtomicBoolean();

        UrlSpaceConfigurer urlSpaceConfigurerPrimary = new UrlSpaceConfigurer("/./spaceEventConfigurer")
                .clusterInfo(new ClusterInfo("primary_backup", 1, null, 1, 1)).lookupGroups(System.getProperty("user.name"));

        UrlSpaceConfigurer urlSpaceConfigurerBackup = new UrlSpaceConfigurer("/./spaceEventConfigurer")
                .clusterInfo(new ClusterInfo("primary_backup", 1, 1, 1, 1)).lookupGroups(System.getProperty("user.name"));

        GigaSpace gigaSpacePrimary = new GigaSpaceConfigurer(urlSpaceConfigurerPrimary.space()).gigaSpace();
        SimpleAsyncPollingEventListenerContainer pollingEventListenerContainerPrimary = new SimpleAsyncPollingContainerConfigurer(gigaSpacePrimary)
                .template(new TestMessage())
                .eventListener(new SpaceDataEventListener() {
                    public void onEvent(Object data, GigaSpace gigaSpace, TransactionStatus txStatus, Object source) {
                        primaryEventCalled.set(true);
                    }
                }).pollingContainer();

        GigaSpace gigaSpaceBackup = new GigaSpaceConfigurer(urlSpaceConfigurerBackup.space()).gigaSpace();
        SimpleAsyncPollingEventListenerContainer pollingEventListenerContainerBackup = new SimpleAsyncPollingContainerConfigurer(gigaSpaceBackup)
                .template(new TestMessage())
                .eventListener(new SpaceDataEventListener() {
                    public void onEvent(Object data, GigaSpace gigaSpace, TransactionStatus txStatus, Object source) {
                        backupEventCalled.set(true);
                    }
                }).pollingContainer();

        UrlSpaceConfigurer urlSpaceConfigurerClient = new UrlSpaceConfigurer("jini://*/*/spaceEventConfigurer").lookupGroups(System.getProperty("user.name"));
        urlSpaceConfigurerClient.space().write(new TestMessage("test"), null, 50000);

        Thread.sleep(2000);
        assertTrue(primaryEventCalled.get());
        assertFalse(backupEventCalled.get());


        pollingEventListenerContainerPrimary.destroy();
        pollingEventListenerContainerBackup.destroy();
        urlSpaceConfigurerPrimary.destroy();
    }
}