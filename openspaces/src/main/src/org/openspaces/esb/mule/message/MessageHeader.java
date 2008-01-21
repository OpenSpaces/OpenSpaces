package org.openspaces.esb.mule.message;

import java.util.Map;

/**
 * Base interface that expose mule meta data.
 *
 * <p>
 * <B>Note:</B> implementation of this interface must have consistent results with equivalent get/set Property method.
 *
 * @author yitzhaki
 */
public interface MessageHeader {

    /**
     * @param key   the key to be placed into this property list.
     * @param value the value corresponding to <tt>key</tt>.
     * @see #getProperty
     */
    void setProperty(String key, Object value);

    /**
     * @param key the property key.
     * @return the value in this property list with the specified key value.
     * @see #setProperty
     */
    Object getProperty(String key);


    /**
     * @return {@link java.util.Map} that contains all the properties.
     */
    Map<String, Object> getProperties();

    /**
     * Sets all the properties from the properties param.
     *
     * @param properties - properties to set.
     */
    void setProperties(Map<String, Object> properties);
}
