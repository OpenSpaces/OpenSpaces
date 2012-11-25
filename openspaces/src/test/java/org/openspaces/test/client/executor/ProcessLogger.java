package org.openspaces.test.client.executor;

import java.text.DateFormat;
import java.util.Date;

/**
 * A logger running on the remote process.
 * We can't use the java.util.logging since we run the risk of overriding
 * the any logging properties the remote process may have.
 * 
 * @author moran
 */
public class ProcessLogger {

    
    private final static DateFormat timeFormatter = DateFormat.getTimeInstance();
    private final static DateFormat dateFormatter = DateFormat.getDateInstance();
    
    public static void log(String message) {
        doLog(message, null);
    }
    
    public static void log(String message, Throwable t) {
        doLog(message, t);
    }
    
    private static void doLog(String message, Throwable t) {

        if (t != null)
            message += " " + ExecutorUtils.getStackTrace( t);
        
        String caller = "";
        
        Date date = new Date();
        System.out.println( "\n"+ dateFormatter.format( date) +" - " + timeFormatter.format( date) + " [host: "
                + ExecutorUtils.getHostName() + "]" + caller + "\n  " + message);
    }
}
