package ${puGroupId}.processor;

import ${puGroupId}.common.Data;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import org.openspaces.core.GigaSpace;


/**
 * Integration test for the Processor. Uses similar xml definition file (ProcessorIntegrationTest-context.xml)
 * to the actual pu.xml. Writs an unprocessed Data to the Space, and verifies that it has been processed by
 * taking a processed one from the space.
 */
public class ProcessorIntegrationTest extends AbstractDependencyInjectionSpringContextTests {

    protected GigaSpace gigaSpace;

    public ProcessorIntegrationTest() {
        setPopulateProtectedVariables(true);
    }

    protected void onSetUp() throws Exception {
        gigaSpace.clear(null);
    }
    
    protected void onTearDown() throws Exception {
        gigaSpace.clear(null);
    }
    
    protected String[] getConfigLocations() {
        return new String[]{"${puGroupPath}/processor/ProcessorIntegrationTest-context.xml"};
    }
    
    public void testVerifyProcessing() throws Exception {
        // write the data to be processed to the Space
        Data data = new Data(new Long(1l), "test");
        gigaSpace.write(data);

        // create a template of the processed data (processed)
        Data template = new Data();
        template.setType(new Long(1l));
        template.setProcessed(Boolean.TRUE);

        // wait for the result
        Data result = (Data)gigaSpace.take(template, 500);
        // verify it
        assertNotNull("No data object was processed", result);
        assertEquals("Processed Flag is false, data was not processed", Boolean.TRUE, result.isProcessed());
        assertEquals("Processed text mismatch", "PROCESSED : " + data.getRawData(), result.getData());
    }
}
