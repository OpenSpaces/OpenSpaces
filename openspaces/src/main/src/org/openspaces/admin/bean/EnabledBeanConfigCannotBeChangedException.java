package org.openspaces.admin.bean;


/**
 * Exception indicating that a configuration for an already enabled bean can't be changed unless first disabled.
 * 
 * @see BeanConfigManager
 * 
 * @author Moran Avigdor
 * @author Itai Frenkel
 * @since 8.0
 */
public class EnabledBeanConfigCannotBeChangedException extends BeanConfigException {

    private static final long serialVersionUID = 1L;

    public EnabledBeanConfigCannotBeChangedException(String message) {
        super(message);
    }

}
