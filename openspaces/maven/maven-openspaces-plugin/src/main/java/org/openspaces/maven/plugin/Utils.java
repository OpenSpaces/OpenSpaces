package org.openspaces.maven.plugin;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import sun.misc.URLClassPath;

public class Utils {

    /**
     * Uses reflection to replace all white spaces in the ClassLoader's URLs to %20.
     * @param classLoader the ClassLoader
     * @throws NoSuchFieldException 
     * @throws SecurityException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws MalformedURLException 
     */
    public static void changeClassLoaderToSupportWhiteSpacesRepository(ClassLoader mavenClassLoader) 
        throws SecurityException, NoSuchFieldException, IllegalArgumentException,
               IllegalAccessException, MalformedURLException {
        // Get the class of the ClassLoader, this should be RealmClassLoader.
        Class mavenClassLoaderClass = mavenClassLoader.getClass();
        
        // Get the super class of RealmClassLoader, this should be URLClassLoader.
        Class clazz2 = mavenClassLoaderClass.getSuperclass();
        
        // get the ucp field of URLClassLoader that holds the URLs
        Field ucpField = clazz2.getDeclaredField("ucp");
        
        // make this field accessible
        if (!ucpField.isAccessible())
        {
            ucpField.setAccessible(true);
        }
        
        // get the value of the field, this should be URLClassPath.
        Object value = ucpField.get(mavenClassLoader);
        
        // get the URLs
        URLClassPath urlClassPath = (URLClassPath)value;
        URL[] urls = urlClassPath.getURLs();
        URL[] urls2 = new URL[urls.length];
        
        // replace the white spaces of all URLs with %20
        for (int i = 0; i < urls.length; i++) 
        {
            String path = urls[i].toExternalForm();
            path = path.replaceAll(" ", "%20");
            urls2[i] = new URL(path);
        }
        
        // create the new URLClassPath
        URLClassPath uRLClassPath2 = new URLClassPath(urls2);
        
        // update the ucf field value 
        ucpField.set(mavenClassLoader, uRLClassPath2);
    }
    
    public static List resolveProjects(MavenProject project, String moduleName) {
        List projects = new ArrayList();
        if (moduleName != null) {
            if (project.getPackaging() != null && project.getPackaging().equalsIgnoreCase("pom")) {
                List collectedProjects = project.getCollectedProjects();
                for (Iterator projIt = collectedProjects.iterator(); projIt.hasNext();) {
                    MavenProject proj = (MavenProject) projIt.next();
                    if (proj.getName().equals(moduleName)) {
                        projects.add(proj);
                    }
                }
            } else {
                projects.add(project);
            }
        } else {
            if (project.getPackaging() != null && project.getPackaging().equalsIgnoreCase("pom")) {
                List collectedProjects = project.getCollectedProjects();
                // sort the projects by the order parameter
                Collections.sort(collectedProjects, new PUProjectSorter(true));
                for (Iterator projIt = collectedProjects.iterator(); projIt.hasNext();) {
                    MavenProject proj = (MavenProject) projIt.next();
                    if (proj.getProperties().getProperty(PUProjectSorter.PARAM_ORDER) != null) {
                        projects.add(proj);
                    }
                }
            } else {
                projects.add(project);
            }
        }
        return projects;
    }
    
    public static String getProcessingUnitJar(MavenProject project) {
        String targetDir = project.getBuild().getDirectory();
        String curDir = System.getProperty("user.dir");
        String relativePath = targetDir.substring(curDir.length()+1);
        relativePath = relativePath.replace("\\", "/");
        String finalName = project.getBuild().getFinalName();
        String name = relativePath + "/" + finalName + ".jar";
        return name;
    }

    /**
     * resolves the PU classpath.
     * It includes the PU executables and dependencies. 
     * @return a list containing all classpath paths.
     */
    public static List resolveClasspath(MavenProject project) throws Exception {
        List dependencyFiles = new ArrayList();
        dependencyFiles.add(getURL(project.getArtifact().getFile()));
        Set dependencyArtifacts = project.getArtifacts();
        for (Iterator i = dependencyArtifacts.iterator(); i.hasNext();) {
            Artifact artifact = (Artifact)i.next();
            dependencyFiles.add(getURL(artifact.getFile()));
        }
        return dependencyFiles; 
    }
    
    private static URL getURL(File f) throws Exception {
        String path = f.toURL().toExternalForm();
        path = path.replaceAll(" ", "%20");
        return new URL(path);
    }
    
    /**
     * Creates the class loader of the PU application.
     * @param classpathUrls the classpath to use by the class loader.
     * @param parent the parent classloader
     * @return the class loader of the PU application.
     * @throws MalformedURLException
     */
    public static ClassLoader createClassLoader( List classpathUrls, ClassLoader parent) throws Exception {
        // create the classloader
        ClassLoader urlCL = URLClassLoader.newInstance((URL[]) classpathUrls.toArray(new URL[classpathUrls.size()]), parent);
        return urlCL;
    }
    
    public static MojoExecutionException createMojoException(Exception e) {
        Throwable cause = (Throwable)e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
            if (cause instanceof SecurityException) {
                if (cause.getMessage() != null && cause.getMessage().contains("gslicense.xml")) {
                    String msg = 
                        "\nThe GigaSpaces license file - gslicense.xml - was not found in " +
                        "Maven repository.\nThis file should be placed in the directory " +
                        "where gs-boot.jar resides.\n" +
                        "Please try to reinstall OpenSpaces Plugin for Maven by running " +
                        "'installmavenrep' script again.\nAlternatively, copy gslicense.xml " +
                        "manually to <maven-repository-home>/gigaspaces/gs-boot/<version>.";
                    return new MojoExecutionException(msg);
                }
            }
        }
        return new MojoExecutionException(e.getMessage(), e);
    }
}
