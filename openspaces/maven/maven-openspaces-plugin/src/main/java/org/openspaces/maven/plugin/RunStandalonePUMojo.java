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
 * @goal run-standalone
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
     * @parameter expression="${project}" 
     * @required
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
        
		try
		{
			Iterator<String> i = puNames.iterator();
			while (i.hasNext())
			{
				String name = i.next();
				getLog().info("Running processing unit: " + name);
				String[] args = createAttributesArray(name);
				getLog().info("Arguments list: " + args);
				System.out.println("1111111111111");
				StandaloneProcessingUnitContainer.runContainer(args);
				System.out.println("2222222222222");
			}
		}
		catch (Exception e)
		{
		    System.out.println("333333333333");
		    Throwable cause = (Throwable)e;
            while (cause.getCause() != null)
            {
                cause = cause.getCause();
                if (cause instanceof SecurityException)
                {
                    if (cause.getMessage() != null && cause.getMessage().contains("gslicense.xml"))
                    {
                        String msg = 
                            "\nThe GigaSpaces license file - gslicense.xml - was not found in " +
                            "Maven repository.\nThis file should be placed in the directory " +
                            "where gs-boot.jar resides.\n" +
                            "Please try to reinstall OpenSpaces Plugin for Maven by running " +
                            "'installmavenrep' script again.\nAlternatively, copy gslicense.xml " +
                            "manually to <maven-repository-home>/gigaspaces/gs-boot/<version>.";
                        throw new MojoExecutionException(msg);
                    }
                }
            }
            System.out.println("4444444444444");
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}
	
	/**
	 * Returns a list of all processing unit names to deploy.
	 * @return processing unit names list
	 * @throws MojoExecutionException 
	 */
	private List<String> resolvePUNames() throws MojoExecutionException
	{
	    List<String> puNames = new LinkedList<String>();
	    if (project.getPackaging().equalsIgnoreCase("pom"))
        {
	        if (puName != null)
	        {
	            List projects = project.getCollectedProjects();
	            Iterator i = projects.iterator();
	            while (i.hasNext())
	            {
	                MavenProject proj = (MavenProject)i.next();
	                if (proj.getName().equals(puName))
	                {
	                    puNames.add(getPURelativePath(proj));
	                }
	            }
	        }
	        else
	        {
	            throw new MojoExecutionException("Missing argument: puName");
	        }
        }
	    else if (project.getPackaging().equalsIgnoreCase("jar"))
	    {
	        puNames.add(getPURelativePath(project));
	    }
	    else
	    {
	        throw new MojoExecutionException("Unknown project packaging: " + project.getPackaging());
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
