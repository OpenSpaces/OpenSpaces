/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.example.data.common;

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
public class Data implements Serializable {

    private Long id;

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
        this.type = new Long(type);
        this.rawData = rawData;
    }

    /**
     * The id of this object.
     */
    public Long getId() {
        return id;
    }

    /**
     * The id of this object. Its value will be auto generated when it is written
     * to the space.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * The type of the data object. Used as the routing field when working with
     * a partitioned space.
     */
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
