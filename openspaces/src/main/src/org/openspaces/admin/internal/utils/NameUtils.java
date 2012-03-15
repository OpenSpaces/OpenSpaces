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
package org.openspaces.admin.internal.utils;

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
