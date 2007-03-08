package org.openspaces.example.data.common;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;

import java.io.Serializable;

/**
 * @author kimchy
 */
@SpaceClass
public class Data implements Serializable {

    public static long[] TYPES = {1, 2, 3, 4};

    private String id;

    private Long type;

    private String rawData;

    private String data;

    private Boolean processed;

    public Data() {

    }

    public Data(long type, String rawData) {
        this.type = type;
        this.rawData = rawData;
    }

    @SpaceId(autoGenerate = true)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @SpaceRouting
    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Boolean isProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    public String toString() {
        return "id[" + id + "] type[" + type + "] rawData[" + rawData + "] data[" + data + "] processed[" + processed + "]";
    }
}
