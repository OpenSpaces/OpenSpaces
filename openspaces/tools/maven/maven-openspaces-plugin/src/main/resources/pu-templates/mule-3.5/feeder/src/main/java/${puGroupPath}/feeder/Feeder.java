package ${puGroupId}.feeder;

import ${puGroupId}.common.Data;


/**
 * A feeder that uses a configured mule outboud channel to write Data
 * objects to the space (in an unprocessed state).
 *
 * <p>The scheduling uses the mule quartz service.
 */
public class Feeder  {

    private long numberOfTypes = 10;

    private static  long counter = 1;
    
    /**
     * Sets the number of types.
     *
     * <p>The type is used as the routing index for partitioned space. This will affect the distribution of Data
     * objects over a partitioned space.
     */
    public void setNumberOfTypes(long num) {
        numberOfTypes = num;
    }
    
    /**
     * Triggered by mule quartz squeduling service to write new Data object to the space.
     */
    public Data feed() {
        long time = System.currentTimeMillis();
        Data data = new Data((counter++ % numberOfTypes), "FEEDER " + Long.toString(time));
        System.out.println("--- FEEDER WROTE " + data);
        return data;
    }

}
