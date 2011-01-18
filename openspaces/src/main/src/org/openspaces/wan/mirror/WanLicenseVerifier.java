package org.openspaces.wan.mirror;

import static com.j_spaces.core.Constants.Container.CONTAINER_LICENSEKEY_FILE_NAME;
import static com.j_spaces.core.Constants.Container.CONTAINER_LICENSEKEY_PROP;
import static com.j_spaces.core.Constants.Container.LICENSE_KEY_SYS_PROP;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.ParseException;
import java.util.Properties;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.gigaspaces.internal.license.LicenseManagerVerifier;
import com.gigaspaces.start.Locator;
import com.j_spaces.kernel.Environment;
import com.j_spaces.kernel.JSpaceUtilities;
import com.j_spaces.kernel.ResourceLoader;
import com.j_spaces.kernel.log.JProperties;

public class WanLicenseVerifier {

    private static java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(WanLicenseVerifier.class.getName());

    private String getLicenseKey(LicenseManagerVerifier lm) {

        String licenseKey = getLicenseFromSystemProperty();
        if (!JSpaceUtilities.isEmpty(licenseKey)) {
            return licenseKey;
        }

        licenseKey = getLicenseFromClasspath();
        
        if(JSpaceUtilities.isEmpty(licenseKey)) {
            licenseKey = getLicenseFromHomeDir();
        }
        if (JSpaceUtilities.isEmpty(licenseKey)) {
            licenseKey = getLicenseFromCurrentDir();
        }

        if (!JSpaceUtilities.isEmpty(licenseKey)) {
            licenseKey = lm.normalizeLicense(licenseKey);
            System.setProperty(LICENSE_KEY_SYS_PROP, licenseKey);
        }

        return licenseKey;

    }

    private String getLicenseFromCurrentDir() {
       

        String licensekey = null;
        // look for it in the home dir
       
        File licenseKeyFileInCurrentDir = new File("."
                + File.separator + CONTAINER_LICENSEKEY_FILE_NAME);
        
        licensekey = getLicenseFromFile(licenseKeyFileInCurrentDir);

        return licensekey;
    }

    private String getLicenseFromFile(File licenseKeyFileInCurrentDir) {
        String licensekey = null;
        if (licenseKeyFileInCurrentDir.exists()) {
            Properties licenseProps;
            try {
                licenseProps = JProperties.convertXML(licenseKeyFileInCurrentDir.getPath(), true);
                licensekey = licenseProps.getProperty(CONTAINER_LICENSEKEY_PROP);
            } catch (SAXException e) {
                logger.log(Level.SEVERE, "Failed to load license file", e);
            } catch (ParserConfigurationException e) {
                logger.log(Level.SEVERE, "Failed to load license file", e);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to load license file", e);
            }

        }
        return licensekey;
    }

    private String getLicenseFromHomeDir() {
        String licensekey = null;
        // look for it in the home dir
        String homeDir = Environment.getHomeDirectory();

        File licenseKeyFile = new File(homeDir + File.separator
                + CONTAINER_LICENSEKEY_FILE_NAME);
        
        licensekey = getLicenseFromFile(licenseKeyFile);

        return licensekey;
    }

    private String getLicenseFromClasspath() {
        String licensekey = null;
        Properties props = Locator.deriveDirectories(Locator.class);
        String gsBootLib = (String) props.get(Locator.GS_BOOT_LIB);
        InputStream licenseInputStream = ResourceLoader.getResourceStream(CONTAINER_LICENSEKEY_FILE_NAME, gsBootLib);
        if (licenseInputStream != null) {
            Properties licenseProps;
            try {
                licenseProps = JProperties.convertXML(licenseInputStream,
                        true,
                        null);
                licensekey = licenseProps.getProperty(CONTAINER_LICENSEKEY_PROP);
            } catch (SAXException e) {
                logger.log(Level.SEVERE, "Failed to load license file", e);
            } catch (ParserConfigurationException e) {
                logger.log(Level.SEVERE, "Failed to load license file", e);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to load license file", e);
            }
        }
        return licensekey;
    }

    private String getLicenseFromSystemProperty() {
        return System.getProperty(LICENSE_KEY_SYS_PROP);
    }

    /** Verifying license for the GigaSpaces product. */
    public void verifyLicense()
    {

            LicenseManagerVerifier lm;
            try {
                lm = new LicenseManagerVerifier();
            } catch (NoSuchProviderException e) {
                throw new SecurityException("GigaSpaces license could not be verified: " + e.getMessage(), e);
            } catch (NoSuchAlgorithmException e) {
                throw new SecurityException("GigaSpaces license could not be verified: " + e.getMessage(), e);
            }
            String licensekey = getLicenseKey(lm);
            // create license manager
            
            // TODO - use standard error messages
            if(licensekey == null) {
                throw new SecurityException("GigaSpaces license could not be found!");
            }
            
            try {
                if(!lm.verifyLicenseKey(licensekey)) {
                    throw new SecurityException("GigaSpaces license is Invalid!");
                }
            } catch (ParseException e) {
                throw new SecurityException("GigaSpaces license could not be verified: " + e.getMessage(), e);
            }
            
            if(!LicenseManagerVerifier.getLicenseWAN(licensekey)) {
                throw new SecurityException("The GigaSpaces WAN Module is not enabled in the License being used. " +
                		"Please contact support for more details: http://www.gigaspaces.com/supportcenter");
            }
        
    } 
}
