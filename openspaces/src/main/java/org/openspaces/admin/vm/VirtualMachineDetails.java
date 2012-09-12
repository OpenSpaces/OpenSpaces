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

package org.openspaces.admin.vm;

import java.util.Map;

/**
 * Details (non changeable information) of a single virtual machine.
 *
 * @author kimchy
 */
public interface VirtualMachineDetails {

    /**
     * Returns <code>true</code> if the details of the virtual machine is not available.
     */
    boolean isNA();
    
    /**
     * The address of a JMX API connector server
     * This value is taken from JMXAttribute lookup attribute, in the case of LookupService null returned
     * @return url to JMX API connector server, can be null in the case of Lookup Service
     * @since 8.0
     */
    public String getJmxUrl();    

    /**
     * Returns the uid of the virtual machine.
     */
    String getUid();

    String getVmName();

    String getVmVersion();

    String getVmVendor();

    long getStartTime();

    /**
     * Returns the process id of the virtual machine.
     */
    long getPid();

    String getBootClassPath();
    String getClassPath();
    String[] getInputArguments();
    Map<String, String> getSystemProperties();

    long getMemoryHeapInitInBytes();
    double getMemoryHeapInitInMB();
    double getMemoryHeapInitInGB();

    long getMemoryHeapMaxInBytes();
    double getMemoryHeapMaxInMB();
    double getMemoryHeapMaxInGB();

    long getMemoryNonHeapInitInBytes();
    double getMemoryNonHeapInitInMB();
    double getMemoryNonHeapInitInGB();

    long getMemoryNonHeapMaxInBytes();
    double getMemoryNonHeapMaxInMB();
    double getMemoryNonHeapMaxInGB();
}
