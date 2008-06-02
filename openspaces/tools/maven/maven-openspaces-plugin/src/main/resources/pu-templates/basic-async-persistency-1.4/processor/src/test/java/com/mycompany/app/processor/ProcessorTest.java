package ${puGroupId}.processor;

import ${puGroupId}.common.Data;

import junit.framework.TestCase;


/**
 * A simple unit test that verifies the Processor processData method actually processes
 * the Data object.
 */
public class ProcessorTest extends TestCase {

    public void testVerifyProcessedFlag() {
        Processor processor = new Processor();
        Data data = new Data(new Long(1l), "test");

        Data result = processor.processData(data);
        assertEquals("verify that the data object was processed", Boolean.TRUE, result.isProcessed());
        assertEquals("verify the data was processed", "PROCESSED : " + data.getRawData(), result.getData());
        assertEquals("verify the type was not changed", data.getType(), result.getType());
    }
}
