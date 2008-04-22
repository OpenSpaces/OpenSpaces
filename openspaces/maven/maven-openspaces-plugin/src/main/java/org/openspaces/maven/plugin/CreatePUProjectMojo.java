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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal which creates the openspaces project.
 *
 * @goal create
 * 
 * @requiresProject  false
 */
public class CreatePUProjectMojo extends AbstractMojo
{
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
     * @parameter expression="${artifactId}"
     * @required
     */
    private File projectDir;
    
    /**
     * The packageName.
     * @parameter expression="${groupId}"
     * @required
     */
    private String packageName;
    
    /**
     * The template.
     * @parameter expression="${template}" default-value="default"
     */
    private String template;
    
    /**
     * The directory structure of the package.
     */
    private String packageDirs;
    
    /**
     * The template directory name.
     */
    private String templateDirName;
    
    private boolean isMirror = false;

    
    public void execute() throws MojoExecutionException
    {
    	getLog().info("Project template: " + template);
    	if (template.equals("default"))
    	{
    		executeDefault();
    	}
    	else if (template.equals("mule"))
    	{
    	    executeMule();
    	}
    	else if (template.equals("mirror"))
        {
    	    isMirror = true;
            executeMirror();
        }
    	else
    	{
    		throw new MojoExecutionException("Unknown project template: " + template);
    	}
    }
    
