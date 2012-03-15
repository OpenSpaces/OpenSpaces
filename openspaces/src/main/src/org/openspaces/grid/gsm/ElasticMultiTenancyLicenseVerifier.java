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
package org.openspaces.grid.gsm;

import com.gigaspaces.internal.license.LicenseManager;

/**
 * Verifies license for multitenancy support when using Elastic Processing Unit.
 * 
 * @author Moran Avigdor
 * @since 8.0.1
 */
public class ElasticMultiTenancyLicenseVerifier {
    
    public static void verify() {
        LicenseManager licenseManager = new LicenseManager();
        licenseManager.verifyLicense();
        
        if (!licenseManager.isLicensedForElasticMultitenancy()) {
            licenseManager.getLogger()
                .warning(
                        "Warning: you have deployed a multitenant elastic processing unit. " +
                        "Multitenant deployments require an add on license which is not part of your current license. " +
                        "Please contact the GigaSpaces support team at support@gigaspaces.com");
        }
    }
}
