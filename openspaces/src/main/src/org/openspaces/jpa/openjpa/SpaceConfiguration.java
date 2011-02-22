package org.openspaces.jpa.openjpa;

import net.jini.core.transaction.server.TransactionManager;

import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.kernel.BrokerImpl;
import org.apache.openjpa.lib.conf.Value;
import org.openspaces.core.space.UrlSpaceConfigurer;

import com.gigaspaces.client.transaction.ITransactionManagerProvider;
import com.gigaspaces.client.transaction.ITransactionManagerProvider.TransactionManagerType;
import com.gigaspaces.client.transaction.TransactionManagerConfiguration;
import com.gigaspaces.client.transaction.TransactionManagerProviderFactory;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.ReadModifiers;

/**
 * Holds OpenJPA's configuration properties & GigaSpaces resources.
 * OpenJPA keeps a single instance of this class.
 * 
 * @author idan
 * @since 8.0
 * 
 */
public class SpaceConfiguration extends OpenJPAConfigurationImpl {

    private IJSpace _space;
    private ITransactionManagerProvider _transactionManagerProvider;
    private int _readModifier;
    
    public SpaceConfiguration() {
        super();       
        supportedOptions().add(OPTION_OPTIMISTIC);
        // Default transaction timeout
        setLockTimeout(0);
        setLockManager("none");
        setDynamicEnhancementAgent(false);
        _readModifier = ReadModifiers.REPEATABLE_READ;
    }

    public void initialize() {
        // Set a space proxy using the provided connection url
    	// if the space was injected - do nothing.
        if (_space == null) {
            Value configurationValue = getValue("ConnectionFactory");
            if (configurationValue != null && configurationValue.get() != null)
                _space = (IJSpace) configurationValue.get();
            else            
                _space = new UrlSpaceConfigurer(getConnectionURL()).space();
            
            //if configured to use optimistic locking - set it on the space proxy
            if(getOptimistic())
                _space.setOptimisticLocking(true);
        }
        
        // Create a transaction manager
        TransactionManagerConfiguration configuration = new TransactionManagerConfiguration(TransactionManagerType.DISTRIBUTED);
        try {
            _transactionManagerProvider = TransactionManagerProviderFactory.newInstance(_space, configuration);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        
        // Set read lock level (modifier)
        _readModifier = getReadLockLevel().equals("write") ? ReadModifiers.EXCLUSIVE_READ_LOCK
                : ReadModifiers.REPEATABLE_READ;        
    }
        
    public IJSpace getSpace() {
        return _space;
    }
    
    public TransactionManager getTransactionManager() {
        return _transactionManagerProvider.getTransactionManager();
    }
    
    public int getReadModifier() {
        return _readModifier;
    }
    
    @Override
    public void setConnectionFactory(Object space) {
        _space = (IJSpace) space;
    }

    /**
     * Create a new broker instance.
     */
    @Override
    public BrokerImpl newBrokerInstance(String user, String pass) {
        return new Broker();
    }
    
    
}
