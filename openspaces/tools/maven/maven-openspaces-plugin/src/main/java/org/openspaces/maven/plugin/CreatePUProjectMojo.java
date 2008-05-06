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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
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
     * New line
     */
    private static final String NEW_LINE = "\n";


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
     * @parameter expression="${template}" default-value="default"
     */
    private String template;


    /**
     * The directory structure of the package.
     */
    private String packageDirs;


    public void execute() throws MojoExecutionException {
        try {
            Enumeration urls = Thread.currentThread().getContextClassLoader().getResources("/pu-templates");
            while (urls.hasMoreElements()) {
                URL url = (URL) urls.nextElement();
                String jarURLStr = url.toString().substring("jar:file:/".length()-1, url.toString().indexOf('!'));
                boolean foundTemplate = extract(jarURLStr);
                if (!foundTemplate)
                {
                    String[] availableTemplates = getAvailableTemplates(jarURLStr);
                    StringBuilder sb = new StringBuilder();
                    sb.append("Template [" + template + "] not found.\n");
                    sb.append("Available templates: [");
                    for (int i = 0; i < availableTemplates.length; i++) {
                        sb.append(availableTemplates[i]);
                        if (i < availableTemplates.length-1) {
                            sb.append(", ");
                        }
                    }
                    sb.append("]");
                    throw new IllegalArgumentException(sb.toString());
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to create processing unit project", e);
        }
    }
    
    
    /**
     * Extracts the project files to the project directory.
     * @param jarFileName the plugin's JAR file
     * @return true of the template is found, false otherwise.
     * @throws Exception
     */
    private boolean extract(String jarFileName) throws Exception {
        packageDirs = packageName.replaceAll("\\.", "/");
        String puTemplate = "pu-templates/" + template;
        int length = puTemplate.length();
        FileInputStream fis = new FileInputStream(jarFileName);
        BufferedInputStream bis = new BufferedInputStream(fis);
        JarInputStream jis = new JarInputStream(bis);
        JarEntry je = null;
        boolean foundTemplateContent = false;
        while ((je = jis.getNextJarEntry()) != null) {
            String jarEntryName = je.getName();
            getLog().debug("JAR entry: " + jarEntryName);
            if (je.isDirectory() || !jarEntryName.startsWith(puTemplate)) {
                continue;
            }
            foundTemplateContent = true;
            String targetFileName = projectDir + jarEntryName.substring(length);
            getLog().debug("Extracting entry [" + jarEntryName + "] to file [" + targetFileName + "]");
            copyResource("/" + jarEntryName, targetFileName);
        }
        return foundTemplateContent;
    }

    /**
     * Copies a resource to the target directory
     *
     * @param sourceFile the file to copy
     * @param targetFile the name of the target file
     * @throws Exception 
     */
    private void copyResource(String sourceFile, String targetFile) throws Exception {
        
        // convert the ${gsGroupPath} to directory
        targetFile = StringUtils.replace(targetFile, FILTER_GROUP_PATH, packageDirs);
        
        // prepare the file reader
        InputStream is = getClass().getResourceAsStream(sourceFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuffer contentBuilder = new StringBuffer();

        // read the lines one by one and replace property references with 
        // the syntax ${property_name} to their respective property values.
        String data;
        while ((data = reader.readLine()) != null) {
            data = StringUtils.replace(data, FILTER_GROUP_ID, packageName);
            data = StringUtils.replace(data, FILTER_ARTIFACT_ID, projectDir.getName());
            data = StringUtils.replace(data, FILTER_GROUP_PATH, packageDirs);
            contentBuilder.append(data);
            contentBuilder.append(NEW_LINE);
        }

        // write the entire converted file content to the destination file.
        File f = new File(targetFile);
        File dir = f.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        getLog().debug("Copying resource " + sourceFile + " to " + f.getAbsolutePath());
        FileWriter writer = new FileWriter(f);
        writer.write(contentBuilder.toString());
        reader.close();
        writer.close();
    }
    
    
    /**
     * Returns an array of available project templates names.
     * @param jarFileName the plugin's JAR file
     * @return an array of available project templates names.
     * @throws Exception
     */
    private String[] getAvailableTemplates(String jarFileName) throws Exception {
        Set templates = new HashSet();
        String templatesDir = "pu-templates/";
        int length = templatesDir.length();
        FileInputStream fis = new FileInputStream(jarFileName);
        BufferedInputStream bis = new BufferedInputStream(fis);
        JarInputStream jis = new JarInputStream(bis);
        JarEntry je = null;
        while ((je = jis.getNextJarEntry()) != null) {
            String jarEntryName = je.getName();
            if (jarEntryName.startsWith(templatesDir)) {
                int nextSlashLocation = jarEntryName.indexOf("/", length);
                if (nextSlashLocation != -1) {
                    String templateName = jarEntryName.substring(length, nextSlashLocation);
                    templates.add(templateName);
                }
            }
        }
        String[] templatesArray = new String[templates.size()];
        templates.toArray(templatesArray);
        return templatesArray;
    }
}