/*******************************************************************************
 * 
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
 *  
 ******************************************************************************/
package org.openspaces.admin.dump;

import org.openspaces.admin.AdminException;

import java.io.File;

/**
 * A results of a dump operation. Allows to download the dump into a specific file.
 *
 * @author kimchy
 * @see org.openspaces.admin.dump.DumpProvider
*/
public interface DumpResult {

    /**
     * The name of the dump.
     */
    String getName();

    /**
     * The download size of the dump.
     */
    long downloadSize();

    /**
     * Downloads the dump into the target file. Allowing to provide a listener to track the downloading process.
     */
    void download(File target, DumpDownloadListener listener) throws AdminException;

    /**
     * Downloads the dump into the target file. Allowing to provide a listener to track the downloading process.
     */
    void download(File targetDirectory, String fileName, DumpDownloadListener listener) throws AdminException;
}
