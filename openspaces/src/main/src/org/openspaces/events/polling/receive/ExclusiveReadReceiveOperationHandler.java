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

package org.openspaces.events.polling.receive;

import com.j_spaces.core.client.ReadModifiers;
import org.openspaces.core.GigaSpace;
import org.springframework.dao.DataAccessException;

/**
 * Performs single read operation using {@link org.openspaces.core.GigaSpace#read(Object,long)}
 * under an exclusive read lock. This receive operation handler allows to lock entries so other
 * receive operations won't be able to obtain it (mimics the take operation) but without actually
 * performing a take from the Space.
 *
 * Note, this receive operation handler must be performed under a transaction.
 *
 * @author kimchy
 */
public class ExclusiveReadReceiveOperationHandler extends AbstractNonBlockingReceiveOperationHandler {

    /**
     * Performs single read operation using {@link org.openspaces.core.GigaSpace#read(Object,long)}
     * under an exclusive read lock. This receive operation handler allows to lock entries so other
     * receive operations won't be able to obtain it (mimics the take operation) but without actually
     * performing a take from the Space.
     *
     * Note, this receive operation handler must be performed under a transaction.
     */
    protected Object doReceiveBlocking(Object template, GigaSpace gigaSpace, long receiveTimeout) throws DataAccessException {
        return gigaSpace.read(template, receiveTimeout, gigaSpace.getModifiersForIsolationLevel() | ReadModifiers.EXCLUSIVE_READ_LOCK);
    }

    /**
     * Performs single read operation using {@link org.openspaces.core.GigaSpace#read(Object,long)}
     * under an exclusive read lock with no timeout. This receive operation handler allows to lock entries so other
     * receive operations won't be able to obtain it (mimics the take operation) but without actually
     * performing a take from the Space.
     *
     * Note, this receive operation handler must be performed under a transaction.
     */
    protected Object doReceiveNonBlocking(Object template, GigaSpace gigaSpace) throws DataAccessException {
        return gigaSpace.read(template, 0, gigaSpace.getModifiersForIsolationLevel() | ReadModifiers.EXCLUSIVE_READ_LOCK);
    }

    @Override
    public String toString() {
        return "Single Exclusive Read, nonBlocking[" + nonBlocking + "], nonBlockingFactor[" + nonBlockingFactor + "]";
    }
}