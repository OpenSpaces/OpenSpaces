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

import org.openspaces.core.GigaSpace;
import org.springframework.dao.DataAccessException;

/**
 * Performs single take operation using {@link org.openspaces.core.GigaSpace#take(Object,long)}.
 *
 * @author kimchy
 */
public class SingleTakeReceiveOperationHandler extends AbstractNonBlockingReceiveOperationHandler {

    /**
     * Performs a single take operation using {@link org.openspaces.core.GigaSpace#take(Object, long)} with
     * the given timeout.
     */
    @Override
    protected Object doReceiveBlocking(Object template, GigaSpace gigaSpace, long receiveTimeout) throws DataAccessException {
        return gigaSpace.take(template, receiveTimeout);
    }

    /**
     * Performs a single take operation using {@link org.openspaces.core.GigaSpace#take(Object, long)} with
     * no timeout.
     */
    @Override
    protected Object doReceiveNonBlocking(Object template, GigaSpace gigaSpace) throws DataAccessException {
        return gigaSpace.take(template, 0);
    }

    @Override
    public String toString() {
        return "Single Take, nonBlocking[" + nonBlocking + "], nonBlockingFactor[" + nonBlockingFactor + "]";
    }
}
