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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.springframework.util.StringUtils;


/**
 * Goal which creates the openspaces project.
 *
 * @goal create
 * @requiresProject false
 */
public class CreatePUProjectMojo extends AbstractMojo {

    /**
     * the groupId string to replace with the package name
     */
    private static final String FILTER_GROUP_ID = "${puGroupId}";


    /**
     * the groupId string to replace with the package name
     */
    private static final String FILTER_GROUP_PATH = "${puGroupPath}";


    /**
     * the groupId string to replace with the package name
     */
    private static final String FILTER_ARTIFACT_ID = "${puArtifactId}";
    
    
    /**
     * Templates directory name
     */
    private static final String DIR_TEMPLATES = "pu-templates";


    /**
     * Project directory.
     *
     * @parameter expression="${artifactId}" default-value="my-app"
     */
    private File projectDir;


    /**
     * The packageName.
     *
     * @parameter expression="${groupId}" default-value="com.mycompany.app"
     */
    private String packageName;


    /**
     * The template.
     *
     * @parameter expression="${template}"
     */
    private String template;


    /**
     * The directory structure of the package.
     */
    private String packageDirs;
    
    
    
    public void execute() throws MojoExecutionException {
        try {
            List urls = getTemplatesURLs();
            ClassLoader cl = Utils.createClassLoader(urls, Thread.currentThread().getContextClassLoader());
            Thread.currentThread().setContextClassLoader(cl);
            
            if (template == null || template.trim().length() == 0) {
                //throw new IllegalArgumentException(createAvailableTemplatesMessage());
                System.out.println(createAvailableTemplatesMessage());
            }
            
            Enumeration templateUrl = Thread.currentThread().getContextClassLoader().getResources(DIR_TEMPLATES + "/" + template);
            if (templateUrl.hasMoreElements()) {
                // template found
                URL url = (URL) templateUrl.nextElement();
                getLog().debug("Found template at: " + url);

                // extract the jar path from the url
                String jarPath = getJarPathFromURL(url);
                getLog().debug("Template JAR file path: " + jarPath);

                extract(jarPath);
            }
            else {
                // the template was not found - show available templates
                throw new IllegalArgumentException(createAvailableTemplatesMessage());
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to create processing unit project", e);
        }
    }
    

    /**
     * Returns a list that contains all extension templates URLs.
     * @return a list that contains all extension templates URLs.
     * @throws Exception 
     */
    private List getTemplatesURLs() throws Exception {
        String pluginPath = getPluginPath();
        List urls = new ArrayList();
        if (pluginPath != null) {
            getLog().debug("Plugin path: " + pluginPath);
            File f = new File(pluginPath);
            File dir = f.getParentFile();
            File templatesDir = new File (dir, DIR_TEMPLATES);
            if (templatesDir.exists()) {
                File[] templates = templatesDir.listFiles();
                if (templates != null) {
                    for (int i = 0; i < templates.length; i++) {
                        //urls.add(Utils.getURL(templates[i]));
                        urls.add(templates[i].toURL());
                    }
                }
            }
        }
        return urls;
    }
    
    
    /**
     * Extracts the project files to the project directory.
     * @param jarFileName the plugin's JAR file
     * @return true of the template is found, false otherwise.
     * @throws Exception
     */
    private void extract(String jarFileName) throws Exception {
        packageDirs = packageName.replaceAll("\\.", "/");
        String puTemplate = DIR_TEMPLATES + "/" + template;
        int length = puTemplate.length();
        FileInputStream fis = new FileInputStream(jarFileName);
        BufferedInputStream bis = new BufferedInputStream(fis);
        JarInputStream jis = new JarInputStream(bis);
        JarEntry je;
        byte[] buf = new byte[1024];
        int n;
        while ((je = jis.getNextJarEntry()) != null) {
            String jarEntryName = je.getName();
            getLog().debug("JAR entry: " + jarEntryName);
            if (je.isDirectory() || !jarEntryName.startsWith(puTemplate)) {
                continue;
            }
            String targetFileName = projectDir + jarEntryName.substring(length);
            
            // convert the ${gsGroupPath} to directory
            targetFileName = StringUtils.replace(targetFileName, FILTER_GROUP_PATH, packageDirs);
            getLog().debug("Extracting entry " + jarEntryName + " to " + targetFileName);
            
            // read the bytes to the buffer
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            while ((n = jis.read(buf, 0, 1024)) > -1) {
                byteStream.write(buf, 0, n);
            }
            
            // replace property references with the syntax ${property_name}
            // to their respective property values.
            String data = byteStream.toString();
            data = StringUtils.replace(data, FILTER_GROUP_ID, packageName);
            data = StringUtils.replace(data, FILTER_ARTIFACT_ID, projectDir.getName());
            data = StringUtils.replace(data, FILTER_GROUP_PATH, packageDirs);
            
            // write the entire converted file content to the destination file.
            File f = new File(targetFileName);
            File dir = f.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            FileWriter writer = new FileWriter(f);
            writer.write(data);
            jis.closeEntry();
            writer.close();
        }
        jis.close();
    }
    

    /**
     * Returns the plugin path.
     * Needs to be invoked from the main thread.
     * @return the path to the plugin JAR file.
     * @throws Exception
     */
    private String getPluginPath() throws Exception {
        Enumeration urls = Thread.currentThread().getContextClassLoader().getResources(DIR_TEMPLATES);
        while (urls.hasMoreElements()) {
            URL url = (URL) urls.nextElement();
            String jarURLStr = getJarPathFromURL(url);
            return jarURLStr;
        }
        return null;
    }
    
    
    /**
     * Returns the path of a JAR file that appears in the URL
     * @param jarURL
     * @return
     */
    private String getJarPathFromURL(URL url) {
        String urlStr = url.toString();
        return urlStr.substring("jar:file:/".length(), urlStr.indexOf('!'));
    }
    
    
    /**
     * Returns a set containing all templates defined in this JAR file.
     * @param jarFileName the JAR file
     * @return a set containing all templates defined in this JAR file.
     * @throws Exception
     */
    public HashMap getJarTemplates(String jarFileName) throws Exception {
        getLog().debug("retrieving all templates of: " + jarFileName);
        String lookFor = DIR_TEMPLATES + "/";
        int length = lookFor.length();
        HashMap templates = new HashMap();
        FileInputStream fis = new FileInputStream(jarFileName);
        BufferedInputStream bis = new BufferedInputStream(fis);
        JarInputStream jis = new JarInputStream(bis);
        JarEntry je;
        Set temp = new HashSet();
        while ((je = jis.getNextJarEntry()) != null) {
            // find the template name
            String jarEntryName = je.getName();
            getLog().debug("Found entry: " + jarEntryName);
            if (jarEntryName.length() <= length || !jarEntryName.startsWith(lookFor)) {
                continue;
            }
            int nextSlashIndex = jarEntryName.indexOf("/", length);
            if (nextSlashIndex == -1) {
                continue;
            }
            String jarTemplate = jarEntryName.substring(length, nextSlashIndex);
            getLog().debug("Found template: " + jarTemplate);
            if (templates.containsKey(jarTemplate)) {
                continue;
            }
            if (jarEntryName.endsWith("readme.txt")) {
                // a description found - add to templates
                String description = getShortDescription(jis);
                templates.put(jarTemplate, description);
                // remove from temp
                temp.remove(jarTemplate);
            }
            else {
                // add to temp until a description is found
                temp.add(jarTemplate);
            }
        }
        // add all templates that has no description
        Iterator iter = temp.iterator();
        while (iter.hasNext()) {
            templates.put(iter.next(), "No description found.");
        }
        return templates;
    }
    
    
    private String getShortDescription(JarInputStream jis) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(jis));
        String description = reader.readLine();
        jis.closeEntry();
        return description;
    }
    
    
    /**
     * Returns an array of available project templates names.
     * @param jarFileName the plugin's JAR file
     * @return an array of available project templates names.
     * @throws Exception
     */
    private HashMap getAvailableTemplates() throws Exception {
        HashMap templates = new HashMap();
        int templatesDirNameLength = DIR_TEMPLATES.length();
        Enumeration urls = Thread.currentThread().getContextClassLoader().getResources(DIR_TEMPLATES);
        while (urls.hasMoreElements()) {
            URL url = (URL) urls.nextElement();
            String jarPath = getJarPathFromURL(url);
            HashMap jarTemplates = getJarTemplates(jarPath);
            templates.putAll(jarTemplates);
        }
        return templates;
    }
    
    
    /**
     * Creates a String message showing all available templates.
     * @return a String message showing all available templates
     * @throws Exception
     */
    private String createAvailableTemplatesMessage() throws Exception{
        HashMap availableTemplates = getAvailableTemplates();
        StringBuffer sb = new StringBuffer();
        if (template == null || template.trim().length() == 0) {
            sb.append("\nPlease use the -Dtemplate=<template> argument to specify a project template.\n");
        }
        else {
            sb.append("\nThe template '");
            sb.append(template);
            sb.append("' was not found.\n");
        }
        sb.append("Available templates:\n");
        sb.append("--------------------\n");
        Iterator iter = availableTemplates.keySet().iterator();
        int i = 1;
        while (iter.hasNext()) {
            String tmpl = (String)iter.next();
            String desc = (String)availableTemplates.get(tmpl);
            sb.append(i++);
            sb.append(". ");
            sb.append(tmpl);
            sb.append(" - ");
            sb.append(desc);
            sb.append("\n");
        }
        return sb.toString();
    }
}