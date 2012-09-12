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

import com.gigaspaces.admin.cli.RuntimeInfo;
import com.gigaspaces.logger.GSLogConfigLoader;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.openspaces.pu.container.support.CommandLineParser;

import java.io.File;
import java.util.logging.Logger;

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
        String logger = System.getProperty("org.openspaces.launcher.logger", "org.openspaces.launcher"); 
        boolean help  = false;
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
                logger = param.getArguments()[0];
            else if("help".equals(paramName) || "h".equals(paramName))
                help = true;
        }
        if(path==null || help){
            System.out.println("Launcher -path <path> [-work <work>] [-port <port>] [-name <name>] [-logger <logger>]");
            return;
        }
        GSLogConfigLoader.getLoader(name);
        GSLogConfigLoader.getLoader();
        Server server = new Server(port);
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/");
        webAppContext.setWar(path);
        File tempDir = new File(work);
        tempDir.mkdirs();
        webAppContext.setTempDirectory(tempDir);
        webAppContext.setCopyWebDir(false);
        webAppContext.setParentLoaderPriority(true);

        server.setHandler(webAppContext);
        
        Logger.getLogger(logger).info(RuntimeInfo.getShortEnvironmentInfo());
        
        server.start();
        webAppContext.start();
        
        Logger.getLogger(logger).info(name + " server started on port [" + port + "]");
    }
}
