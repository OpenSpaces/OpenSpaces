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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gateway.GatewayProcessingUnit;
import org.openspaces.admin.gateway.events.DefaultGatewayProcessingUnitAddedEventManager;
import org.openspaces.admin.gateway.events.DefaultGatewayProcessingUnitRemovedEventManager;
import org.openspaces.admin.gateway.events.GatewayProcessingUnitAddedEventListener;
import org.openspaces.admin.gateway.events.GatewayProcessingUnitAddedEventManager;
import org.openspaces.admin.gateway.events.GatewayProcessingUnitLifecycleEventListener;
import org.openspaces.admin.gateway.events.GatewayProcessingUnitRemovedEventManager;
import org.openspaces.admin.gateway.events.InternalGatewayProcessingUnitAddedEventManager;
import org.openspaces.admin.gateway.events.InternalGatewayProcessingUnitRemovedEventManager;
import org.openspaces.admin.internal.admin.DefaultAdmin;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.pu.ProcessingUnit;

import com.j_spaces.kernel.SizeConcurrentHashMap;

/**
 * @author evgeny
 * @since 9.5
 */
public class DefaultGatewayProcessingUnits implements InternalGatewayProcessingUnits {

    private final InternalAdmin admin;
    
    //key is uid of GatewayProcessingUnit
    private final Map<String, GatewayProcessingUnit> gatewayProcessingUnits = new SizeConcurrentHashMap<String, GatewayProcessingUnit>();

    private final InternalGatewayProcessingUnitAddedEventManager gatewayProcessingUnitAddedEventManager;

    private final InternalGatewayProcessingUnitRemovedEventManager gatewayProcessingUnitRemovedEventManager;
    

    public DefaultGatewayProcessingUnits(DefaultAdmin admin) {
        this.admin = admin;
        this.gatewayProcessingUnitAddedEventManager = new DefaultGatewayProcessingUnitAddedEventManager(this);
        this.gatewayProcessingUnitRemovedEventManager = new DefaultGatewayProcessingUnitRemovedEventManager(this);
    }
    
    @Override
    public void addGatewayProcessingUnit(final GatewayProcessingUnit gatewayProcessingUnit) {
        assertStateChangesPermitted();
        String uid = gatewayProcessingUnit.getUid();
        GatewayProcessingUnit existingGatewayProcessingUnit = 
        		gatewayProcessingUnits.put( uid, gatewayProcessingUnit );
        if( existingGatewayProcessingUnit == null ) {
            gatewayProcessingUnitAddedEventManager.gatewayProcessingUnitAdded( gatewayProcessingUnit );
        }
    }
    
    @Override
    public GatewayProcessingUnit removeGatewayProcessingUnit( String uid ) {
        assertStateChangesPermitted();
        final GatewayProcessingUnit existingGatewayProcessingUnit = gatewayProcessingUnits.remove( uid );
        if (existingGatewayProcessingUnit != null) {
            gatewayProcessingUnitRemovedEventManager.gatewayProcessingUnitRemoved(existingGatewayProcessingUnit);
        }
        
        return existingGatewayProcessingUnit;
    }    
    
    private void assertStateChangesPermitted() {
        admin.assertStateChangesPermitted();
    }    
    
    @Override
    public Admin getAdmin() {
        return admin;
    }

    @Override
    public int getSize() {
        return getGatewayProcessingUnits().length;
    }

    @Override
    public boolean isEmpty() {
        return getSize() == 0;
    }

	@Override
	public GatewayProcessingUnit[] getGatewayProcessingUnits() {
		return gatewayProcessingUnits.values().toArray( 
							new GatewayProcessingUnit[ gatewayProcessingUnits.size() ] );
	}

	@Override
	public GatewayProcessingUnit getGatewayProcessingUnit(String uid) {
		return gatewayProcessingUnits.get(uid);
	}

	@Override
	public GatewayProcessingUnitAddedEventManager getGatewayProcessingUnitAdded() {
		return gatewayProcessingUnitAddedEventManager;
	}

	@Override
	public GatewayProcessingUnitRemovedEventManager getGatewayProcessingUnitRemoved() {
		return gatewayProcessingUnitRemovedEventManager;
	}

	@Override
	public void addLifecycleListener( GatewayProcessingUnitLifecycleEventListener eventListener ) {

        getGatewayProcessingUnitAdded().add(eventListener);
        getGatewayProcessingUnitRemoved().add(eventListener);
	}

	@Override
	public void removeLifecycleListener( GatewayProcessingUnitLifecycleEventListener eventListener ) {
		
        getGatewayProcessingUnitAdded().remove(eventListener);
        getGatewayProcessingUnitRemoved().remove(eventListener);
	}

	@Override
	public Iterator<GatewayProcessingUnit> iterator() {

		return Collections.unmodifiableCollection(gatewayProcessingUnits.values()).iterator();
	}

	@Override
	public Map<String, GatewayProcessingUnit> getNames() {

		Collection<GatewayProcessingUnit> values = gatewayProcessingUnits.values();
		Map<String, GatewayProcessingUnit> resultMap = new HashMap<String, GatewayProcessingUnit>();
		for( GatewayProcessingUnit gatewayProcessingUnit : values ){
			resultMap.put( retrieveGatewayProcessingUnitName( gatewayProcessingUnit ), gatewayProcessingUnit );
		}
		
		return resultMap;
	}

	@Override
	public GatewayProcessingUnit waitFor(String gatewayProcessingUnitName) {

		return waitFor(gatewayProcessingUnitName, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
	}

	@Override
	public GatewayProcessingUnit waitFor(final String gatewaysProcessingUnitName,
			long timeout, TimeUnit timeUnit) {

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<GatewayProcessingUnit> ref = new AtomicReference<GatewayProcessingUnit>();
        GatewayProcessingUnitAddedEventListener added = new GatewayProcessingUnitAddedEventListener() {
        	@Override
            public void gatewayProcessingUnitAdded(GatewayProcessingUnit gatewayProcessingUnit) {
        		String name = retrieveGatewayProcessingUnitName( gatewayProcessingUnit );
                if( gatewaysProcessingUnitName.equals( name ) ) {
                    ref.set(gatewayProcessingUnit);
                    latch.countDown();
                }
            }
        };
        getGatewayProcessingUnitAdded().add(added);
        try {
            latch.await(timeout, timeUnit);
            return ref.get();
        } catch (InterruptedException e) {
            return null;
        } finally {
        	getGatewayProcessingUnitAdded().remove(added);
        }
	}
	
    private String retrieveGatewayProcessingUnitName( GatewayProcessingUnit gatewayProcessingUnit ){
    	ProcessingUnit processingUnit = gatewayProcessingUnit.getProcessingUnit();
    	return processingUnit.getName();
    }	
}