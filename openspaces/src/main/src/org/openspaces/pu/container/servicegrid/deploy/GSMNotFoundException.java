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

import java.util.Arrays;

/**
 * Indicates error in finding a GSM to deploy to.
 *
 * @author kimchy
 */
public class GSMNotFoundException extends NestedRuntimeException {

    private static final long serialVersionUID = -8073595538933440181L;

    private String[] groups;

    private long timeout;

    public GSMNotFoundException(String[] groups, long timeout, Exception e) {
        super("Failed to find GSM with groups " + toGroups(groups) + " and timeout [" + timeout + "]", e);
        this.groups = groups;
        this.timeout = timeout;
    }

    public GSMNotFoundException(String[] groups, long timeout) {
        super("Failed to find GSM with groups " + toGroups(groups) + " and timeout [" + timeout + "]");
        this.groups = groups;
        this.timeout = timeout;
    }

    public String[] getGroups() {
        return groups;
    }

    public long getTimeout() {
        return timeout;
    }

    private static String toGroups(String[] groups) {
        if (groups == null) {
            return "default groups";
        }
        return Arrays.asList(groups).toString();
    }
}
