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

package org.openspaces.pu.container.support;

import org.openspaces.pu.container.CannotCloseContainerException;
import org.openspaces.pu.container.ProcessingUnitContainer;

/**
 * Compound processing unit container wraps several processing unit containers and
 * allows to close them.
 *
 * @author kimchy
 */
public class CompoundProcessingUnitContainer implements ProcessingUnitContainer {

    private final ProcessingUnitContainer[] containers;

    public CompoundProcessingUnitContainer(ProcessingUnitContainer[] containers) {
        this.containers = containers;
    }
    
    /**
     * @return the underlying processing unit containers.
     * @since 8.0.3
     */
    public ProcessingUnitContainer[] getProcessingUnitContainers() {
        return containers;
    }

    public void close() throws CannotCloseContainerException {
        for (ProcessingUnitContainer container : containers) {
            container.close();
        }
    }
}
