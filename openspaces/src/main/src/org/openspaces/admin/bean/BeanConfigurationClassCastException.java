package org.openspaces.admin.bean;

/**
 * Indicates the bean does not implement the required interfaces.
 * @author itaif
 *
 */
public class BeanConfigurationClassCastException extends BeanConfigurationException {

    public BeanConfigurationClassCastException(String message) {
        super(message);
    }

}
