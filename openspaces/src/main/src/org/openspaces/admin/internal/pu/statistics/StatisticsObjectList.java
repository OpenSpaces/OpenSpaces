/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.openspaces.admin.internal.pu.statistics;

import java.util.ArrayList;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * A List of Objects that supports min,max,percentile operations if the Objects implement Comparable
 * and supports average operation if the Objects implement Number
 * @author itaif
 * @since 9.0.0
 */
public class StatisticsObjectList {

    private final List<Object> values = new ArrayList<Object>();
    private Double sum = 0.0;
    private Comparable<Object> min = null;
    private Comparable<Object> max = null;
    private Class<?> notComparableClass = null;
    private Class<?> notNumberClass = null;
    private boolean sorted = false;
    
    /**
     * @param value
     */
    public void add(Object value) {
        values.add(value);
        sorted = false;
        if (notNumberClass == null) {
            if (value instanceof Number) {
                sum += ((Number)value).doubleValue();
            }
            else {
                notNumberClass = value.getClass();
            }
        }
        
        if (notComparableClass == null) {
            if (value instanceof Comparable<?>) {
                if (min == null || min.compareTo(value) > 0) {
                    min = (Comparable)value;
                }
                
                if (max == null || max.compareTo(value) < 0) {
                    max = (Comparable)value;
                }
            }
            else {
                notComparableClass = value.getClass();
            }
        }
    }
    
    public Double getAverage() {
        if (notNumberClass != null) {
            throw new ClassCastException(notNumberClass + " cannot be cast to a Number");
        }
        return sum / values.size();
    }
    
    public Object getMinimum() {
        if (notComparableClass != null) {
            throw new ClassCastException(notComparableClass + " cannot be cast to a Comparable");
        }
        return min;
    }
    
    public Object getMaximum() {
        if (notComparableClass != null) {
            throw new ClassCastException(notComparableClass + " cannot be cast to a Comparable");
        }
        return max;
    }
    
    public Object getPercentile(double percentile) {
        if (percentile < 0) {
            throw new IllegalArgumentException("percentile ("+percentile+") must be between 0 and 100.");
        }
        if (percentile > 100) {
            throw new IllegalArgumentException("percentile ("+percentile+") must be between 0 and 100");
        }
        if (notComparableClass != null) {
            throw new ClassCastException(notComparableClass + " cannot be cast to a Comparable");
        }
        if (!sorted) {
            Collections.sort(values);
            sorted = true;
        }
        int index = (int) (Math.ceil((values.size()-1)*percentile/100));
        return values.get(index);
    }
}
