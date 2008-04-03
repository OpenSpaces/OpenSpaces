package org.openspaces.maven.plugin;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;

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
               IllegalAccessException, MalformedURLException
    {
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
    
}
