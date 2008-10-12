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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.hsqldb.util.DatabaseManagerSwing;
import org.springframework.util.StringUtils;

/**
 * Goal that runs a processing unit.
 *
 * @goal hsql-ui
 * @requiresProject false
 * @description Runs the HSQLDB viewer.
 */
public class RunHSQLDBViewMojo extends AbstractOpenSpacesMojo {
    
    /**
     * driver
     *
     * @parameter expression="${driver}"
     */
    private String driver;
    
    /**
     * url
     *
     * @parameter expression="${url}" default-value="jdbc:hsqldb:hsql://localhost/testDB"
     */
    private String url;
    
    /**
     * user
     *
     * @parameter expression="${user}"
     */
    private String user;
    
    /**
     * password
     *
     * @parameter expression="${password}"
     */
    private String password;
    
    /**
     * help
     *
     * @parameter expression="${help}"
     */
    private String help;
    
    
    /** Executes the Mojo **/
    public void executeMojo() throws MojoExecutionException, MojoFailureException {
        if (help != null) {
            printUsage();
        }
        else {
            ArrayList argList = new ArrayList();
            // handles the case when the user specifies an empty string for driver
            // in that case driver gets the value 'true'
            if (StringUtils.hasText(driver) && !driver.equals("true")) {
                argList.add("-driver");
                argList.add(driver);
            }
            // handles the case when the user specifies an empty string for driver
            // in that case url gets the value 'true'
            if (StringUtils.hasText(url) && !url.equals("true")) {
                argList.add("-url");
                argList.add(url);
            }
            if (StringUtils.hasText(user)) {
                argList.add("-user");
                argList.add(user);
            }
            if (StringUtils.hasText(password)) {
                argList.add("-password");
                argList.add(password);
            }
            
            // create the arguments array
            String[] args = new String[argList.size()];
            argList.toArray(args);
            PluginLog.getLog().info("Starting HSQLDB viewer with arguments: " + argList);
            
            // start the viewer and sleep forever
            DatabaseManagerSwing.main(args);
            try {
                Thread.currentThread().sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    /** prints usage options **/
    private void printUsage() {
        System.out.println("Usage: mvn os:hsql-ui [-options]");
        System.out.println("    -Ddriver=<driver class> : jdbc driver class (defaults to 'org.hsqldb.jdbcDriver')");
        System.out.println("    -Durl=<url>             : jdbc url (defaults to 'jdbc:hsqldb:hsql://localhost/testDB')");
        System.out.println("    -Duser=<user>           : username used for connection");
        System.out.println("    -Dpassword=<password>   : password for this user");
        System.out.println("    -Dhelp                  : prints the usage options");
    }
    
}
