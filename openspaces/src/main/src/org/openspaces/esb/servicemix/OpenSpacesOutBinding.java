/*
* Copyright 2006-2007 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.openspaces.esb.servicemix;

import com.j_spaces.core.client.UpdateModifiers;
import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;
import org.apache.servicemix.components.util.OutBinding;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import java.util.Arrays;

/**
 * Consumes JBI messages and sends them to a gigaSpaces space.
 *
 * @author yitzhaki
 */
public class OpenSpacesOutBinding extends OutBinding implements InitializingBean {

    private GigaSpace gigaSpace;

    private long writeLease = Lease.FOREVER;

    private boolean updateOrWrite = true;

    private long updateTimeout = JavaSpace.NO_WAIT;

    private OpenSpaceMarshaler marshaler;


    public void afterPropertiesSet() throws Exception {
        if (gigaSpace == null) {
            throw new IllegalArgumentException("Must have a gigaSpace set");
        }
        if (marshaler == null) {
            marshaler = new DefaultOpenSpaceMarshaler();
        }
    }

    /**
     * Receives an InOnly messageExchange, unmarshals it to POJO and writes it to the space.
     * Is capable of handling both a single object and an array of objects. Takes into account the
     * {@link #setUpdateOrWrite(boolean) 'updateOrWrite'} flag when writing/updating the result back
     * to the space.
     *
     * @see org.apache.servicemix.components.util.OutBinding#process(javax.jbi.messaging.MessageExchange, javax.jbi.messaging.NormalizedMessage)
     */
    protected void process(MessageExchange messageExchange, NormalizedMessage message) throws Exception {
        Object pojo = marshaler.fromNMS(messageExchange, message);
        if (pojo != null) {
            if (pojo instanceof Object[]) {
                Object[] payloadArr = (Object[]) pojo;
                if (updateOrWrite) {
                    long[] leases = new long[payloadArr.length];
                    Arrays.fill(leases, writeLease);
                    Object[] retVals = gigaSpace.updateMultiple(payloadArr, leases, UpdateModifiers.UPDATE_OR_WRITE);
                    for (Object retVal : retVals) {
                        if (retVal instanceof DataAccessException) {
                            throw (DataAccessException) retVal;
                        }
                    }
                } else {
                    gigaSpace.writeMultiple(payloadArr, writeLease);
                }
            } else {
                if (updateOrWrite) {
                    gigaSpace.write(pojo, writeLease, updateTimeout, UpdateModifiers.UPDATE_OR_WRITE);
                } else {
                    gigaSpace.write(pojo, writeLease, updateTimeout, UpdateModifiers.WRITE_ONLY);
                }
            }
        }
        done(messageExchange);
    }

    public GigaSpace getGigaSpace() {
        return gigaSpace;
    }

    public void setGigaSpace(GigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
    }

    public long getWriteLease() {
        return writeLease;
    }

    public void setWriteLease(long writeLease) {
        this.writeLease = writeLease;
    }

    public boolean isUpdateOrWrite() {
        return updateOrWrite;
    }

    public void setUpdateOrWrite(boolean updateOrWrite) {
        this.updateOrWrite = updateOrWrite;
    }

    public long getUpdateTimeout() {
        return updateTimeout;
    }

    public void setUpdateTimeout(long updateTimeout) {
        this.updateTimeout = updateTimeout;
    }

} 