package org.openspaces.example.data.common;

/**
 * An interface representing a data processor.
 *
 * @author kimchy
 */
public interface IDataProcessor {

    /**
     * Process a given Data object, returning the processed Data object.
     */
    Data processData(Data data);

    /**
     * Simply says (prints out) the Data object passed as a parameter.
     */
    void sayData(Data data);
}
