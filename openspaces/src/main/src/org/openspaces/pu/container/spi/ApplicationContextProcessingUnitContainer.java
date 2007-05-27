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

package org.openspaces.pu.container.spi;

import org.openspaces.pu.container.ProcessingUnitContainer;
import org.springframework.context.ApplicationContext;

/**
 * A processing unit container that is based on Spring {@link ApplicationContext}.
 * 
 * @author kimchy
 */
public interface ApplicationContextProcessingUnitContainer extends ProcessingUnitContainer {

    ApplicationContext getApplicationContext();
}
