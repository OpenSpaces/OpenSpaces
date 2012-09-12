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
package org.openspaces.admin.internal.dump;

import com.gigaspaces.internal.dump.InternalDumpDownloadListener;
import org.openspaces.admin.dump.DumpDownloadListener;
import org.openspaces.admin.dump.DumpProvider;

/**
 * @author kimchy
 */
public class InternalDumpDownloadListenerAdapter implements InternalDumpDownloadListener {

    private final DumpProvider dumpProvider;

    private final DumpDownloadListener listener;

    public InternalDumpDownloadListenerAdapter(DumpProvider dumpProvider, DumpDownloadListener listener) {
        this.dumpProvider = dumpProvider;
        this.listener = listener;
    }

    @Override
    public void onDownload(long downloadedBytes, String name, String fileName) {
        if (listener != null) {
            listener.onDownload(dumpProvider, downloadedBytes, name, fileName);
        }
    }
}
