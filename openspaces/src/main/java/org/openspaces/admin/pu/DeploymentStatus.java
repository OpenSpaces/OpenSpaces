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

package org.openspaces.admin.pu;

/**
 * Deployment status indicates a {@link org.openspaces.admin.pu.ProcessingUnit} deployment.
 *
 * @author kimchy
 */
public enum DeploymentStatus {
    /**
     * Deployment status is not available.
     */
    NA,
    /**
     * Indicates the Processing Unit is not deployed
     */
    UNDEPLOYED,
    /**
     * Indicates the Processing Unit is scheduled for deployment
     */
    SCHEDULED,
    /**
     * Indicates the Processing Unit is deployed
     */
    DEPLOYED,
    /**
     * Indicates the Processing Unit is deployed and is broken, where all
     * required services are not available
     */
    BROKEN,
    /**
     * Indicates the Processing Unit is deployed and is compromised, where
     * some specified services are not available
     */
    COMPROMISED,
    /**
     * Indicates the Processing Unit is deployed and is intact, where all
     * specified services are available
     */
    INTACT
}
