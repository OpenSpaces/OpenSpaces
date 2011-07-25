package org.openspaces.dsl;

/**
 * Configuration of network elements of a specific service
 * @author itaif
 *
 */
public class ServiceNetwork {

    int defaultPort;
    
    public ServiceNetwork() {
        
    }
    
    public void setDefaultPort(int defaultPort) {
        this.defaultPort = defaultPort;
    }
    
    public int getDefaultPort() {
        return this.defaultPort;
    }
}
