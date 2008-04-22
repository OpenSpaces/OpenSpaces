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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainer;


/**
 * Goal that runs a processing unit.
 *
 * @goal pu-run
 * 
 * @requiresProject false
 * @description Runs ...
 */
public class RunPUMojo extends AbstractMojo
{
    /** 
     * The classpath elements of the project being tested.
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    
    /** 
     * The classpath elements of the project being tested.
     * @parameter expression="${puName}"
     * @required
     * @readonly
     */
    private String puName;
    
    /**
     * cluster
     * @parameter expression="${cluster}"
     */
    private String cluster;
    
    /**
     * proeprties
     * @parameter expression="${proeprties}"
     */
    private String proeprties;

    /**
     * The processing unit project
     */
    private MavenProject puProject;
    
    
	public void execute() throws MojoExecutionException, MojoFailureException 
	{
		if (project == null || !project.getPackaging().equalsIgnoreCase("pom"))
		{
			throw new MojoExecutionException("The goal has to be run in the master context" +
					" of a multi module project (packaging=pom).");
		}
		Iterator i = project.getCollectedProjects().iterator();
		while (i.hasNext())
		{
			MavenProject m = (MavenProject)i.next();
			if (m.getName().equals(puName))
			{
				getLog().info("Located processing unit project: " + puName);
				puProject = m;
				break;
			}
		}
		if (puProject == null)
		{
			throw new MojoExecutionException("Failed to locate processing unit project named '" + puName + "'.");
		}
		executePU();
	}
	
	
	/**
	 * Prepares and executes the PU.  
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	private void executePU() throws MojoExecutionException, MojoFailureException
	{
		if (puProject == null || !puProject.getPackaging().equalsIgnoreCase("jar"))
		{
			throw new MojoExecutionException("The processing unit project '"+ puProject.getName() +
					"' must be of type jar (packaging=jar).");
		}
		
		// resolve the classpath of the PU to run
		List<String> classpath = resolveClasspath();
		
		getLog().info("Processing unit classpath: "+classpath);

		// save the current class loader
		ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
		try
		{
		    // Remove white spaces from ClassLoader's URLs
		    try 
		    {
                Utils.changeClassLoaderToSupportWhiteSpacesRepository(currentCL);
            }
		    catch (Exception e) 
		    {
		        getLog().info("Unable to update ClassLoader. Proceeding with processing unit invocation.", e);
            }
		    
			// create the class loader for the PU execution
			ClassLoader classLoader = createClassLoader(classpath, currentCL);

			// set the current ClassLoader
			getLog().debug("Setting the processing unit's ClassLoader.");
			Thread.currentThread().setContextClassLoader(classLoader);

			// run the PU
			runPU();
		}
		catch (MojoExecutionException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new MojoFailureException(e, "Processing unit execution failure",
					"Failed to execute processing unit: " + puProject.getBuild().getFinalName());
		}
		finally
		{
			// restore the class loader
			getLog().debug("Restoring the ClassLoader.");
			Thread.currentThread().setContextClassLoader(currentCL);
		}
	}
	
	
	
	
	/**
	 * resolves the PU classpath.
	 * It includes the PU executables and dependencies. 
	 * @return a list containing all classpath paths.
	 */
	private List<String> resolveClasspath()
	{
		List<String> dependencyFiles = new ArrayList<String>();
		dependencyFiles.add(puProject.getArtifact().getFile().getAbsolutePath());
		Set dependencyArtifacts = puProject.getArtifacts();
		Iterator i = dependencyArtifacts.iterator();
		while (i.hasNext())
		{
			Artifact artifact = (Artifact)i.next();
			dependencyFiles.add(artifact.getFile().getAbsolutePath());
		}
		return dependencyFiles; 
	}
	
	
	/**
	 * Creates the class loader of the PU application.
	 * @param classpathUrls the classpath to use by the class loader.
	 * @param parent the parent classloader
	 * @return the class loader of the PU application.
	 * @throws MalformedURLException
	 */
	private ClassLoader createClassLoader( List<String> classpathUrls, ClassLoader parent)
	throws MalformedURLException
	{
		// convert classpath strings to URLs
		List<URL> urls = new ArrayList<URL>();
		for (Iterator<String> i = classpathUrls.iterator(); i.hasNext();)
		{
			String url = i.next();
			File f = new File(url);
			urls.add(f.toURL());
		}
		
		// convert to array
		URL[] urlsArray = new URL[urls.size()];
		urls.toArray(urlsArray);
		
		// create the classloader
		ClassLoader urlCL = URLClassLoader.newInstance(urlsArray, parent);
		return urlCL;
	}
	
	
	/**
	 * Runs the PU.
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	public void runPU() throws MojoExecutionException, MojoFailureException 
	{
		getLog().info("Running processing unit: " + puProject.getBuild().getFinalName());
		String[] args = createAttributesArray();
		try
		{
			IntegratedProcessingUnitContainer.main(args);
		}
		catch (Exception e)
		{
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	/**
	 * Creates the attributes array
	 * @return attributes array
	 */
	private String[] createAttributesArray()
	{
		ArrayList<String> attlist = new ArrayList<String>();
		addAttributeToList(attlist, "-cluster", cluster);
		addAttributeToList(attlist, "-proeprties", proeprties);
		getLog().info("Arguments list: " + attlist);
		String[] attArray = new String[attlist.size()];
		attlist.toArray(attArray);
		return attArray;
	}
	
	/**
	 * Adds an attribute with all of its parameters to the list.
	 * @param list the list
	 * @param name the attribute's name
	 * @param value contains the attributes value or parameters
	 */
	private void addAttributeToList(ArrayList<String> list, String name, String value)
	{
		if (value != null)
		{
			getLog().debug("Adding argument to the arguments list: name="+name+" ,value="+value);
			list.add(name);
			StringTokenizer st = new StringTokenizer(value);
			String next;
			while (st.hasMoreTokens())
			{
				next = st.nextToken();
				list.add(next);
			}
		}
	}
}