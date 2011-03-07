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

package org.openspaces.maven.plugin;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;

import sun.misc.URLClassPath;
import java.util.Collections;


/**
 * This class contains static helper methods for the Plugin.
 *
 * @author shaiw
 */
public class Utils {

    static final String GS_TYPE = "gsType";

    static final String GS_TYPE_PU = "PU";

    /**
     * Uses reflection to replace all white spaces in the ClassLoader's URLs to %20.
     */
    static void changeClassLoaderToSupportWhiteSpacesRepository(ClassLoader mavenClassLoader)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException, MalformedURLException {
        // Get the class of the ClassLoader, this should be RealmClassLoader.
        Class mavenClassLoaderClass = mavenClassLoader.getClass();

        // Get the super class of RealmClassLoader, this should be URLClassLoader.
        Class clazz2 = mavenClassLoaderClass.getSuperclass();

        // get the ucp field of URLClassLoader that holds the URLs
        Field ucpField = clazz2.getDeclaredField("ucp");

        // make this field accessible
        if (!ucpField.isAccessible()) {
            ucpField.setAccessible(true);
        }

        // get the value of the field, this should be URLClassPath.
        Object value = ucpField.get(mavenClassLoader);

        // get the URLs
        URLClassPath urlClassPath = (URLClassPath) value;
        URL[] urls = urlClassPath.getURLs();
        URL[] urls2 = new URL[urls.length];

        // replace the white spaces of all URLs with %20
        for (int i = 0; i < urls.length; i++) {
            String path = urls[i].toExternalForm();
            path = path.replaceAll(" ", "%20");
            urls2[i] = new URL(path);
        }

        // create the new URLClassPath
        URLClassPath uRLClassPath2 = new URLClassPath(urls2);

        // update the ucf field value 
        ucpField.set(mavenClassLoader, uRLClassPath2);
    }


    /**
     * Checks if a project is a PU project.
     *
     * @param project the project
     * @return true if the project is a PU project, false otherwise.
     */
    static boolean isPUType(MavenProject project) {
        String gsType = project.getProperties().getProperty(Utils.GS_TYPE);
        if (gsType == null) {
            return false;
        }
        return gsType.equalsIgnoreCase(Utils.GS_TYPE_PU);
    }


    /**
     * Gets a list of projects and returns only those that are PU.
     *
     * @param projects a list of projects.
     * @return a list of projects and returns only those that are PU.
     */
    static List getProjectsToExecute(List projects, String moduleName) {
        List puProjects = new ArrayList();
        Iterator i = projects.iterator();
        while (i.hasNext()) {
            MavenProject proj = (MavenProject) i.next();
            if (Utils.isPUType(proj) &&
                    (moduleName == null || moduleName.equals(proj.getName()))) {
                puProjects.add(proj);
            }
        }
        return puProjects;
    }


    /**
     * Returns a list of the artifacts' URLs.
     *
     * @return a list of the artifacts' URLs.
     */
    static List getArtifactURLs(Set artifacts) throws Exception {
        List urls = new ArrayList(artifacts.size());
        for (Iterator i = artifacts.iterator(); i.hasNext();) {
            Artifact artifact = (Artifact) i.next();
            urls.add(getURL(artifact.getFile()));
        }
        return urls;
    }
    
    
    /**
     * Converts a comma separated list in a String to a String array.
     * 
     * @param str the string
     * @return a String array that contains the list elements
     */
    static String[] convertCommaSeparatedListToArray(String str) {
        StringTokenizer st = new StringTokenizer(str, ",");
        List l = new LinkedList();
        while (st.hasMoreTokens()) {
            l.add(st.nextToken());
        }
        return (String[])l.toArray(new String[l.size()]);
    }
    
    /**
     * Resolves the processing unit's dependencies classpath.
     * 
     * @param project the processing unit project
     * @param includeScopes the scopes of the dependencies to include
     * @param includeProjects whether to include project's output directories
     * @param reactorProjects the reactor projects
     * @param dependencyTreeBuilder the dependency tree builder
     * @param metadataSource the metadata source
     * @param artifactCollector the artifact collector
     * @param artifactResolver the artifact resolver
     * @param artifactFactory the artifact factory
     * @param localRepository the local repository
     * @param remoteRepositories the remote repositories
     * @return a list containing all dependency URLs.
     * @throws Exception
     */
    static List resolveExecutionClasspath(MavenProject project, String[] includeScopes, 
            boolean includeProjects, List reactorProjects, DependencyTreeBuilder dependencyTreeBuilder, 
            ArtifactMetadataSource metadataSource, ArtifactCollector artifactCollector, 
            ArtifactResolver artifactResolver, ArtifactFactory artifactFactory, 
            ArtifactRepository localRepository, List remoteRepositories) throws Exception {
        
        Set scopes = new HashSet(includeScopes.length);
        Collections.addAll(scopes, includeScopes);
        
        // resolve all dependency of the specifies scope
        // scope 'test' is the widest scope available.
        ArtifactFilter artifactFilter = new ScopeArtifactFilter("test");
        DependencyNode root = dependencyTreeBuilder.buildDependencyTree(project, localRepository, artifactFactory,
                                                         metadataSource, artifactFilter,artifactCollector);
        
        // resolve all dependency files. if the dependency is a referenced project and not
        // a file in the repository add its output directory to the classpath. 
        Iterator i = root.preorderIterator();
        Set artifacts = new HashSet();
        while (i.hasNext()) {
            DependencyNode node = (DependencyNode)i.next();
            // the dependency may not be included due to duplication
            // dependency cycles and version conflict.
            // don't include those in the classpath.
            if (node.getState() != DependencyNode.INCLUDED) {
                PluginLog.getLog().debug("Not including dependency: " + node);
                continue;
            }
            Artifact artifact = node.getArtifact();
            if (artifact.getFile() == null) {
                try {
                    // if file is not found an exception is thrown
                    //artifactResolver.resolve(artifact, remoteRepositories, localRepository);
                }
                catch (Exception e) {
                    if (includeProjects) {
                        // try to see if the dependency is a referenced project
                        Iterator projectsIterator = reactorProjects.iterator();
                        while (projectsIterator.hasNext()) {
                            MavenProject proj = (MavenProject)projectsIterator.next();
                            if (proj.getArtifactId().equals(artifact.getArtifactId())) {
                                artifact.setFile(new File(proj.getBuild().getOutputDirectory()));
                                break;
                            }
                        }
                    }
                }
            }
            if (!scopes.contains(artifact.getScope())) {
                if (artifact.getScope() != null) {
                    continue;
                }
                // if it's not the same project don't add 
                if (!includeProjects || !project.getArtifactId().equals(artifact.getArtifactId())) {
                    continue;
                }
            }
            artifacts.add(artifact);
        }
        
        return getArtifactURLs(artifacts);
    }

    
    /**
     * Replaces white spaces in URLs to %20.
     *
     * @param f the file
     * @return A URL with %20 where white space suppose to be.
     * @throws Exception
     */
    static URL getURL(File f) throws Exception {
        String path = f.toURL().toExternalForm();
        path = path.replaceAll(" ", "%20");
        return new URL(path);
    }


