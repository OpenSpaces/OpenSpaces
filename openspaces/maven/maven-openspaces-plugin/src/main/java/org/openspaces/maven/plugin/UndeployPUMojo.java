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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.openspaces.pu.container.servicegrid.deploy.Undeploy;


/**
 * Goal that undeploys a processing unit.
 *
 * @goal undeploy
 * 
 * @requiresProject  false
 */
public class UndeployPUMojo extends AbstractMojo
{
	/**
     * groups
     * @parameter expression="${groups}"
     */
    private String groups;
    
    /**
     * locators
     * @parameter expression="${locators}"
     */
    private String locators;
    
    /**
     * timeout
     * @parameter expression="${timeout}" default-value="10000"
     */
    private String timeout;
    
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
				getLog().info("Undeploying processing unit: " + name);
				String[] attributesArray = createAttributesArray(name);
				Undeploy.main(attributesArray);
			}
		}
		catch (Exception e)
		{
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}
	
	/**
	 * Returns a list of all processing unit names to undeploy.
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
				getLog().info("Undeploying processing units of multi module project: " + project.getName());
				List projects = project.getCollectedProjects();
				
				// sort the projects by the order parameter
				Collections.sort(projects, new PUProjectSorter(false));
				
				// create processing unit name for each PU sub project
				Iterator i = projects.iterator();
				while (i.hasNext())
				{
					MavenProject proj = (MavenProject)i.next();
					if (proj.getProperties().getProperty(PUProjectSorter.PARAM_ORDER) != null)
					{
						puNames.add(getPUName(proj));
					}
				}
			}
			else
			{
				puNames.add(getPUName(project));
			}
		}
		return puNames;
	}
	
	/**
	 * Returns the name of the processing unit 
	 * @param proj the Maven project
	 * @return the name of the processing unit
	 */
	private String getPUName(MavenProject proj)
	{
		return proj.getBuild().getFinalName();
	}
	
	/**
	 * Creates the attributes array
	 * @return attributes array
	 */
	private String[] createAttributesArray(String name)
	{
		ArrayList<String> Attlist = new ArrayList<String>();
		addAttributeToList(Attlist, "-groups", groups);
		addAttributeToList(Attlist, "-locators", locators);
		addAttributeToList(Attlist, "-timeout", timeout);
		Attlist.add(name);
		getLog().info("Arguments list: " + Attlist);
		String[] attArray = new String[Attlist.size()];
		Attlist.toArray(attArray);
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
