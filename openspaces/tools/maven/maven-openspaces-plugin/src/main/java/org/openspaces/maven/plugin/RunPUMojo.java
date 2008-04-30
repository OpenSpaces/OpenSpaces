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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * Goal that runs a processing unit.
 *
 * @goal run
 * @requiresProject false
 * @description Runs ...
 */
public class RunPUMojo extends AbstractMojo {
    /**
     * The classpath elements of the project being tested.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;


    /**
     * The classpath elements of the project being tested.
     *
     * @parameter expression="${puName}"
     * @readonly
     */
    private String puName;


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
     * Container list.
     */
    private List containers = new ArrayList();


    /**
     * Executed the Mojo.
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

        ClassLoader classLoader;
        try {
            List classpath = Utils.resolveClasspath(project);
            getLog().info("Processing unit [" + project.getName() + "] classpath: " + classpath);
            classLoader = Utils.createClassLoader(classpath, null);
        } catch (Exception e1) {
            throw new MojoExecutionException("Failed to resolve project classpath", e1);
        }
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
        Utils.addAttributeToList(attlist, "-proeprties", proeprties);
        getLog().info("Arguments list: " + attlist);
        String[] attArray = new String[attlist.size()];
        attlist.toArray(attArray);
        return attArray;
    }


    /**
     * Prints usage instructions.
     */
    public static void printUsage() {
        System.out.println("Usage: mvn compile os:run [-Dcluster=\"...\"] [-Dproperties=\"...\"] -DpuName=<module-name>");
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
        System.out.println("1. -Dcluster=\"schema=partitioned total_members=2 id=1\"");
        System.out.println("    - Starts a processing unit with a partitioned cluster schema of two members with instance id 1");
        System.out.println("2. -Dcluster=\"schema=partitioned total_members=2 id=2\"");
        System.out.println("    - Starts a processing unit with a partitioned cluster schema of two members with instance id 2");
        System.out.println("3. -Dcluster=\"schema=partitioned-sync2backup total_members=2,1 id=1 backup_id=1\"");
        System.out.println("    - Starts a processing unit with a partitioned sync2backup cluster schema of two members with two members each with one backup with instance id of 1 and backup id of 1");
        System.out.println("4. -Dproperties=file://config/context.properties -Dproperties space1 file://config/space1.properties");
        System.out.println("    - Starts a processing unit called data-processor using context level properties called context.properties and bean level properties called space1.properties applied to bean named space1");
        System.out.println("5. -Dproperties=embed://prop1=value1 -Dproperties space1 embed://prop2=value2;prop3=value3");
        System.out.println("    - Starts a processing unit called data-processor using context level properties with a single property called prop1 with value1 and bean level properties with two properties");
        System.out.println("");
    }
}