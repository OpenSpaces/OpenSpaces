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

package org.openspaces.pu.container;

/**
 * A processing unit container provider is responsible for creating
 * {@link org.openspaces.pu.container.ProcessingUnitContainer}. Usually concrete implementation
 * will have additional parameters controlling the nature of how specific container will be created.
 * 
 * @author kimchy
 */
public interface ProcessingUnitContainerProvider {

    public static final String CONTAINER_CLASS_PROP = "pu.container.class";
    
    /**
     * Creates a processing unit container.
     * 
     * @return A newly created processing unit container.
     * @throws CannotCreateContainerException
     */
    ProcessingUnitContainer createContainer() throws CannotCreateContainerException;
}
