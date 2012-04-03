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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A List of Objects that supports min,max,percentile operations if the Objects implement Comparable
 * and supports average operation if the Objects implement Number
 * @author itaif
 * @since 9.0.0
 */
public class StatisticsObjectList {

    private static final Log logger = LogFactory.getLog(StatisticsObjectList.class);
    
    @SuppressWarnings("rawtypes")
    private final List values = new ArrayList<Object>();
    private Double sum = 0.0;
    @SuppressWarnings("rawtypes")
    private Comparable min = null;
    private Long firstTimeStampMillis = null;
    private Long lastTimeStampMillis = null;
    @SuppressWarnings("rawtypes")
    private Comparable max = null;
    private Class<?> notComparableClass = null;
    private Class<?> notNumberClass = null;
    private boolean sorted = false;
    private Object last = null;
    private Object first = null;
    
    /**
     * Adds the specified object to the list
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void add(Object value, long timeStampMillis) {
        values.add(value);
        if(first == null)
            first = value;
        if(firstTimeStampMillis == null)
            firstTimeStampMillis = timeStampMillis;
        last = value;
        lastTimeStampMillis = timeStampMillis;
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
    
    /**
     * @return the average values (cast to double) of the object in the list or null if the list is empty
     * @throws ClassCastException if any object in the list is not a Number 
     */
    public Double getAverage() {
        if (notNumberClass != null) {
            throw new ClassCastException(notNumberClass + " cannot be cast to a Number");
        }
        if (values.isEmpty()) {
            return null;
        }
        
        double average = sum / values.size();
        
        if (logger.isDebugEnabled()) {
            logger.debug("average("+toString()+")="+average);
        }
        return average;
    }
    
    /**
     * @return the minimum (natural order) value object in the list or null if the list is empty. 
     * @throws ClassCastException if any object in the list is not a Comparable
     */
    public Object getMinimum() {
        if (notComparableClass != null) {
            throw new ClassCastException(notComparableClass + " cannot be cast to a Comparable");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("minimum("+toString()+")="+min);
        }
        return min;
    }
    
    /**
     * @return the maximum (natural order) value object in the list or null if the list is empty. 
     * @throws ClassCastException if any object in the list is not a Comparable
     */
    public Object getMaximum() {
        if (notComparableClass != null) {
            throw new ClassCastException(notComparableClass + " cannot be cast to a Comparable");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("maximum("+toString()+")="+max);
        }
        return max;
    }
    
    /**
     * @return the last added sample or null if the list is empty.
     */
    public Object getLast() {
        if (logger.isDebugEnabled()) {
            logger.debug("last("+toString()+")="+last);
        }
        return last;
    }
    
    /**
     * @return the specified percentile (natural order) value object in the list or null if the list is empty. 
     * @throws ClassCastException if any object in the list is not a Comparable
     * @throws IllegalArgumentException if percentile is bigger than 100 or less than 0
     */
    @SuppressWarnings("unchecked")
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
        if (values.isEmpty()) {
            return null;
        }
        if (!sorted) {
            Collections.sort(values);
            sorted = true;
        }
        int index = (int) (Math.round((values.size()-1)*percentile/100));
        Object percentileValue = values.get(index);
        if (logger.isDebugEnabled()) {
            logger.debug("percentile("+percentile+","+toString()+")="+percentileValue);
        }
        return percentileValue;
    }

    /**
     * @param timeWindowSeconds 
     * @return the (last-first)/deltaTimeInNanos (cast to double) of the object in the list or null if the are less then 2 values
     * @throws ClassCastException if any object in the list is not a Number 
     */
    public Double getDeltaValuePerNanoSecond() {
        return getDeltaPerTimeunit(TimeUnit.NANOSECONDS);
    }
    
    /**
     * @param timeWindowSeconds 
     * @return the (last-first)/deltaTimeInMillis (cast to double) of the object in the list or null if the are less then 2 values
     * @throws ClassCastException if any object in the list is not a Number 
     */
    public Double getDeltaValuePerMilliSecond() {
        return getDeltaPerTimeunit(TimeUnit.MILLISECONDS);
    }
    
    /**
     * @param timeWindowSeconds 
     * @return the (last-first)/deltaTimeInSeconds (cast to double) of the object in the list or null if the are less then 2 values
     * @throws ClassCastException if any object in the list is not a Number 
     */
    public Double getDeltaValuePerSecond() {
        return getDeltaPerTimeunit(TimeUnit.SECONDS);
    }

    private Double getDeltaPerTimeunit(TimeUnit timeUnit) {
        if (notNumberClass != null) {
            throw new ClassCastException(notNumberClass + " cannot be cast to a Number");
        }
        if (values.size() < 2) {
            return null;
        }

        Long timeWindowInTimeunit = timeUnit.convert(lastTimeStampMillis - firstTimeStampMillis, TimeUnit.MILLISECONDS);
        double lastValue = ((Number)last).doubleValue();
        double firstValue = ((Number)first).doubleValue();
        
        double deltaValuePerTimeunit = (lastValue-firstValue)/timeWindowInTimeunit;
        
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "deltaValuePer" + timeUnit.toString() +"("+toString()+")="+
                    "("+lastValue+"-"+firstValue+")/"+timeWindowInTimeunit+"="+deltaValuePerTimeunit);
        }
        return deltaValuePerTimeunit;
    }
    
    
    @Override
    public String toString() {
        return values.toString();
    }
}
