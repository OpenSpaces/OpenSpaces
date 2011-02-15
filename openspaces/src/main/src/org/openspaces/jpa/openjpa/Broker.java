package org.openspaces.jpa.openjpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.openjpa.kernel.BrokerImpl;
import org.apache.openjpa.kernel.OpCallbacks;
import org.apache.openjpa.kernel.StateManager;
import org.apache.openjpa.kernel.StateManagerImpl;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.util.OpenJPAException;
import org.apache.openjpa.util.UserException;

/**
 * GigaSpaces OpenJPA's Broker implementation.
 * 
 * @author Idan Moyal
 * @since 8.0.1
 *
 */
public class Broker extends BrokerImpl {
    //
    private static final long serialVersionUID = 1L;

    public Broker() {
        super();
    }
   
    /**
     * Create a state manager for the given oid and metadata.
     */
    @Override
    protected StateManagerImpl newStateManagerImpl(Object oid, ClassMetaData meta) {
        return new StateManager(oid, meta, this);
    }
    
    /**
     * Persist the provided Collection elements.
     */
    public void persistCollection(Collection<?> collection, boolean explicit, OpCallbacks call, StateManager ownerStateManager) {
        
        if (collection.isEmpty())
            return;

        beginOperation(true);
        List<Exception> exceps = null;
        try {
            assertWriteOperation();

            for (Object object : collection) {
                try {
                    StateManager stateManager = (StateManager) persist(object, null, explicit, call);
                    stateManager.setOwnerStateManager(ownerStateManager);
                } catch (UserException e) {
                    if (exceps == null)
                        exceps = new ArrayList<Exception>();
                    exceps.add(e);
                }
            }
        } finally {
            endOperation();
        }
        
        // Throw exception if needed
        if (exceps != null && !exceps.isEmpty()) {
            boolean fatal = false;
            Throwable[] throwables = exceps.toArray(new Throwable[exceps.size()]);
            for (int i = 0; i < throwables.length; i++) {
                if (throwables[i] instanceof OpenJPAException
                    && ((OpenJPAException) throwables[i]).isFatal())
                    fatal = true;
            }
            
            Localizer loc = Localizer.forPackage(BrokerImpl.class);
            OpenJPAException err = new UserException(loc.get("nested-exceps"));
            throw err.setNestedThrowables(throwables).setFatal(fatal);            
        }
        
    }
    
    
}
