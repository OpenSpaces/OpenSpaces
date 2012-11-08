/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.openspaces.core.properties.BeanLevelProperties;

import com.gigaspaces.internal.jvm.JVMDetails;

/**
 * Helper class for filter Admin services
 * @see AdminFilter
 * @since 9.1.1
 * @author evgenyf
 */
public class AdminFilterHelper {

    public static boolean acceptJvm( AdminFilter adminFilter, JVMDetails jvmDetails ){
        return acceptJvm( 
                   adminFilter, jvmDetails.getEnvironmentVariables(), jvmDetails.getSystemProperties() );
    }
    
    public static boolean acceptJvm( AdminFilter adminFilter, 
                            Map<String,String> envVariables, Map<String,String> sysProperties ){
        
        return adminFilter == null ? true : 
                    adminFilter.acceptJavaVirtualMachine( envVariables, sysProperties );
    }
    
    public static boolean acceptProcessingUnit( AdminFilter adminFilter, 
                                                BeanLevelProperties beanLevelProperties ){
        
        Properties contextProperties = null;
        if( beanLevelProperties == null ){
            contextProperties = new Properties();
        }
        else{
            contextProperties = beanLevelProperties.getContextProperties();
            if( contextProperties == null ){
                contextProperties = new Properties();
            }
        }
                
        return adminFilter == null ? true : adminFilter.acceptProcessingUnit( 
                                    new HashMap<String, String>( ( Map )contextProperties ) );
    }
}