package org.openspaces.pu.service;



/**
 * This class should be used by customers in order to implement their ServiceDetails
 *
 * @since 8.0.1
 */
public class CustomServiceDetails extends PlainServiceDetails {
    public static final String SERVICE_TYPE = "custom-details";
    private static final long serialVersionUID = -8038713604075604209L;
    
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
     * @deprecated since 8.0.5. Use {@link #CustomServiceDetails(String, String, String, String)} 
     * instead - constructor that does not receive service type as parameter since custom 
     * type is always {@link SERVICE_TYPE}
     */
    public CustomServiceDetails(String id, String serviceType, String serviceSubType,
                               String description, String longDescription) {
        
        super(id, serviceType, serviceSubType, description, longDescription);
    }     
    
    /**
     * Constructor 
     * @param id should identify that service, should be same as {@link #CustomServiceMonitors}'s id
     * @param serviceSubType 
     * @param description should be same as {@link #CustomServiceMonitors}'s description 
     * @param longDescription
     */
    public CustomServiceDetails( String id, String serviceSubType, String description, 
                                String longDescription ) {
        
        super( id, SERVICE_TYPE, serviceSubType, description, longDescription );
    }    
}