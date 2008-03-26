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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.openspaces.pu.container.standalone.StandaloneProcessingUnitContainer;

/**
 * Goal that runs a processing unit as standalone.
 *
 * @goal pu-run-standalone
 * 
 * @requiresProject true
 */
public class RunStandalonePUMojo extends AbstractMojo
{
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
     * puName
     * @parameter expression="${puName}"
     */
    private String puName;
    
    /** 
     * Project instance, used to add new source directory to the build. 
     * @parameter default-value="${project}" 
     * @readonly 
     */ 
    private MavenProject project;
    
    /**
     * executes the mojo.
     */
	public void execute() throws MojoExecutionException, MojoFailureException 
	{
		if (project == null || !project.getPackaging().equalsIgnoreCase("jar"))
		{
			throw new MojoExecutionException("The goal has to be run in a context" +
					" of an OpenSpaces project.");
		}
		List<String> puNames = resolvePUNames();
		if (puNames.size() == 0)
		{
			throw new MojoExecutionException("No processing unit was resolved.");
		}
		try
		{
			Iterator<String> i = puNames.iterator();
			while (i.hasNext())
			{
				String name = i.next();
				getLog().info("Running processing unit: " + name);
				String[] args = createAttributesArray(name);
				getLog().info("Arguments list: " + args);
				StandaloneProcessingUnitContainer.main(args);
			}
		}
		catch (Exception e)
		{
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}
	
	/**
	 * Returns a list of all processing unit names to deploy.
	 * @return processing unit names list
	 */
	private List<String> resolvePUNames()
	{
		List<String> puNames = new LinkedList<String>();
		if (puName != null)
		{
			puNames.add(puName);
		}
		else if (project != null)
		{
			if (project.getPackaging() != null && project.getPackaging().equalsIgnoreCase("pom"))
			{
				getLog().error("Can't run goal 'purun' on multi module project.");
			}
			else
			{
				puNames.add(getPURelativePath(project));
			}
		}
		return puNames;
	}

	/**
	 * Returns the relative path of the processing unit 
	 * @param proj the Maven project
	 * @return the relative path of the processing unit
	 */
	private String getPURelativePath(MavenProject proj)
	{
		String targetDir = proj.getBuild().getDirectory();
		String curDir = System.getProperty("user.dir");
		String relativePath = targetDir.substring(curDir.length()+1);
		relativePath = relativePath.replace("\\", "/");
		String finalName = proj.getBuild().getFinalName();
		String name = relativePath+"/"+finalName+".jar";
		return name;
	}
	
	/**
	 * Creates the attributes array
	 * @return attributes array
	 */
	private String[] createAttributesArray(String name)
	{
		ArrayList<String> attlist = new ArrayList<String>();
		addAttributeToList(attlist, "-cluster", cluster);
		addAttributeToList(attlist, "-proeprties", proeprties);
		attlist.add(name);
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
