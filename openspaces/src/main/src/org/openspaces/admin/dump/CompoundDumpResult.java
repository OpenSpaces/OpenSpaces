package org.openspaces.admin.dump;

import org.openspaces.admin.internal.dump.InternalDumpResult;
import org.openspaces.admin.AdminException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipOutputStream;

/**
 * @author kimchy
 */
public class CompoundDumpResult implements DumpResult {

    private ArrayList<DumpResult> dumpResults = new ArrayList<DumpResult>();

    public CompoundDumpResult add(DumpResult dumpResult) {
        dumpResults.add(dumpResult);
        return this;
    }

    public String getName() {
        StringBuilder sb = new StringBuilder();
        for (DumpResult dumpResult : dumpResults) {
            sb.append(dumpResult.getName()).append(",");
        }
        return sb.toString();
    }

    public DumpResult[] getResults() {
        return dumpResults.toArray(new DumpResult[dumpResults.size()]);
    }

    public long downloadSize() {
        long downloadSize = 0;
        for (DumpResult dumpResult : dumpResults) {
            downloadSize += dumpResult.downloadSize();
        }
        return downloadSize;
    }

    public void download(File targetDirectory, String fileName, DumpDownloadListener listener) throws AdminException {
        if (!fileName.endsWith(".zip")) {
            fileName = fileName + ".zip";
        }
        File zipFile = new File(targetDirectory, fileName);
        download(zipFile, listener);
    }

    public void download(File target, DumpDownloadListener listener) throws AdminException {
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(target));
            for (DumpResult dumpResult : dumpResults) {
                ((InternalDumpResult) dumpResult).download(zos, listener);
            }
            zos.close();
        } catch (IOException e) {
            throw new AdminException("Failed to download", e);
        }
    }
}
