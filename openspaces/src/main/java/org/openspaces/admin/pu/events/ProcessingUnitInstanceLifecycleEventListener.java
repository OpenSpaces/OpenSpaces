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

package org.openspaces.admin.pu.events;

/**
 * A simple combination of a {@link org.openspaces.admin.pu.ProcessingUnitInstance} lifecycle events.
 *
 * @author kimchy
 * @see org.openspaces.admin.pu.ProcessingUnit#addLifecycleListener(ProcessingUnitInstanceLifecycleEventListener)
 * @see org.openspaces.admin.pu.ProcessingUnit#removeLifecycleListener(ProcessingUnitInstanceLifecycleEventListener)
 * @see org.openspaces.admin.pu.ProcessingUnits#addLifecycleListener(ProcessingUnitInstanceLifecycleEventListener)
 * @see org.openspaces.admin.pu.ProcessingUnits#removeLifecycleListener(ProcessingUnitInstanceLifecycleEventListener)
 */
public interface ProcessingUnitInstanceLifecycleEventListener extends ProcessingUnitInstanceAddedEventListener, ProcessingUnitInstanceRemovedEventListener {
}
