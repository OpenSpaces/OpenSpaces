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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gateway.Gateway;
import org.openspaces.admin.gateway.Gateways;
import org.openspaces.admin.internal.admin.DefaultAdmin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener;
import org.openspaces.core.gateway.GatewayUtils;

/**
 * 
 * @author eitany
 * @since 8.0.4
 */
public class DefaultGateways implements Gateways {

    private final DefaultAdmin admin;

    public DefaultGateways(DefaultAdmin admin) {
        this.admin = admin;
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
        List<Gateway> gateways = new LinkedList<Gateway>();
        for (ProcessingUnit processingUnit : admin.getProcessingUnits()) {
            String gatewayName = GatewayUtils.extractGatewayNameIfExists(processingUnit);
            if (gatewayName != null)
                gateways.add(new DefaultGateway(admin, gatewayName));
        }
        return gateways.toArray(new Gateway[gateways.size()]);
    }

    @Override
    public Gateway getGateway(String gatewayName) {
        for (ProcessingUnit processingUnit : admin.getProcessingUnits()) {
            for (ProcessingUnitInstance processingUnitInstance : processingUnit) {
              if (GatewayUtils.isPuInstanceOfGateway(gatewayName, processingUnitInstance))
                  return new DefaultGateway(admin, gatewayName);
            } 
        }
        return null;
    }

    @Override
    public Map<String, Gateway> getNames() {
        Map<String, Gateway> names = new HashMap<String, Gateway>();
        for (Gateway gateway : this) {
            names.put(gateway.getName(), gateway);
        }
        return names;
    }

    @Override
    public Gateway waitFor(String gatewayName) {
        return waitFor(gatewayName, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    @Override
    public Gateway waitFor(final String gatewayName, long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(1);
        ProcessingUnitInstanceAddedEventListener added = new ProcessingUnitInstanceAddedEventListener() {
            
            @Override
            public void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance) {
                if (GatewayUtils.isPuInstanceOfGateway(gatewayName, processingUnitInstance))
                    latch.countDown();
            }
        };
        
        admin.getProcessingUnits().getProcessingUnitInstanceAdded().add(added);
        try {
            if (latch.await(timeout, timeUnit))
                return new DefaultGateway(admin, gatewayName);
            return null;
        } catch (InterruptedException e) {
            return null;
        } finally {
            admin.getProcessingUnits().getProcessingUnitInstanceAdded().remove(added);
        }
    }

    @Override
    public int getSize() {
        return getGateways().length;
    }

    @Override
    public boolean isEmpty() {
        return getSize() == 0;
    }

}
