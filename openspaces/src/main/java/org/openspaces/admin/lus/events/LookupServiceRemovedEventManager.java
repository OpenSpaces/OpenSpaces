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

package org.openspaces.admin.lus.events;

/**
 * An event manager allowing to remove and add {@link LookupServiceRemovedEventListener}s.
 *
 * @author kimchy
 * @see org.openspaces.admin.lus.LookupServices#getLookupServiceRemoved()
 */
public interface LookupServiceRemovedEventManager {

    /**
     * Add the event listener.
     */
    void add(LookupServiceRemovedEventListener eventListener);

    /**
     * Remove the event listener.
     */
    void remove(LookupServiceRemovedEventListener eventListener);
}