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
