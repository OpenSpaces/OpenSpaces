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
package org.openspaces.admin.internal.gateway;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gateway.Gateway;
import org.openspaces.admin.gateway.InternalGateways;
import org.openspaces.admin.gateway.events.DefaultGatewayAddedEventManager;
import org.openspaces.admin.gateway.events.DefaultGatewayRemovedEventManager;
import org.openspaces.admin.gateway.events.GatewayAddedEventListener;
import org.openspaces.admin.gateway.events.GatewayAddedEventManager;
import org.openspaces.admin.gateway.events.GatewayLifecycleEventListener;
import org.openspaces.admin.gateway.events.GatewayRemovedEventManager;
import org.openspaces.admin.gateway.events.InternalGatewayAddedEventManager;
import org.openspaces.admin.gateway.events.InternalGatewayRemovedEventManager;
import org.openspaces.admin.internal.admin.DefaultAdmin;

import com.j_spaces.kernel.SizeConcurrentHashMap;

/**
 * 
 * @author eitany
 * @since 8.0.4
 */
public class DefaultGateways implements InternalGateways {

    private final DefaultAdmin admin;
    
    private final Map<String, Gateway> gateways = new SizeConcurrentHashMap<String, Gateway>();

    private final InternalGatewayAddedEventManager gatewayAddedEventManager;

    private final InternalGatewayRemovedEventManager gatewayRemovedEventManager;    

    public DefaultGateways(DefaultAdmin admin) {
        this.admin = admin;
        this.gatewayAddedEventManager = new DefaultGatewayAddedEventManager(this);
        this.gatewayRemovedEventManager = new DefaultGatewayRemovedEventManager(this);        
    }

    @Override
    public Admin getAdmin() {
        return admin;
    }

    @Override
    public Iterator<Gateway> iterator() {
        return Arrays.asList(getGateways()).iterator();
    }

    @Override
    public Gateway[] getGateways() {
		return gateways.values().toArray( new Gateway[ gateways.size() ] );    	
    }

    @Override
    public Gateway getGateway(String gatewayName) {
    	return gateways.get( gatewayName );    	
    }

    @Override
    public Map<String, Gateway> getNames() {
    	return Collections.unmodifiableMap(gateways);
    }

    @Override
    public Gateway waitFor(String gatewayName) {
        return waitFor(gatewayName, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    @Override
    public Gateway waitFor(final String gatewayName, long timeout, TimeUnit timeUnit) {
    	
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Gateway> ref = new AtomicReference<Gateway>();
        GatewayAddedEventListener added = new GatewayAddedEventListener() {
        	@Override
            public void gatewayAdded(Gateway gateway) {
                if (gatewayName.equals(gateway.getName())) {
                    ref.set(gateway);
                    latch.countDown();
                }
            }
        };
        getGatewayAdded().add(added);
        try {
            latch.await(timeout, timeUnit);
            return ref.get();
        } catch (InterruptedException e) {
            return null;
        } finally {
            getGatewayAdded().remove(added);
        }    	
    }

    @Override
    public int getSize() {
        return gateways.size();
    }

    @Override
    public boolean isEmpty() {
        return gateways.isEmpty();
    }

	@Override
	public GatewayAddedEventManager getGatewayAdded() {
		return gatewayAddedEventManager;
	}

	@Override
	public GatewayRemovedEventManager getGatewayRemoved() {
		return gatewayRemovedEventManager;
	}

	@Override
	public void addLifecycleListener(GatewayLifecycleEventListener eventListener) {

        getGatewayAdded().add(eventListener);
        getGatewayRemoved().add(eventListener);
	}

	@Override
	public void removeLifecycleListener( GatewayLifecycleEventListener eventListener ) {
		
        getGatewayAdded().remove(eventListener);
        getGatewayRemoved().remove(eventListener);		
	}

	@Override
	public void addGateway(Gateway gateway) {
        assertStateChangesPermitted();
        String name = gateway.getName();
        Gateway existingGateway = gateways.put( name, gateway );
        if( existingGateway == null ) {
            gatewayAddedEventManager.gatewayAdded( gateway );
        }
	}

	@Override
	public Gateway removeGateway(String name) {

        assertStateChangesPermitted();
        final Gateway existingGateway = gateways.remove( name );
        if (existingGateway != null) {
            gatewayRemovedEventManager.gatewayRemoved( existingGateway );
        }
        return existingGateway;
	}
	
    private void assertStateChangesPermitted() {
        admin.assertStateChangesPermitted();
    }	
}