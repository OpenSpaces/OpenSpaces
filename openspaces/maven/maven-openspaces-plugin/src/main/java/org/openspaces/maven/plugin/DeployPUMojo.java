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
import org.openspaces.pu.container.servicegrid.deploy.Deploy;

/**
 * Goal that deploys a processing unit.
 *
 * @goal deploy
 * 
 * @requiresProject  false
 */
public class DeployPUMojo extends AbstractMojo 
{
	
    /**
     * sla
     * @parameter expression="${sla}"
     */
    private String sla;
    
    /**
     * cluster
     * @parameter expression="${cluster}"
     */
    private String cluster;
    
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
     * proeprties
     * @parameter expression="${proeprties}"
     */
    private String proeprties;
    
    /**
     * override-name
     * @parameter expression="${override-name}"
     */
    private String overrideName;
    
    /**
     * max-instances-per-vm
     * @parameter expression="${max-instances-per-vm}"
     */
    private String maxInstancesPerVm;
    
    /**
     * max-instances-per-machine
     * @parameter expression="${max-instances-per-machine}"
     */
    private String maxInstancesPerMachine;
    
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
	    // Remove white spaces from ClassLoader's URLs
	    ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
	    try 
        {
            Utils.changeClassLoaderToSupportWhiteSpacesRepository(currentCL);
        }
        catch (Exception e) 
        {
            getLog().info("Unable to update ClassLoader. Proceeding with processing unit invocation.", e);
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
				getLog().info("Deploying processing unit: " + name);
				String[] attributesArray = createAttributesArray(name);
				Deploy.main(attributesArray);
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
				getLog().info("Deploying processing units of multi module project: " + project.getName());
				List projects = project.getCollectedProjects();
				
				// sort the projects by the order parameter
				Collections.sort(projects, new PUProjectSorter(true));
				
				// create processing unit name for each PU sub project
				Iterator i = projects.iterator();
				while (i.hasNext())
				{
					MavenProject proj = (MavenProject)i.next();
					if (proj.getProperties().getProperty(PUProjectSorter.PARAM_ORDER) != null)
					{
						puNames.add(getPURelativePath(proj));
					}
				}
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
		ArrayList<String> Attlist = new ArrayList<String>();
		addAttributeToList(Attlist, "-sla", sla);
		addAttributeToList(Attlist, "-cluster", cluster);
		addAttributeToList(Attlist, "-groups", groups);
		addAttributeToList(Attlist, "-locators", locators);
		addAttributeToList(Attlist, "-timeout", timeout);
		addAttributeToList(Attlist, "-proeprties", proeprties);
		addAttributeToList(Attlist, "-override-name", overrideName);
		addAttributeToList(Attlist, "-max-instances-per-vm", maxInstancesPerVm);
		addAttributeToList(Attlist, "-max-instances-per-machine", maxInstancesPerMachine);
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
			getLog().debug("Adding argument to the srguments list: name="+name+" ,value="+value);
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
