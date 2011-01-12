package ${puGroupId}.processor;

import ${puGroupId}.common.Data;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;


/**
 * The processor simulates work done on unprocessed Data object. The processData
 * accepts a Data object, simulate work by sleeping, and then sets the processed
 * flag to true and reutrn the processed Data.
 */
public class Processor {

    private long workDuration = 100;

    @GigaSpaceContext
    private GigaSpace gigaSpace;

    /**
     * Sets the simulated work duration (in milliseconds). Default to 100.
     */
    public void setWorkDuration(long workDuration) {
        this.workDuration = workDuration;
    }
    
    /**
     * Process the given Data object and returning the processed Data.
     * Invoked by mule.
     */
    public Data processData(Data data) {
        // sleep to simulate some work
        try {
            Thread.sleep(workDuration);
        } catch (InterruptedException e) {
            // do nothing
        }
        data.setProcessed(true);
        data.setData("PROCESSED : " + data.getRawData());
        System.out.println(" ------ PROCESSED : " + data);
        return data;
    }

}