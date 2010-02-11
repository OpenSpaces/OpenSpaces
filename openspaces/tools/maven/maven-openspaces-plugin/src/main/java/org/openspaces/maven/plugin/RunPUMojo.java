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

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Goal that runs a processing unit.
 *
 * @goal run
 * @requiresProject false
 * @description Runs a processing unit from the from the project's output directory
 */
public class RunPUMojo extends AbstractOpenSpacesMojo {

    /**
     * The classpath elements of the project being tested.
     *
     * @parameter expression="${module}"
     * @readonly
     */
    private String module;


    /**
     * cluster
     *
     * @parameter expression="${cluster}"
     */
    private String cluster;


    /**
     * proeprties
     *
     * @parameter expression="${properties}"
     */
    private String properties;

    
    /**
     * groups
     *
     * @parameter expression="${groups}"
     */
    private String groups;

    
    /**
     * locators
     *
     * @parameter expression="${locators}"
     */
    private String locators;


    /**
     * Container list.
     */
    private List containers = new ArrayList();
    
    
    /**
    * The remote repositories.
    * 
    * @parameter expression="${project.remoteArtifactRepositories}"
    */
    private List remoteRepositories;
   
   
    /**
     * Project instance, used to add new source directory to the build.
     *
     * @parameter default-value="${reactorProjects}"
     * @readonly
     */
    private List reactorProjects;

    
    /**
     * The local repository.
     * 
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
     private ArtifactRepository localRepository;
     
     
     /**
      * The scopes for dependencies inclusion.
      *
      * @parameter default-value="compile,provided,runtime,system"
      */
     private String scopes;
    
    
     /**
      * @component
      */
     private ArtifactResolver artifactResolver;
     
     
     /**
      *
      * @component
      */
     private ArtifactFactory artifactFactory;

     
     /**
     *
     * @component
     */
     private ArtifactMetadataSource metadataSource;
     
     
     /**
     * The dependency tree builder to use.
     * 
     * @component
     */
     private DependencyTreeBuilder dependencyTreeBuilder; 

     
     /**
      * The artifact collector to use.
      * 
      * @component
      */
     private ArtifactCollector artifactCollector;
     
     
    /**
     * Executed the Mojo.
     */
    public void executeMojo() throws MojoExecutionException, MojoFailureException {
        // Remove white spaces from ClassLoader's URLs
        ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
        try {
            Utils.changeClassLoaderToSupportWhiteSpacesRepository(currentCL);
        } catch (Exception e) {
            PluginLog.getLog().info("Unable to update ClassLoader. Proceeding with processing unit invocation.", e);
        }

        Utils.handleSecurity();
        try {
            ClassUtils.forName("com.gigaspaces.logger.GSLogConfigLoader").getMethod("getLoader", new Class[0]).invoke(null, new Object[0]);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to configure logging", e);
        }

        System.setProperty("com.gs.printRuntimeInfo", "false");

        // get a list of project to execute in the order set by the reactor
        List projects = Utils.getProjectsToExecute(reactorProjects, module);
        
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
     *
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    private void executePU(MavenProject project) throws MojoExecutionException, MojoFailureException {
        if (project == null || !project.getPackaging().equalsIgnoreCase("jar")) {
            throw new MojoExecutionException("The processing unit project '" + (project == null ? "unknown" : project.getName()) +
                    "' must be of type jar (packaging=jar).");
        }

        // run the PU
        PluginLog.getLog().info("Running processing unit: " + project.getBuild().getFinalName());
        
        // resolve the classpath for the execution of the processing unit
        List classpath = null;
        ClassLoader classLoader = null;
        try {
            String[] includeScopes = Utils.convertCommaSeparatedListToArray(scopes);
            classpath = Utils.resolveExecutionClasspath(project, includeScopes, true, reactorProjects, dependencyTreeBuilder,
                    metadataSource, artifactCollector, artifactResolver, artifactFactory, localRepository, remoteRepositories);
            PluginLog.getLog().info("Processing unit [" + project.getName() + "] classpath: " + classpath);
            classLoader = Utils.createClassLoader(classpath, null);
        } catch (Exception e1) {
            throw new MojoExecutionException("Failed to resolve the processing unit's  classpath", e1);
        }
        
        // set groups
        if (groups != null && !groups.trim().equals("")) {
            System.setProperty("com.gs.jini_lus.groups", groups);
        }
        
        // set locators
        if (locators != null && !locators.trim().equals("")) {
            System.setProperty("com.gs.jini_lus.locators", locators);
        }
        
        // execute the processing unit in the new class loader 
        ContainerRunnable conatinerRunnable = new ContainerRunnable("org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainer", createAttributesArray());
        Thread thread = new Thread(conatinerRunnable, "Processing Unit [" + project.getBuild().getFinalName() + "]");
        thread.setContextClassLoader(classLoader);
        thread.start();
        while (!conatinerRunnable.hasStarted()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
        }
        if (conatinerRunnable.getException() != null) {
            Utils.throwMissingLicenseException(conatinerRunnable.getException(), localRepository);
            throw new MojoExecutionException("Failed to start processing unit [" + project.getBuild().getFinalName() + "]", conatinerRunnable.getException());
        }
        containers.add(thread);
    }
    
    /**
     * Creates the attributes array
     *
     * @return attributes array
     */
    private String[] createAttributesArray() {
        ArrayList attlist = new ArrayList();
        Utils.addAttributeToList(attlist, "-cluster", cluster);
        Utils.addAttributeToList(attlist, "-properties", properties);
        PluginLog.getLog().info("Arguments list: " + attlist);
        String[] attArray = new String[attlist.size()];
        attlist.toArray(attArray);
        return attArray;
    }


    /**
     * Prints usage instructions.
     */
    public static void printUsage() {
        System.out.println("Usage: mvn compile os:run [-Dcluster=\"...\"] [-Dgroups=<groups>] [-Dlocators=<locators>] [-Dproperties=\"...\"] [-Dmodule=<module name>]");
        System.out.println("    -Dmodule [module name]          : The name of the module to run. If none is specified, will run all the PU modules");
        System.out.println("    -Dcluster [cluster properties]  : Space separated cluster parameters");
        System.out.println("               schema=partitioned   : The cluster schema to use");
        System.out.println("               total_members=1,1    : The number of instances and number of backups to use");
        System.out.println("               id=1                 : The instance id of this processing unit");
        System.out.println("               backup_id=1          : The backup id of this processing unit");
        System.out.println("    -Dgroups [Jini groups]          : Comma separated list of Jini lookup groups");
        System.out.println("    -Dlocators [Jini locator hosts] : Comma separated list of Jini locator hosts");
        System.out.println("    -Dproperties [properties-loc]   : Location of context level properties");
        System.out.println("");
        System.out.println("");
        System.out.println("Some Examples:");
        System.out.println("1. -Dcluster=\"schema=partitioned total_members=2 id=1\"");
        System.out.println("    - Starts a processing unit with a partitioned cluster schema of two members with instance id 1");
        System.out.println("2. -Dcluster=\"schema=partitioned total_members=2 id=2\"");
        System.out.println("    - Starts a processing unit with a partitioned cluster schema of two members with instance id 2");
        System.out.println("3. -Dcluster=\"schema=partitioned-sync2backup total_members=2,1 id=1 backup_id=1\"");
        System.out.println("    - Starts a processing unit with a partitioned sync2backup cluster schema of two members with two members each with one backup with instance id of 1 and backup id of 1");
        System.out.println("4. -Dproperties=file://config/context.properties");
        System.out.println("    - Starts a processing unit called data-processor using context level properties called context.properties");
        System.out.println("5. -Dproperties=embed://prop1=value1");
        System.out.println("    - Starts a processing unit called data-processor using context level properties with a single property called prop1 with value1");
        System.out.println("");
    }
}