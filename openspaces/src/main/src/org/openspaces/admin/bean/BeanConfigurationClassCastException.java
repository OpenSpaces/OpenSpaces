package org.openspaces.admin.bean;

/**
 * Indicates the bean does not implement the required interfaces.
 * @author itaif
 */
public class BeanConfigurationClassCastException extends BeanConfigurationException {

    private static final long serialVersionUID = 1L;

    public BeanConfigurationClassCastException(String message) {
        super(message);
    }

}
