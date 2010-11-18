package org.openspaces.jpa.openjpa;

import net.jini.core.transaction.server.TransactionManager;

import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.openspaces.core.space.UrlSpaceConfigurer;

import com.gigaspaces.client.transaction.ITransactionManagerProvider;
import com.gigaspaces.client.transaction.ITransactionManagerProvider.TransactionManagerType;
import com.gigaspaces.client.transaction.TransactionManagerConfiguration;
import com.gigaspaces.client.transaction.TransactionManagerProviderFactory;
import com.j_spaces.core.IJSpace;

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
    
    public SpaceConfiguration() {
        super();        
        // Default transaction timeout
        setLockTimeout(0);
        setOptimistic(false);
        setLockManager("none");
        setDynamicEnhancementAgent(false);
    }

    public void initialize() {
        // Set a space proxy using the provided connection url        
        _space = new UrlSpaceConfigurer(getConnectionURL()).space();
        
        // Create a transaction manager
        TransactionManagerConfiguration configuration = new TransactionManagerConfiguration(TransactionManagerType.DISTRIBUTED);
        try {
            _transactionManagerProvider = TransactionManagerProviderFactory.newInstance(_space, configuration);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    public IJSpace getSpace() {
        return _space;
    }
    
    public TransactionManager getTransactionManager() {
        return _transactionManagerProvider.getTransactionManager();
    }
    
    
    
}
