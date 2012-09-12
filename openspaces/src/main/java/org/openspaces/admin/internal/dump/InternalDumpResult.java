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

import com.gigaspaces.internal.dump.InternalDumpProvider;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.dump.DumpProvider;
import org.openspaces.admin.dump.DumpResult;
import org.openspaces.admin.dump.DumpDownloadListener;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

/**
 * @author kimchy
 */
public class InternalDumpResult implements DumpResult {

    private final DumpProvider dumpProvider;

    private final InternalDumpProvider internalDumpProvider;

    private final com.gigaspaces.internal.dump.InternalDumpResult internalResult;

    public InternalDumpResult(DumpProvider dumpProvider, InternalDumpProvider internalDumpProvider, com.gigaspaces.internal.dump.InternalDumpResult internalResult) {
        this.dumpProvider = dumpProvider;
        this.internalDumpProvider = internalDumpProvider;
        this.internalResult = internalResult;
    }

    @Override
    public String getName() {
        return internalResult.getName();
    }

    @Override
    public long downloadSize() {
        return internalResult.downloadSize();
    }

    public void download(ZipOutputStream zos, DumpDownloadListener listener) throws IOException {
        internalResult.download(internalDumpProvider, zos, new InternalDumpDownloadListenerAdapter(dumpProvider, listener));
    }

    @Override
    public void download(File target, DumpDownloadListener listener) throws AdminException {
        try {
            internalResult.download(internalDumpProvider, target, new InternalDumpDownloadListenerAdapter(dumpProvider, listener));
        } catch (Exception e) {
            throw new AdminException("Failed to download", e);
        }
    }

    @Override
    public void download(File targetDirectory, String fileName, DumpDownloadListener listener) throws AdminException {
        try {
            internalResult.download(internalDumpProvider, targetDirectory, fileName, new InternalDumpDownloadListenerAdapter(dumpProvider, listener));
        } catch (Exception e) {
            throw new AdminException("Failed to download", e);
        }
    }
}
