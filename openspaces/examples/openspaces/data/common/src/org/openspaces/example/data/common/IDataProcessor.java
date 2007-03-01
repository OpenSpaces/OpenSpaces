package org.openspaces.example.data.common;

/**
 * @author kimchy
 */
public interface IDataProcessor {

    Data processData(Data data);

    void sayData(Data data);
}
