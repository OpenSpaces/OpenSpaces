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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;


/**
 * Goal that undeploys a processing unit.
 *
 * @goal undeploy
 * @requiresProject false
 */
public class UndeployPUMojo extends AbstractOpenSpacesMojo {

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
        Utils.handleSecurity();

        // get a list of project to execute in the order set by the reactor
        List projects = Utils.getProjectsToExecute(reactorProjects, module);
        
        // in undeploy reverse the order of projects
        Collections.reverse(projects);

        int failureCount = 0;
        Throwable lastException = null;
        for (Iterator projIt = projects.iterator(); projIt.hasNext();) {
            MavenProject proj = (MavenProject) projIt.next();
            PluginLog.getLog().info("Undeploying processing unit: " + proj.getBuild().getFinalName());
            String[] attributesArray = createAttributesArray(proj.getBuild().getFinalName());
            try {
                Class deployClass = Class.forName("org.openspaces.pu.container.servicegrid.deploy.Undeploy", true, Thread.currentThread().getContextClassLoader());
                deployClass.getMethod("main", new Class[]{String[].class}).invoke(null, new Object[]{attributesArray});
            } catch (InvocationTargetException e) {
                lastException = e.getTargetException();
                failureCount++;
                PluginLog.getLog().info("Failed to undeploy processing unit: " + proj.getBuild().getFinalName() + " reason: " + e.getTargetException().getMessage());
            } catch (Exception e) {
                lastException = e;
                PluginLog.getLog().info("Failed to undeploy processing unit: " + proj.getBuild().getFinalName() + " reason: " + e.getMessage());
            }
        }
        if (failureCount == projects.size() && lastException != null) {
            throw new MojoExecutionException(lastException.getMessage(), lastException);
        }
    }


    /**
     * Creates the attributes array
     *
     * @return attributes array
     */
    private String[] createAttributesArray(String name) {
        ArrayList Attlist = new ArrayList();
        Utils.addAttributeToList(Attlist, "-groups", groups, ",");
        Utils.addAttributeToList(Attlist, "-locators", locators, ",");
        Utils.addAttributeToList(Attlist, "-timeout", timeout);
        Attlist.add(name);
        PluginLog.getLog().info("Arguments list: " + Attlist);
        String[] attArray = new String[Attlist.size()];
        Attlist.toArray(attArray);
        return attArray;
    }
}