    /**
     * Mojo implementation
     */
    public void executeDefault() throws MojoExecutionException
    {
        packageDirs = packageName.replaceAll("\\.", "/");

        if ( !projectDir.exists() )
        {
        	projectDir.mkdirs();
        }
        
        templateDirName = "/templates/" + template;
        try
        {
        	copyResource(templateDirName+"/pom.xml", projectDir, "pom.xml");
        	getLog().info("Generating module: common");
        	createCommonModule(projectDir);
        	getLog().info("Generating module: feeder");
        	createFeederModule(projectDir);
        	getLog().info("Generating module: processor");
        	createProcessorModule(projectDir);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void executeMule() throws MojoExecutionException
    {
        packageDirs = packageName.replaceAll("\\.", "/");

        if ( !projectDir.exists() )
        {
            projectDir.mkdirs();
        }
        
        templateDirName = "/templates/" + template;
        try
        {
            copyResource(templateDirName+"/pom.xml", projectDir, "pom.xml");
            getLog().info("Generating module: common");
            createCommonModule(projectDir);
            getLog().info("Generating module: feeder");
            createFeederModule(projectDir);
            getLog().info("Generating module: processor");
            createMuleProcessorModule(projectDir);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void executeMirror() throws MojoExecutionException
    {
        packageDirs = packageName.replaceAll("\\.", "/");

        if ( !projectDir.exists() )
        {
            projectDir.mkdirs();
        }
        
        templateDirName = "/templates/" + template;
        try
        {
            copyResource(templateDirName+"/pom.xml", projectDir, "pom.xml");
            getLog().info("Generating module: common");
            createMirrorCommonModule(projectDir);
            getLog().info("Generating module: feeder");
            createFeederModule(projectDir);
            getLog().info("Generating module: processor");
            createProcessorModule(projectDir);
            getLog().info("Generating module: mirror");
            createMirrorModule(projectDir);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Creates the common module
     * @param projDir the project directory
     */
    private void createCommonModule(File projDir)
    {
    	File commonDir = new File(projDir, "common/src/main/java/"+packageDirs+"/common");
    	commonDir.mkdirs();
    	
    	// copy common dir
    	copyResource(templateDirName+"/common/src/Data.java", commonDir, "Data.java");
    	
        // copy pom.xml
    	File pomDir = new File(projDir, "common");
    	copyResource(templateDirName+"/common/pom.xml", pomDir, "pom.xml");
    }
    
    /**
     * Creates the feeder module
     * @param projDir the project directory
     */
    private void createFeederModule(File projDir)
    {
    	File feederDir = new File(projDir, "feeder/src/main/java/"+packageDirs+"/feeder");
    	feederDir.mkdirs();
    	
    	// copy feeder dir
    	copyResource(templateDirName+"/feeder/src/Feeder.java", feederDir, "Feeder.java");
    	
    	// copy pu.xml
    	File puDir = new File(projDir, "feeder/src/main/resources/META-INF/spring");
    	puDir.mkdirs();
    	copyResource(templateDirName+"/feeder/META-INF/spring/pu.xml", puDir, "pu.xml");
        
        // copy assembly dir
        File assemblyDir = new File(projDir, "feeder/src/main/assembly");
    	assemblyDir.mkdirs();
    	copyResource(templateDirName+"/feeder/assembly/assembly-jar.xml", assemblyDir, "assembly-jar.xml");
        copyResource(templateDirName+"/feeder/assembly/assembly-dir.xml", assemblyDir, "assembly-dir.xml");
    	
        // copy pom.xml
    	File pomDir = new File(projDir, "feeder");
    	copyResource(templateDirName+"/feeder/pom.xml", pomDir, "pom.xml");
    }
    
    /**
     * Creates the processor module
     * @param projDir the project directory
     */
    private void createProcessorModule(File projDir)
    {
    	File processorDir = new File(projDir, "processor/src/main/java/"+packageDirs+"/processor");
    	processorDir.mkdirs();
    	
    	// copy processor dir
    	copyResource(templateDirName+"/processor/src/Processor.java", processorDir, "Processor.java");
    	
    	// copy pu.xml
    	File puDir = new File(projDir, "processor/src/main/resources/META-INF/spring");
    	puDir.mkdirs();
    	copyResource(templateDirName+"/processor/META-INF/spring/pu.xml", puDir, "pu.xml");
        
        // copy assembly dir
        File assemblyDir = new File(projDir, "processor/src/main/assembly");
    	assemblyDir.mkdirs();
        copyResource(templateDirName+"/processor/assembly/assembly-jar.xml", assemblyDir, "assembly-jar.xml");
        copyResource(templateDirName+"/processor/assembly/assembly-dir.xml", assemblyDir, "assembly-dir.xml");
    	
        // copy pom.xml
    	File pomDir = new File(projDir, "processor");
    	copyResource(templateDirName+"/processor/pom.xml", pomDir, "pom.xml");
    }
    
    /**
     * Creates the mule processor module
     * @param projDir the project directory
     */
    private void createMuleProcessorModule(File projDir)
    {
        createProcessorModule(projDir);
     
        // copy mule.xml
        File puDir = new File(projDir, "processor/src/main/resources/META-INF/spring");
        if (!puDir.exists())
        {
            puDir.mkdirs();
        }
        copyResource(templateDirName+"/processor/META-INF/spring/mule.xml", puDir, "mule.xml");
    }
    
    /**
     * Creates the common module for the mirror project
     * @param projDir the project directory
     */
    private void createMirrorCommonModule(File projDir)
    {
        createCommonModule(projDir);
        
        // copy the Data hibernate mapping file
        File commonDir = new File(projDir, "common/src/main/java/"+packageDirs+"/common");
        commonDir.mkdirs();
        
        // copy common dir
        copyResource(templateDirName+"/common/src/Data.hbm.xml", commonDir, "Data.hbm.xml");
    }
    
    /**
     * Creates the mirror module
     * @param projDir the project directory
     */
    private void createMirrorModule(File projDir)
    {
        // copy pu.xml
        File puDir = new File(projDir, "mirror/src/main/resources/META-INF/spring");
        puDir.mkdirs();
        copyResource(templateDirName+"/mirror/META-INF/spring/pu.xml", puDir, "pu.xml");
        
        // copy assembly dir
        File assemblyDir = new File(projDir, "mirror/src/main/assembly");
        assemblyDir.mkdirs();
        copyResource(templateDirName+"/mirror/assembly/assembly-jar.xml", assemblyDir, "assembly-jar.xml");
        copyResource(templateDirName+"/mirror/assembly/assembly-dir.xml", assemblyDir, "assembly-dir.xml");
        
        // copy pom.xml
        File pomDir = new File(projDir, "mirror");
        copyResource(templateDirName+"/mirror/pom.xml", pomDir, "pom.xml");
    }
    
    /**
     * Copies a resource to the target directory
     * @param sourceFile the file to copy
     * @param targetDir the destination directory
     * @param targetFile the name of the target file
     */
    private void copyResource(String sourceFile, File targetDir, String targetFile)
    {
    	try
        {
    		String data;
            
            // prepare the file reader
    		InputStream is = getClass().getResourceAsStream( sourceFile );
    		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    		StringBuilder contentBuilder = new StringBuilder();
    		
            // read the lines one by one and replace property references with 
            // the syntax ${property_name} to their respective property values.
    		while ((data = reader.readLine()) != null)
    		{
   				data = data.replace(FILTER_GROUP_ID, packageName);
   				data = data.replace(FILTER_ARTIFACT_ID, projectDir.getName());
   				if (isMirror)
   				{
   				    data = data.replace(FILTER_GROUP_PATH, packageDirs);
   				}
   				contentBuilder.append(data);
    			contentBuilder.append(NEW_LINE);
    		}
    		
            // write the entire converted file content to the destination file.
    		File f = new File(targetDir, targetFile);
    		getLog().debug("Copying resource " + sourceFile + " to " + f.getAbsolutePath());
        	FileWriter writer = new FileWriter(f);
            writer.write(contentBuilder.toString());
            reader.close();
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
   
}
