/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.launcher;

import java.io.File;
import java.util.logging.Logger;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.openspaces.pu.container.support.CommandLineParser;

import com.gigaspaces.admin.cli.RuntimeInfo;
import com.gigaspaces.internal.io.FileUtils;
import com.gigaspaces.internal.utils.StringUtils;
import com.gigaspaces.logger.GSLogConfigLoader;

/**
 * @author Guy Korland
 * @since 8.0.4
 */
public class Launcher {

    public static void main(String[] args) throws Exception {
        
        int port = Integer.getInteger("org.openspaces.launcher.port", 8099);
        String name = System.getProperty("org.openspaces.launcher.name", "launcher");
        String path = System.getProperty("org.openspaces.launcher.path", null);
        String work = System.getProperty("org.openspaces.launcher.work", "./work");
        String loggerName = System.getProperty("org.openspaces.launcher.logger", "org.openspaces.launcher");
        String sessionManagerStr = System.getProperty( "org.openspaces.launcher.jetty.session.manager" );
        
        CommandLineParser.Parameter[] params = CommandLineParser.parse(args);
        for (CommandLineParser.Parameter param : params) {
            String paramName = param.getName();
            if ("port".equals(paramName))
                port = Integer.parseInt(param.getArguments()[0]);
            else if ("name".equals(paramName))
                name = param.getArguments()[0];
            else if ("path".equals(paramName))
                path = param.getArguments()[0];
            else if ("work".equals(paramName))
                work = param.getArguments()[0];
            else if ("logger".equals(paramName))
            	loggerName = param.getArguments()[0];
            else if("help".equals(paramName) || "h".equals(paramName)) {
            	printHelpMessage();
            	return;
            }
        }

        GSLogConfigLoader.getLoader(name);
        GSLogConfigLoader.getLoader();
        final Logger logger = Logger.getLogger(loggerName);
        final String warFilePath = getWarFilePath(path, logger);
        if (warFilePath == null)
        	return;
        
        Server server = new Server();
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setReuseAddress( false );
        connector.setPort( port );
        server.setConnectors( new Connector[]{ connector } );
        
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/");
        webAppContext.setWar(warFilePath);
        File tempDir = new File(work);
        tempDir.mkdirs();
        webAppContext.setTempDirectory(tempDir);
        webAppContext.setCopyWebDir(false);
        webAppContext.setParentLoaderPriority(true);
        
        if( sessionManagerStr != null ){
            //change default session manager implementation ( in order to change "JSESSIONID" )
        	//GS-10830, CLOUDIFY-1797
        	try{
        		Class sessionManagerClass = Class.forName( sessionManagerStr );
        		SessionManager sessionManagerImpl = ( SessionManager )sessionManagerClass.newInstance();
        		webAppContext.getSessionHandler().setSessionManager( sessionManagerImpl );
        	}
        	catch( Throwable t ){
        		System.out.println( "Session Manager [" + sessionManagerStr + "] was not set cause following exception:" + t.toString() );
        		t.printStackTrace();
        	}
        }
        else{
        	System.out.println( "Session Manager was not provided" );
        }

        server.setHandler(webAppContext);
        
		logger.info(RuntimeInfo.getShortEnvironmentInfo());
        server.start();
        webAppContext.start();        
        logger.info(name + " server started on port [" + port + "]");
    }

    private static void printHelpMessage() {
        System.out.println("Launcher -path <path> [-work <work>] [-port <port>] [-name <name>] [-logger <logger>]");
    }
    private static String getWarFilePath(String path, Logger logger) {
    	// Verify path is not empty:
    	if (!StringUtils.hasLength(path)) {
    		printHelpMessage();
    		return null;
    	}
    	// Verify path exists:
    	final File file = new File(path);
    	if (!file.exists()) {
    		System.out.println("Path does not exist: " + path);
    		printHelpMessage();
    		return null;
    	}
    	// If File is an actual file, return it:
		if (file.isFile())
			return path;
		// If file is a directory, Get the 1st war file (if any):
		if (file.isDirectory()) {
			File[] warFiles = FileUtils.findFiles(file, null, ".war");
			if (warFiles.length == 0) {
	    		System.out.println("Path does not contain any war files: " + path);
	    		printHelpMessage();
	    		return null;
			}
			final String warFile = warFiles[0].getPath(); 
			if (warFiles.length > 1)
				logger.warning("Found " + warFiles.length + " war files in " + path + ", using " + warFile);
			return warFile;
		}
		System.out.println("Path is neither file nor folder: " + path);
		return null;
    }
}