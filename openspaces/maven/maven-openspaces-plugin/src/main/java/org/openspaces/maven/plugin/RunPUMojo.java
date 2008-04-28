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
import org.openspaces.core.space.CannotCreateSpaceException;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainer;
import org.springframework.beans.factory.BeanCreationException;

import com.gigaspaces.logger.GSLogConfigLoader;
import com.j_spaces.core.Constants;
import com.j_spaces.kernel.SecurityPolicyLoader;


/**
 * Goal that runs a processing unit.
 *
 * @goal run
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

    private List containers = new ArrayList();
    
    
	public void execute() throws MojoExecutionException, MojoFailureException 
	{
        // save the current class loader
        ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
        // Remove white spaces from ClassLoader's URLs
        try {
            Utils.changeClassLoaderToSupportWhiteSpacesRepository(currentCL);
        } catch (Exception e) {
            getLog().info("Unable to update ClassLoader. Proceeding with processing unit invocation.", e);
        }
        
        GSLogConfigLoader.getLoader();
        if (System.getProperty("java.security.policy") == null) {
            SecurityPolicyLoader.loadPolicy(Constants.System.SYSTEM_GS_POLICY);
        }
        
        List projects = Utils.resolveProjects(project, puName);
        for (Iterator projIt = projects.iterator(); projIt.hasNext();) {
            MavenProject proj = (MavenProject) projIt.next();
            executePU(proj);
        }
        
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    for (int i = (containers.size() - 1); i >= 0; --i) {
                        ((ProcessingUnitContainer) containers.get(i)).close();
                    }
                } finally {
                    mainThread.interrupt();
                }
            }
        });
        while (!mainThread.isInterrupted()) {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                // do nothing, simply exit
            }
        }
	}
	
	
	/**
	 * Prepares and executes the PU.  
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	private void executePU(MavenProject project) throws MojoExecutionException, MojoFailureException {
		if (project == null || !project.getPackaging().equalsIgnoreCase("jar")) {
			throw new MojoExecutionException("The processing unit project '"+ project.getName() +
					"' must be of type jar (packaging=jar).");
		}
		
		// resolve the classpath of the PU to run
		List classpath;
        try {
            classpath = Utils.resolveClasspath(project);
        } catch (Exception e1) {
            throw new MojoExecutionException("Failed to resolve project classpath", e1);
        }
		
		getLog().info("Processing unit [" + project.getName() + "] classpath: " + classpath);

		// run the PU
        getLog().info("Running processing unit: " + project.getBuild().getFinalName());
        String[] args = createAttributesArray();
        ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
        try {
            // create the class loader for the PU execution
            ClassLoader classLoader = Utils.createClassLoader(classpath, currentCL);
            Thread.currentThread().setContextClassLoader(classLoader);
            
            ProcessingUnitContainer container = IntegratedProcessingUnitContainer.createContainer(args);
            containers.add(container);
        } catch (Exception e) {
            printUsage();
            throw Utils.createMojoException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(currentCL);
        }
	}
	
	
	/**
	 * Creates the attributes array
	 * @return attributes array
	 */
	private String[] createAttributesArray()
	{
		ArrayList attlist = new ArrayList();
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
	
	/**
	 * Prints usage instructions.
	 */
	public static void printUsage() {
        System.out.println("Usage: mvn compile os:run [-Dcluster=\"...\"] [-Dproperties=\"...\"] -DpuName=<module-name>");
        System.out.println("    -cluster [cluster properties]: Allows specify cluster parameters");
        System.out.println("             schema=partitioned  : The cluster schema to use");
        System.out.println("             total_members=1,1   : The number of instances and number of backups to use");
        System.out.println("             id=1                : The instance id of this processing unit");
        System.out.println("             backup_id=1         : The backup id of this processing unit");
        System.out.println("    -properties [properties-loc] : Location of context level properties");
        System.out.println("    -properties [bean-name] [properties-loc] : Location of properties used applied only for a specified bean");
        System.out.println("");
        System.out.println("");
        System.out.println("Some Examples:");
        System.out.println("1. -Dcluster=\"schema=partitioned total_members=2 id=1\"");
        System.out.println("    - Starts a processing unit with a partitioned cluster schema of two members with instance id 1");
        System.out.println("2. -Dcluster=\"schema=partitioned total_members=2 id=2\"");
        System.out.println("    - Starts a processing unit with a partitioned cluster schema of two members with instance id 2");
        System.out.println("3. -Dcluster=\"schema=partitioned-sync2backup total_members=2,1 id=1 backup_id=1\"");
        System.out.println("    - Starts a processing unit with a partitioned sync2backup cluster schema of two members with two members each with one backup with instance id of 1 and backup id of 1");
        System.out.println("4. -Dproperties=file://config/context.properties -properties space1 file://config/space1.properties");
        System.out.println("    - Starts a processing unit called data-processor using context level properties called context.properties and bean level properties called space1.properties applied to bean named space1");
        System.out.println("5. -Dproperties=embed://prop1=value1 -properties space1 embed://prop2=value2;prop3=value3");
        System.out.println("    - Starts a processing unit called data-processor using context level properties with a single property called prop1 with value1 and bean level properties with two properties");
        System.out.println("");
    }
}