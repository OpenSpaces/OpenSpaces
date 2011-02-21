package org.openspaces.pu.service;

/**
 * This class should be used by customers in order to implement their ServiceDetails
 *
 * @since 8.0.1
 */
public class CustomServiceDetails extends PlainServiceDetails {
    public static final String SERVICE_TYPE = "custom-details";
    
    // Just for externalizable
    public CustomServiceDetails() {
    }

    /**
     * 
     * @param id should identify that service
     * @param serviceType
     * @param serviceSubType
     * @param description
     * @param longDescription
     */
    public CustomServiceDetails(String id, String serviceType, String serviceSubType,
                               String description, String longDescription) {
        
        super(id, serviceType, serviceSubType, description, longDescription);
    }    
}