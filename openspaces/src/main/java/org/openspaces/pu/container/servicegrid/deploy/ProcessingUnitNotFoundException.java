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

package org.openspaces.pu.container.servicegrid.deploy;

import org.springframework.core.NestedRuntimeException;

import com.gigaspaces.grid.gsm.GSM;

/**
 * Failure to find a processing unit to deploy to.
 *
 * @author kimchy
 */
public class ProcessingUnitNotFoundException extends NestedRuntimeException {

    private static final long serialVersionUID = 2795475112878615318L;

    public ProcessingUnitNotFoundException(String name, GSM gsm) {
        super("Failed to find Processing Unit [" + name + "] under GSM [" + gsm + "]");
    }
}