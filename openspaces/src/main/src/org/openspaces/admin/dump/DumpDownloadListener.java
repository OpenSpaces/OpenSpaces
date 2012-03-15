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

/**
 * A dump download listener allowing to track the downloading of dump result.
 *
 * @author kimchy
 * @see org.openspaces.admin.dump.DumpResult
 */
public interface DumpDownloadListener {

    /**
     * Called repeatedly with the downloaded bytes since the last call (the total can be obtained from
     * {@link DumpResult#downloadSize()}.
     */
    void onDownload(DumpProvider dumpProvider, long downloadedBytes, String name, String fileName);
}
