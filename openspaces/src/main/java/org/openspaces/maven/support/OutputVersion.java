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

package org.openspaces.maven.support;

import com.j_spaces.kernel.PlatformVersion;
import com.j_spaces.kernel.XapVersion;

/**
 * Outputs the version to use with Maven. Following the following rules:
 *
 * 1. If this is an internal milestone, then 9.1.0-SNAPSHOT will be used.
 * 2. If this is patch, then concatenate build number to product release version 9.1.0-RELEASE-7510  
 * 3. If this is GA, then just 9.1.0-RELEASE will be used.
 * 4. If this is a final milestone, then 9.1.0-[MILESTONE] will be used.
 *
 * @author kimchy
 */
public class OutputVersion {

    public static String computeVersion() {
        
        if (PlatformVersion.getBuildNumber().indexOf("-") != -1) {
            return PlatformVersion.getVersion() + "-" + PlatformVersion.getBuildTimestamp() + "-SNAPSHOT";
        }
  
        if (PlatformVersion.getMilestone().equalsIgnoreCase("GA")) {
            return PlatformVersion.getVersion() + "-" + PlatformVersion.getBuildTimestamp() + "-RELEASE";
        }
        
        if (PlatformVersion.getBuildNumber().indexOf("-") == -1) {
            return PlatformVersion.getVersion() + "-" + PlatformVersion.getBuildTimestamp() + "-" + PlatformVersion.getMilestone().toUpperCase();
        }
        
        return "";
        
    }
    
    public static String computeXapVersion() {
        
        XapVersion xapVersion = new XapVersion();
        
        if (xapVersion.getBuildNumber().indexOf("-") != -1) {
            return xapVersion.getVersion() + "-" + xapVersion.getBuildTimestamp() + "-SNAPSHOT";
        }
        
        if (xapVersion.getMilestone().equalsIgnoreCase("GA")) {
            return xapVersion.getVersion() + "-" + xapVersion.getBuildTimestamp() + "-RELEASE";
        }
        
        if (xapVersion.getBuildNumber().indexOf("-") == -1) {
            return xapVersion.getVersion() + "-" + xapVersion.getBuildTimestamp() + "-" + xapVersion.getMilestone().toUpperCase();
        }
        
        return "";
    }
    
    public static String computeCloudifyVersion() {

        //CloudifyVersion class is resolved by PlatformVersion.
        //class is not in the default classpath - only in cloudify build.
        return computeVersion();
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("usage error");
        }

        if (args[0].equals("edition")) {
            String edition = PlatformVersion.getEdition();
            if (edition.startsWith(PlatformVersion.EDITION_XAP)) {
                System.out.println(PlatformVersion.EDITION_XAP);
            } else {
                System.out.println(PlatformVersion.EDITION_CLOUDIFY);
            }
        } else if (args[0].equals(PlatformVersion.EDITION_XAP)) {
            System.out.println(computeXapVersion());
        } else if (args[0].equals(PlatformVersion.EDITION_CLOUDIFY)) {
            System.out.println(computeCloudifyVersion());
        }
    }
}
