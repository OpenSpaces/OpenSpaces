package ${puGroupId}.processor;

import ${puGroupId}.common.Data;


/**
 *  This service gets an approved (and unprocessed) Data object and verifies it by
 *  concatenating a verification message to the payload. 
 */
public class Verifier {

    public Data verify(Data data) throws Exception {
        System.out.println("----- Verifier got Data: " + data);
        data.setRawData(data.getRawData() + "-Verified");
        return data;
    }
    
}