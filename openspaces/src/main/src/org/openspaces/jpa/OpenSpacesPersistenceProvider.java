package org.openspaces.jpa;

import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;

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
    
    @SuppressWarnings("rawtypes")
    public EntityManagerFactory createEntityManagerFactory(String name, Map m) {
        OpenJPAEntityManagerFactorySPI factory =
            (OpenJPAEntityManagerFactorySPI) _persistenceProvider.createEntityManagerFactory(name, m);
        factory.getConfiguration().setConnectionFactory(_space);
        return factory;
    }

    @SuppressWarnings("rawtypes")
    public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo pui, Map m) {
        OpenJPAEntityManagerFactorySPI factory =
            (OpenJPAEntityManagerFactorySPI) _persistenceProvider.createContainerEntityManagerFactory(pui, m);
        factory.getConfiguration().setConnectionFactory(_space);
        return factory;
    }

    public void setSpace(IJSpace space) {
        _space = space;        
    }
    
}
