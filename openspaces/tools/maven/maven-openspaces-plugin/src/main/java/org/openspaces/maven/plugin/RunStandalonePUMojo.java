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
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.j_spaces.core.Constants;
import com.j_spaces.kernel.SecurityPolicyLoader;

/**
 * Goal that runs a processing unit as standalone.
 *
 * @goal run-standalone
 * 
 * @requiresProject true
 */
public class RunStandalonePUMojo extends AbstractMojo {
    
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
     * Container list.
     */
    private List containers = new ArrayList();

    private ClassLoader sharedClassLoader;
    
    /**
     * executes the mojo.
     */
	public void execute() throws MojoExecutionException, MojoFailureException {
	    
	    // Remove white spaces from ClassLoader's URLs 
        ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
        try {
            Utils.changeClassLoaderToSupportWhiteSpacesRepository(currentCL);
        } catch (Exception e) {
            getLog().info("Unable to update ClassLoader. Proceeding with processing unit invocation.", e);
        }
        
        if (System.getProperty("java.security.policy") == null) {
            SecurityPolicyLoader.loadPolicy(Constants.System.SYSTEM_GS_POLICY);
        }
        
        try {
            sharedClassLoader = Utils.createClassLoader(new ArrayList(), Thread.currentThread().getContextClassLoader());
        } catch (Exception e1) {
            throw new MojoExecutionException("Failed to create ClassLoader", e1);
        }
        
        List projects = Utils.resolveProjects(project, puName);
        
        // sort the projects by the order parameter
        Collections.sort(projects, new PUProjectSorter(true));
        
        for (Iterator projIt = projects.iterator(); projIt.hasNext();) {
            MavenProject proj = (MavenProject) projIt.next();
            executePU(proj);
        }
        
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    for (int i = (containers.size() - 1); i >= 0; --i) {
                        ((Thread) containers.get(i)).interrupt();
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
        
        // run the PU
        getLog().info("Running processing unit: " + project.getName());

        
        // iterate over target/[]dir/[]/shared-lib/*.jar and add it (if does not exists in class loader) 
        // into the shared class loader
        try {
            Utils.addSharedLibToClassLoader(project, this.sharedClassLoader);
        } catch (Exception e1) {
            throw new MojoExecutionException("Failed to add shared-lib jars of processing unit ["+ project.getName() +"]", e1);
        }
        
        ContainerRunnable conatinerRunnable = new ContainerRunnable("org.openspaces.pu.container.standalone.StandaloneProcessingUnitContainer", createAttributesArray(Utils.getProcessingUnitJar((project))));
        Thread thread = new Thread(conatinerRunnable, "Processing Unit [" + project.getName() + "]");
        thread.setContextClassLoader(sharedClassLoader);
        thread.start();
        while (!conatinerRunnable.hasStarted()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
        }
        if (conatinerRunnable.getException() != null) {
            throw new MojoExecutionException("Failed to start processing unit [" + project.getName() + "]", conatinerRunnable.getException());
        }
        containers.add(thread);
    }
    
    

	/**
	 * Creates the attributes array
	 * @return attributes array
	 */
	private String[] createAttributesArray(String name)	{
		ArrayList attlist = new ArrayList();
		Utils.addAttributeToList(attlist, "-cluster", cluster);
		Utils.addAttributeToList(attlist, "-proeprties", proeprties);
		attlist.add(name);
		getLog().info("Arguments list: " + attlist);
		String[] attArray = new String[attlist.size()];
		attlist.toArray(attArray);
		return attArray;
	}
	
	
	/**
     * Prints usage instructions.
     */
	public static void printUsage() {
        System.out.println("Usage: puInstance [-Dcluster=\"...\"] [-Dproperties=\"...\"] pu-location");
        System.out.println("    pu-location                  : The relative/absolute path to a processing unit directory location");
        System.out.println("    -Dcluster [cluster properties]: Allows specify cluster parameters");
        System.out.println("             schema=partitioned  : The cluster schema to use");
        System.out.println("             total_members=1,1   : The number of instances and number of backups to use");
        System.out.println("             id=1                : The instance id of this processing unit");
        System.out.println("             backup_id=1         : The backup id of this processing unit");
        System.out.println("    -Dproperties [properties-loc] : Location of context level properties");
        System.out.println("    -Dproperties [bean-name] [properties-loc] : Location of properties used applied only for a specified bean");
        System.out.println("");
        System.out.println("");
        System.out.println("Some Examples:");
        System.out.println("1. puInstance ../examples/openspaces/data/processor/pu/data-processor");
        System.out.println("    - Starts a processing unit with a directory location of ../examples/openspaces/data/processor/pu/data-processor");
        System.out.println("1. puInstance -Dcluster=\"schema=partitioned total_members=2 id=1\" ../deploy/data-processor");
        System.out.println("    - Starts a processing unit with a partitioned cluster schema of two members with instance id 1");
        System.out.println("2. puInstance -Dcluster=\"schema=partitioned total_members=2 id=2\" ../deploy/data-processor");
        System.out.println("    - Starts a processing unit with a partitioned cluster schema of two members with instance id 2");
        System.out.println("3. puInstance -Dcluster=\"schema=partitioned-sync2backup total_members=2,1 id=1 backup_id=1\" ../deploy/data-processor");
        System.out.println("    - Starts a processing unit with a partitioned sync2backup cluster schema of two members with two members each with one backup with instance id of 1 and backup id of 1");
        System.out.println("4. puInstance -Dproperties=\"file://config/context.properties\" -Dproperties=\"space1 file://config/space1.properties\" ../deploy/data-processor");
        System.out.println("    - Starts a processing unit called data-processor using context level properties called context.properties and bean level properties called space1.properties applied to bean named space1");
        System.out.println("5. puInstance -Dproperties=\"embed://prop1=value1\" -Dproperties=\"space1 embed://prop2=value2;prop3=value3\" ../deploy/data-processor");
        System.out.println("    - Starts a processing unit called data-processor using context level properties with a single property called prop1 with value1 and bean level properties with two properties");
    }
}