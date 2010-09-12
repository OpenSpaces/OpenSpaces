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

package org.openspaces.events.support;

import com.gigaspaces.async.AsyncResult;
import org.openspaces.core.executor.DistributedTask;

import java.util.List;

/**
 * A task that unregisters (stops and disposes it) a dynamically added event container in a distributed manner.
 * (using {@link org.openspaces.events.polling.Polling} or {@link org.openspaces.events.notify.Notify}.
 * Returns <code>true</code> if all event containers were found and unregistered, <code>false</code> otherwise.
 *
 * @author kimchy
 */
public class UnregisterDistributedEventContainerTask extends UnregisterEventContainerTask implements DistributedTask<Boolean, Boolean> {

    protected UnregisterDistributedEventContainerTask() {
        super();
    }

    public UnregisterDistributedEventContainerTask(String containerName) {
        super(containerName);
    }

    public Boolean reduce(List<AsyncResult<Boolean>> results) throws Exception {
        for (AsyncResult<Boolean> result : results) {
            if (result.getException() != null) {
                throw result.getException();
            }
            if (!result.getResult()) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }
}