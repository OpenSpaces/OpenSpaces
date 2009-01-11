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

/**
 * An aggreagted details of all the currently discovered virtual machines.
 *
 * @author kimchy
 */
public interface VirtualMachinesDetails {

    /**
     * Retruns the number of {@link org.openspaces.admin.vm.VirtualMachineDetails} that are being aggregated.
     */
    int getSize();

    String[] getVmName();

    String[] getVmVersion();

    String[] getVmVendor();

    /**
     * Retuns an aggregation of all the different virtual machines {@link VirtualMachineDetails#getMemoryHeapInitInBytes()}
     */
    long getMemoryHeapInitInBytes();
    double getMemoryHeapInitInMB();
    double getMemoryHeapInitInGB();

    /**
     * Retuns an aggregation of all the different virtual machines {@link VirtualMachineDetails#getMemoryHeapMaxInBytes()}
     */
    long getMemoryHeapMaxInBytes();
    double getMemoryHeapMaxInMB();
    double getMemoryHeapMaxInGB();

    /**
     * Retuns an aggregation of all the different virtual machines {@link VirtualMachineDetails#getMemoryNonHeapInitInBytes()}
     */
    long getMemoryNonHeapInitInBytes();
    double getMemoryNonHeapInitInMB();
    double getMemoryNonHeapInitInGB();

    /**
     * Retuns an aggregation of all the different virtual machines {@link VirtualMachineDetails#getMemoryNonHeapMaxInBytes()}
     */
    long getMemoryNonHeapMaxInBytes();
    double getMemoryNonHeapMaxInMB();
    double getMemoryNonHeapMaxInGB();
}
