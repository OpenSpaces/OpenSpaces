package ${puGroupId}.processor;

import ${puGroupId}.common.Data;


/**
 *  This service gets an uprocessed Data object and approves it by
 *  concatenating an approvement message to the payload. 
 */
public class Approver {

    public Data approve(Data data) throws Exception {
        System.out.println("----- Approver got Data: " + data);
        data.setRawData(data.getRawData() + "-Approved");
        return data;
    }
    
}