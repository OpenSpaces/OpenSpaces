/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.admin.internal.space;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.openspaces.admin.space.SpaceInstanceRuntimeDetails;

/**
 * Internal Future referencing a SpaceInstanceRuntimeDetails.
 * 
 * @author moran
 * @since 9.1.0
 */
public class FutureSpaceInstanceRuntimeDetails implements Future<SpaceInstanceRuntimeDetails> {
    private final CountDownLatch latch = new CountDownLatch(1);
    private final AtomicReference<SpaceInstanceRuntimeDetails> ref = new AtomicReference<SpaceInstanceRuntimeDetails>();
    
    @Override
    public boolean isDone() {
        return latch.getCount() == 0;
    }
    
    @Override
    public boolean isCancelled() {
        return false;
    }
    
    @Override
    public SpaceInstanceRuntimeDetails get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        if (latch.await(timeout, unit)) {
            return ref.get();
        }
        if (timeout == 0) { //optimization if we just want to peek the Future ref and not throw a TimeoutException each time.
            return null;
        }
        throw new TimeoutException();
    }
    
    
    @Override
    public SpaceInstanceRuntimeDetails get() throws InterruptedException, ExecutionException {
        latch.await();
        return ref.get();
    }
    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }
    
    //package protected
    void set(SpaceInstanceRuntimeDetails runtimeDetails) {
        ref.set(runtimeDetails);
        latch.countDown();
    }
}
