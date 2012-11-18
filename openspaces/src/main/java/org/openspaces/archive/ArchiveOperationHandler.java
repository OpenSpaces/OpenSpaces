/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.openspaces.archive;

/**
 * @author Itai Frenkel
 * @since 9.1.1
 */
public interface ArchiveOperationHandler {

    void archive(Object... objects);

    /**
     * What happens when the archive operation receives a batch of objects to persist and throws an exception in the middle of the archiving?
     * The external archive container (assuming the exception is not swollen with an exception handler) will retry writing the objects.
     *  
     * If the archive operation implementation is atomic (meaning that throwing an exception cancels all writes - all or nothing) then return true.
     * If the archive operation implementation is idempotent (meaning that writing the same objects twice has no visible on the result) then return true.
     * 
     * Otherwise return false, since raising an exception would case all objects to be archived again, which would result in objects being archived twice.
     * Even when returning false, there is a possibility of an object being archived twice in case of a process fail-over.
     * 
     * @return true - if calling archive with more than one object
     *         false - means that calls to {@link #archive(Object...)} will contain only one object 
     */
    boolean supportsBatchArchiving();
}
