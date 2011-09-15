package org.openspaces.admin.internal;

/**
 * 
 * Utility class for calculating name of Admin instances
 * 
 * @author	evgenyf
 * @version	1.0
 * @since	8.0.5
 */
public class NameUtils {

    /**
     * Method calculates full space instance of processing unit instance name
     * @param name processing unit name
     * @param instanceId instance id
     * @param backupId backup id - can be null
     * @param numOfBackups number of planned backup instances
     * @return space instance name
     */
    public static String getSpaceInstanceName( String name, Integer instanceId, 
                                                Integer backupId, int numOfBackups ){
        StringBuilder strBuilder = new StringBuilder( name );

        if( numOfBackups > 0 ){
            int bid = backupId != null ? backupId : Integer.valueOf( 0 );
            
            strBuilder.append( "." );
            strBuilder.append( instanceId );
            strBuilder.append( " [" );
            strBuilder.append( ( bid +1 ) );
            strBuilder.append( "]" );
        }
        else{
            strBuilder.append( " [" );
            strBuilder.append( instanceId );
            strBuilder.append( "]" );            
        }
        
        return strBuilder.toString();        
    }
}