package org.openspaces.pu.container.support;

import org.jini.rio.boot.ServiceClassLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A specific pattern resolver that handles the following:
 *
 * 1. Allows to create matching on "file system" resource which actually resides on webster
 *
 * Note: This is only applicable when not downloading the processing unit. Or when we are downloading
 * the processing unit, and shared-lib is used (which is not longer recommended in 7.0).
 *
 * @author kimchy
 */
public class PUPathMatchingResourcePatternResolver extends PathMatchingResourcePatternResolver {

    protected Resource convertClassLoaderURL(URL url) {
        if (!(getClassLoader() instanceof ServiceClassLoader)) {
            return super.convertClassLoaderURL(url);
        }
        // add a special case when working with webster, we can list with http
        if (url.getProtocol().equals("http") && url.toExternalForm().endsWith("/")) {
            return new WebsterResoruce(url);
        }
        return new UrlResource(url);
    }

    protected Set doFindMatchingFileSystemResources(File rootDir, String subPattern) throws IOException {
        Set result = super.doFindMatchingFileSystemResources(rootDir, subPattern);
        Set actualResult = new LinkedHashSet();
        for (Object val : result) {
            if (!(val instanceof FileSystemResource)) {
                continue;
            }
            FileSystemResource fsResource = (FileSystemResource) val;
            if (fsResource.getFile() instanceof WebsterFile) {
                WebsterFile websterFile = (WebsterFile) fsResource.getFile();
                actualResult.add(new UrlResource(websterFile.toURL()));
            } else {
                actualResult.add(fsResource);
            }
        }
        return actualResult;
    }

    protected Set doFindPathMatchingJarResources(Resource rootDirResource, String subPattern) throws IOException {
        try {
            return super.doFindPathMatchingJarResources(rootDirResource, subPattern);
        } catch (IOException e) {
            // ignore exceptions on shraed-lib, since they come and go when we undeploy and deploy from the FS
            // but still remain in the CommonClassLoader
            if (rootDirResource.getURL().toExternalForm().indexOf("shared-lib") != -1) {
                return new LinkedHashSet();
            }
            throw e;
        }
    }
}
