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
 * 2. Fixes a possible Spring bug (or atleast in our case we need it). See: http://jira.springframework.org/browse/SPR-4875
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
            // continue here, since Spring does not try in its based class checking for JarURLConnection
            // It has this !!!! :) :
            // if (false && con instanceof JarURLConnection)
        }
        URLConnection con = rootDirResource.getURL().openConnection();
        JarFile jarFile = null;
        String jarFileUrl = null;
        String rootEntryPath = null;
        boolean newJarFile = false;

        if (con instanceof JarURLConnection) {
            // Should usually be the case for traditional JAR files.
            JarURLConnection jarCon = (JarURLConnection) con;
            jarCon.setUseCaches(false);
            jarFile = jarCon.getJarFile();
            jarFileUrl = jarCon.getJarFileURL().toExternalForm();
            JarEntry jarEntry = jarCon.getJarEntry();
            rootEntryPath = (jarEntry != null ? jarEntry.getName() : "");
        } else {
            // No JarURLConnection -> need to resort to URL file parsing.
            // We'll assume URLs of the format "jar:path!/entry", with the protocol
            // being arbitrary as long as following the entry format.
            // We'll also handle paths with and without leading "file:" prefix.
            String urlFile = rootDirResource.getURL().getFile();
            int separatorIndex = urlFile.indexOf(ResourceUtils.JAR_URL_SEPARATOR);
            if (separatorIndex != -1) {
                jarFileUrl = urlFile.substring(0, separatorIndex);
                rootEntryPath = urlFile.substring(separatorIndex + ResourceUtils.JAR_URL_SEPARATOR.length());
                jarFile = getJarFile(jarFileUrl);
            } else {
                jarFile = new JarFile(urlFile);
                jarFileUrl = urlFile;
                rootEntryPath = "";
            }
            newJarFile = true;
        }

        try {
            if (!"".equals(rootEntryPath) && !rootEntryPath.endsWith("/")) {
                // Root entry path must end with slash to allow for proper matching.
                // The Sun JRE does not return a slash here, but BEA JRockit does.
                rootEntryPath = rootEntryPath + "/";
            }
            Set result = new LinkedHashSet(8);
            for (Enumeration entries = jarFile.entries(); entries.hasMoreElements();) {
                JarEntry entry = (JarEntry) entries.nextElement();
                String entryPath = entry.getName();
                if (entryPath.startsWith(rootEntryPath)) {
                    String relativePath = entryPath.substring(rootEntryPath.length());
                    if (getPathMatcher().match(subPattern, relativePath)) {
                        result.add(rootDirResource.createRelative(relativePath));
                    }
                }
            }
            return result;
        }
        finally {
            // Close jar file, but only if freshly obtained -
            // not from JarURLConnection, which might cache the file reference.
            if (newJarFile) {
                jarFile.close();
            }
        }
    }
}
