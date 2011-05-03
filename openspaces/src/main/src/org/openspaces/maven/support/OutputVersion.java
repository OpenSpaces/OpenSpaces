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

/**
 * Outputs the version to use with Maven. Following the following rules:
 *
 * 1. If this is patch, then concatenate build number to product version 6.5-3500  
 * 2. If this is GA, then just 6.5 will be used.
 * 3. If this is a final milestone, then 6.5-[milestone] will be used.
 * 4. If this is an internal milestone, then 6.5-[milestone]-[build number] will be used.
 *
 * @author kimchy
 */
public class OutputVersion {

    public static String computeVersion() {
        
        if(PlatformVersion.getBuildType().contains("patch")) {
            return PlatformVersion.getVersion() + "-" + PlatformVersion.getBuildNumber();
        }
        
        if (PlatformVersion.getMilestone().equalsIgnoreCase("GA")) {
            return PlatformVersion.getVersion();
        }
        
        if (PlatformVersion.getBuildNumber().indexOf("-") == -1) {
            return PlatformVersion.getVersion() + "-" + PlatformVersion.getMilestone();
        }
        
        return PlatformVersion.getVersion() + "-" + PlatformVersion.getMilestone() + "-" + PlatformVersion.getBuildNumber();
        
    }

    public static void main(String[] args) throws Exception {
        System.out.println(computeVersion());
    }
}
