package org.openspaces.jpa;

import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;

import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.PersistenceProviderImpl;

import com.j_spaces.core.IJSpace;

/**
 * An OpenSpaces implementation for the PersistenceProvider interface.
 * Provides an OpenJPA entity manager factory with an optional injected space instance.
 * 
 * @author idan
 * @since 8.0
 * 
 */
public class OpenSpacesPersistenceProvider implements PersistenceProvider {

    private final PersistenceProvider _persistenceProvider;
    private IJSpace _space;
    
    
    public OpenSpacesPersistenceProvider() {
        _persistenceProvider = new PersistenceProviderImpl();
    }
    
    /**
     * Creates an {@link EntityManagerFactory} instance and injects the provided space instance to it. 
     * @param persistenceUnitName The persistence unit name.
     * @param space The space instance to inject.
     * @param properties Properties map.
     * @return {@link EntityManagerFactory} instance.
     */
    @SuppressWarnings("rawtypes")
    public EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, IJSpace space, Map properties) {
        setSpace(space);
        return createEntityManagerFactory(persistenceUnitName, properties);
    }
    
    /**
     * Creates an {@link EntityManagerFactory} instance and injects the provided space instance to it.
     * @param persistenceUnitName The persistence unit name.
     * @param space The space instance to inject.
     * @return {@link EntityManagerFactory} instance.
     */
    public EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, IJSpace space) {
        return createEntityManagerFactory(persistenceUnitName, space);
    }
    
    @SuppressWarnings("rawtypes")
    public EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, Map properties) {
        OpenJPAEntityManagerFactorySPI factory =
            (OpenJPAEntityManagerFactorySPI) _persistenceProvider.createEntityManagerFactory(
                    persistenceUnitName, properties);
        factory.getConfiguration().setConnectionFactory(_space);
        return factory;
    }

    @SuppressWarnings("rawtypes")
    public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo pui, Map properties) {
        OpenJPAEntityManagerFactorySPI factory =
            (OpenJPAEntityManagerFactorySPI) _persistenceProvider.createContainerEntityManagerFactory(
                    pui, properties);
        factory.getConfiguration().setConnectionFactory(_space);
        return factory;
    }

    /**
     * Sets the space instance which will be injected to the {@link EntityManagerFactory} instance.
     * @param space The space instance to inject.
     */
    public void setSpace(IJSpace space) {
        _space = space;        
    }

    public ProviderUtil getProviderUtil() {
        return _persistenceProvider.getProviderUtil();
    }
    
}
