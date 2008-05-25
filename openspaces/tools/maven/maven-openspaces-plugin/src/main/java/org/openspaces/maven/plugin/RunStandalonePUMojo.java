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

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Goal that runs a processing unit as standalone.
 *
 * @goal run-standalone
 * @requiresProject true
 */
public class RunStandalonePUMojo extends AbstractMojo {

    /**
     * cluster
     *
     * @parameter expression="${cluster}"
     */
    private String cluster;


    /**
     * proeprties
     *
     * @parameter expression="${proeprties}"
     */
    private String proeprties;


    /**
     * puName
     *
     * @parameter expression="${module}"
     */
    private String module;


    /**
     * Project instance, used to add new source directory to the build.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;
    
    
    /**
     * Project instance, used to add new source directory to the build.
     *
     * @parameter default-value="${reactorProjects}"
     * @readonly
     */
    private List reactorProjects;
    
    /**
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    protected ArtifactRepository localRepository;


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

        Utils.handleSecurity();

        try {
            sharedClassLoader = Utils.createClassLoader(new ArrayList(), Thread.currentThread().getContextClassLoader());
        } catch (Exception e1) {
            throw new MojoExecutionException("Failed to create ClassLoader", e1);
        }

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
            throw new MojoExecutionException("The processing unit project '" + project.getName() +
                    "' must be of type jar (packaging=jar).");
        }

        // run the PU
        getLog().info("Running processing unit: " + project.getBuild().getFinalName());

        ContainerRunnable conatinerRunnable = new ContainerRunnable("org.openspaces.pu.container.standalone.StandaloneProcessingUnitContainer", createAttributesArray(Utils.getProcessingUnitJar((project))));
        Thread thread = new Thread(conatinerRunnable, "Processing Unit [" + project.getBuild().getFinalName() + "]");
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
    private String[] createAttributesArray(String name) {
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
        System.out.println("Usage: mvn os:run-standalone [-Dcluster=\"...\"] [-Dproperties=\"...\"] [-Dmodule=<module name>]");
        System.out.println("    -Dmodule [module name]        : The name of the module to run. If none is specified, will run all the PU modules");
        System.out.println("    -Dcluster [cluster properties]: Allows specify cluster parameters");
        System.out.println("             schema=partitioned  : The cluster schema to use");
        System.out.println("             total_members=1,1   : The number of instances and number of backups to use");
        System.out.println("             id=1                : The instance id of this processing unit");
        System.out.println("             backup_id=1         : The backup id of this processing unit");
        System.out.println("    -Dproperties [properties-loc] : Location of context level properties");
        System.out.println("    -Dproperties [bean-name] [properties-loc] : Location of properties used applied only for a specified bean");
   }
}