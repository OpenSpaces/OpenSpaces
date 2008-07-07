package org.openspaces.maven.plugin;

import java.util.ArrayList;

import org.apache.maven.plugin.AbstractMojo;
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
public class RunHSQLDBViewMojo extends AbstractMojo {
    
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
    public void execute() throws MojoExecutionException, MojoFailureException {
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
            getLog().info("Starting HSQLDB viewer with arguments: " + argList);
            
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
        System.out.println("Usage: mvn os:show-hsqldb [-options]");
        System.out.println("    -Ddriver [jdbc driver class]");
        System.out.println("    -Durl=<name>          : jdbc url (defaults to 'jdbc:hsqldb:hsql://localhost/testDB')");
        System.out.println("    -Duser=<name>         : username used for connection");
        System.out.println("    -Dpassword=<password> : password for this user");
    }
    
}
