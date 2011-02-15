package org.openspaces.jpa;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.kernel.AbstractBrokerFactory;
import org.apache.openjpa.lib.conf.ConfigurationProvider;
import org.openspaces.jpa.openjpa.SpaceConfiguration;

/**
 * GigaSpaces OpenJPA BrokerFactory implementation.
 * 
 * @author Idan Moyal
 * @since 8.0.1
 *
 */
public class BrokerFactory extends AbstractBrokerFactory {
    //
    private static final long serialVersionUID = 1L;

    protected BrokerFactory(OpenJPAConfiguration config) {
        super(config);
    }
    
    @Override
    protected StoreManager newStoreManager() {
        return new org.openspaces.jpa.StoreManager();
    }

    /**
     * Factory method for constructing a {@link org.apache.openjpa.kernel.BrokerFactory}.
     */
    public static BrokerFactory newInstance(ConfigurationProvider cp) {
        StoreManager store = new org.openspaces.jpa.StoreManager();
        SpaceConfiguration conf = new SpaceConfiguration();
        cp.setInto(conf);
        conf.supportedOptions().removeAll(store.getUnsupportedOptions());

        return new BrokerFactory(conf);
    }


    
    
}