    /**
     * Creates the class loader of the PU application.
     *
     * @param classpathUrls the classpath to use by the class loader.
     * @param parent        the parent classloader
     * @return the class loader of the PU application.
     * @throws MalformedURLException
     */
    static ClassLoader createClassLoader(List classpathUrls, ClassLoader parent) throws Exception {
        // create the classloader
        ClassLoader urlCL = URLClassLoader.newInstance((URL[]) classpathUrls.toArray(new URL[classpathUrls.size()]), parent);
        return urlCL;
    }


    /**
     * In case there was a SecurityException about missing license file creates an exception with
     * a special message, explaining what to do. In casse of other exceptions, the original
     * exception is wrapped with MojoExecutionException.
     *
     * @param e the original exception
     * @return the new exception.
     * @throws MojoExecutionException
     */
    static void throwMissingLicenseException(Throwable e, ArtifactRepository localRepository) throws MojoExecutionException {
        System.out.println("rep: " + localRepository.getBasedir());
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
            if (cause instanceof SecurityException) {
                if (cause.getMessage() != null && cause.getMessage().indexOf("gslicense.xml") > 0) {
                    String msg =
                            "\nThe GigaSpaces license file - gslicense.xml - was not found in Maven repository.\n" +
                                    "This file should be placed in the directory where gs-boot.jar resides.\n" +
                                    "Please try to reinstall OpenSpaces Plugin for Maven by running the installmavenrep' script again." +
                                    "Alternatively, copy gslicense.xml manually to " + localRepository.getBasedir() + "/com/gigaspaces/core/gs-boot/[version].";
                    throw new MojoExecutionException(msg);
                }
            }
        }
    }


    /**
     * Adds an attribute with all of its parameters to the list.
     *
     * @param list  the list
     * @param name  the attribute's name
     * @param value contains the attributes value or parameters
     */
    static void addAttributeToList(ArrayList list, String name, String value) {
        addAttributeToList(list, name, value, " ");
    }
    
    /**
     * Adds an attribute with all of its parameters to the list.
     *
     * @param list  the list
     * @param name  the attribute's name
     * @param value contains the attributes value or parameters
     * @param delimiter the delimiter of the list
     */
    static void addAttributeToList(ArrayList list, String name, String value, String delimiter) {
        if (value != null) {
            list.add(name);
            StringTokenizer st = new StringTokenizer(value, delimiter);
            String next;
            while (st.hasMoreTokens()) {
                next = st.nextToken();
                list.add(next);
            }
        }
    }


    /**
     * Finds the relative path from current directory to the PU jar file.
     *
     * @param project the Maven project
     * @return the relative path from current directory to the PU jar file.
     */
    static String getProcessingUnitJar(MavenProject project) {
        String targetDir = project.getBuild().getDirectory();
        String curDir = System.getProperty("user.dir");
        String relativePath = targetDir.substring(curDir.length() + 1);
        relativePath = relativePath.replace('\\', '/');
        String finalName = project.getBuild().getFinalName();
        if ("war".equalsIgnoreCase(project.getPackaging())) {
            return relativePath + "/" + finalName + ".war";
        } else {
            return relativePath + "/" + finalName + ".jar";
        }
    }


    /**
     * Finds the relative path from current directory to the PU dir.
     *
     * @param project the Maven project
     * @return the relative path from current directory to the PU dir.
     */
    static String getPURelativePathDir(MavenProject project) {
        String targetDir = project.getBuild().getDirectory();
        String curDir = System.getProperty("user.dir");
        String relativePath = targetDir.substring(curDir.length() + 1);
        relativePath = relativePath.replace('\\', '/');
        String finalName = project.getBuild().getFinalName();
        return relativePath + "/" + finalName;
    }


    static void handleSecurity() throws MojoExecutionException {
        if (System.getProperty("java.security.policy") == null) {
            try {
                Class secLoaderClass = Class.forName("com.j_spaces.kernel.SecurityPolicyLoader", true, Thread.currentThread().getContextClassLoader());
                secLoaderClass.getMethod("loadPolicy", new Class[]{String.class}).invoke(null, new Object[]{"policy/policy.all"});
            } catch (Exception e) {
                throw new MojoExecutionException("Failed to load security file", e);
            }
        }
    }
}