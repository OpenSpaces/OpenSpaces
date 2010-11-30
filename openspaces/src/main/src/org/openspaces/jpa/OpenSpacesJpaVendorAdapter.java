package org.openspaces.jpa;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;

import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.JpaVendorAdapter;

import com.j_spaces.core.IJSpace;

/**
 * A spring OpenSpaces JPA vendor adapter.
 * 
 * @author idan
 * @since 8.0
 *
 */
public class OpenSpacesJpaVendorAdapter implements JpaVendorAdapter {

    private final OpenSpacesPersistenceProvider _persistenceProvider = new OpenSpacesPersistenceProvider();
    
    public PersistenceProvider getPersistenceProvider() {
        return _persistenceProvider;
    }
    
    public String getPersistenceProviderRootPackage() {
        return "org.openspaces.jpa";
    }
    
    public void setSpace(IJSpace space) {
        _persistenceProvider.setSpace(space);
    }

    public Class<? extends EntityManagerFactory> getEntityManagerFactoryInterface() {
        return javax.persistence.EntityManagerFactory.class;
    }

    public Class<? extends EntityManager> getEntityManagerInterface() {
        return javax.persistence.EntityManager.class;
    }

    public JpaDialect getJpaDialect() {
        return null;
    }

    public Map<String, ?> getJpaPropertyMap() {
        return null;
    }

    public void postProcessEntityManagerFactory(EntityManagerFactory entitymanagerfactory) {
    }

}
