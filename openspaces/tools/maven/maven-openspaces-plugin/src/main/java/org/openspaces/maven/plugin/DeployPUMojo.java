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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Goal that deploys a processing unit.
 *
 * @goal deploy
 * @requiresProject false
 */
public class DeployPUMojo extends AbstractOpenSpacesMojo {

    /**
     * sla
     *
     * @parameter expression="${sla}"
     */
    private String sla;

    /**
     * cluster
     *
     * @parameter expression="${cluster}"
     */
    private String cluster;

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
     * timeout
     *
     * @parameter expression="${timeout}" default-value="10000"
     */
    private String timeout;

    /**
     * proeprties
     *
     * @parameter expression="${properties}"
     */
    private String properties;

    /**
     * override-name
     *
     * @parameter expression="${override-name}"
     */
    private String overrideName;

    /**
     * max-instances-per-vm
     *
     * @parameter expression="${max-instances-per-vm}"
     */
    private String maxInstancesPerVm;

    /**
     * max-instances-per-machine
     *
     * @parameter expression="${max-instances-per-machine}"
     */
    private String maxInstancesPerMachine;

    /**
     * puName
     *
     * @parameter expression="${module}"
     */
    private String module;

    /**
     * Project instance, used to add new source directory to the build.
     *
     * @parameter default-value="${project}"
     * @readonly
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
     * executes the mojo.
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

        // get a list of project to execute in the order set by the reactor
        List projects = Utils.getProjectsToExecute(reactorProjects, module);
        
        for (Iterator projIt = projects.iterator(); projIt.hasNext();) {
            MavenProject proj = (MavenProject) projIt.next();
            PluginLog.getLog().info("Deploying processing unit: " + proj.getBuild().getFinalName());
            String[] attributesArray = createAttributesArray(Utils.getProcessingUnitJar(proj));
            try {
                Class deployClass = Class.forName("org.openspaces.pu.container.servicegrid.deploy.Deploy", true, Thread.currentThread().getContextClassLoader());
                deployClass.getMethod("main", new Class[]{String[].class}).invoke(null, new Object[]{attributesArray});
            } catch (InvocationTargetException e) {
                throw new MojoExecutionException(e.getTargetException().getMessage(), e.getTargetException());
            } catch (Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

    /**
     * Creates the attributes array
     *
     * @return attributes array
     */
    private String[] createAttributesArray(String name) {
        ArrayList Attlist = new ArrayList();
        Utils.addAttributeToList(Attlist, "-sla", sla);
        Utils.addAttributeToList(Attlist, "-cluster", cluster);
        Utils.addAttributeToList(Attlist, "-groups", groups, ",");
        Utils.addAttributeToList(Attlist, "-locators", locators, ",");
        Utils.addAttributeToList(Attlist, "-timeout", timeout);
        Utils.addAttributeToList(Attlist, "-properties", properties);
        Utils.addAttributeToList(Attlist, "-override-name", overrideName);
        Utils.addAttributeToList(Attlist, "-max-instances-per-vm", maxInstancesPerVm);
        Utils.addAttributeToList(Attlist, "-max-instances-per-machine", maxInstancesPerMachine);
        Attlist.add(name);
        PluginLog.getLog().info("Arguments list: " + Attlist);
        String[] attArray = new String[Attlist.size()];
        Attlist.toArray(attArray);
        return attArray;
    }
}
