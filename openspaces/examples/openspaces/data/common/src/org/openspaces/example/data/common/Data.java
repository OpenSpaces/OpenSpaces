package org.openspaces.example.data.common;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;

import java.io.Serializable;

/**
 * A simple object used to work with the Space. Important properties include the id
 * of the object, a type (used to perform routing when working with partitioned space),
 * the raw data and processed data, and a boolean flag indicating if this Data object
 * was processed or not.
 *
 * <p>Note, this object implements Serializable because it is used as a parameter when
 * using OpenSpaces remoting support.
 *
 * @author kimchy
 */
@SpaceClass
public class Data implements Serializable {

    /**
     * Static values representing the differnet values the type propery
     * can have.
     */
    public static long[] TYPES = {1, 2, 3, 4};

    private String id;

    private Long type;

    private String rawData;

    private String data;

    private Boolean processed;

    /**
     * Constructs a new Data object.
     */
    public Data() {

    }

    /**
     * Constructs a new Data object with the given type
     * and raw data.
     */
    public Data(long type, String rawData) {
        this.type = type;
        this.rawData = rawData;
    }

    /**
     * The id of this object. Its value will be auto generated when it is written
     * to the space.
     */
    @SpaceId(autoGenerate = true)
    public String getId() {
        return id;
    }

    /**
     * The id of this object. Its value will be auto generated when it is written
     * to the space.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * The type of the data object. Used as the routing field when working with
     * a partitioned space.
     */
    @SpaceRouting
    public Long getType() {
        return type;
    }

    /**
     * The type of the data object. Used as the routing field when working with
     * a partitioned space.
     */
    public void setType(Long type) {
        this.type = type;
    }

    /**
     * The raw data this object holds.
     */
    public String getRawData() {
        return rawData;
    }

    /**
     * The raw data this object holds.
     */
    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    /**
     * The processed data this object holds.
     */
    public String getData() {
        return data;
    }

    /**
     * The processed data this object holds.
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * A boolean flag indicating if the data object was processed or not.
     */
    public Boolean isProcessed() {
        return processed;
    }

    /**
     * A boolean flag indicating if the data object was processed or not.
     */
    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    public String toString() {
        return "id[" + id + "] type[" + type + "] rawData[" + rawData + "] data[" + data + "] processed[" + processed + "]";
    }
}
