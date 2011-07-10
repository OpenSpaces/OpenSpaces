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
        
        int port = Integer.getInteger("org.openspaces.launcher.port", 8099); // backward compatibility
        String name = "launcher";
        String path = null;
        String work = "./work";
        String logger = "org.openspaces.launcher"; 
        boolean help  = false;
        CommandLineParser.Parameter[] params = CommandLineParser.parse(args);
        for (CommandLineParser.Parameter param : params) {
            if ("port".equals(param.getName())) {
                port = Integer.parseInt(param.getArguments()[0]);
            }else if ("name".equals(param.getName())){
                name = param.getArguments()[0];
            }else if ("path".equals(param.getName())){
                path = param.getArguments()[0];
            }
            else if ("work".equals(param.getName())){
                work = param.getArguments()[0];
            }
            else if ("logger".equals(param.getName())){
                logger = param.getArguments()[0];
            }
            else if("help".equals(param.getName()) || "h".equals(param.getName())){
                help = true;
            }
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
