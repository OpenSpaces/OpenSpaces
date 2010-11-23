package org.openspaces.jpa.openjpa;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.TransactionFactory;

import org.apache.openjpa.abstractstore.AbstractStoreManager;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.PCState;
import org.apache.openjpa.kernel.QueryLanguages;
import org.apache.openjpa.kernel.StoreQuery;
import org.apache.openjpa.kernel.exps.ExpressionParser;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.util.ApplicationIds;

import com.gigaspaces.internal.client.QueryResultTypeInternal;
import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
import com.gigaspaces.internal.client.spaceproxy.metadata.ObjectType;
import com.gigaspaces.internal.metadata.ITypeDesc;
import com.gigaspaces.internal.transport.IEntryPacket;
import com.gigaspaces.internal.transport.ITemplatePacket;
import com.gigaspaces.internal.transport.TemplatePacketFactory;
import com.gigaspaces.internal.transport.TransportPacketType;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.ReadModifiers;
import com.j_spaces.core.client.UpdateModifiers;

/**
 * A GigaSpaces back-end implementation for OpenJPA.
 * Responsible for storing and fetching data from GigaSpaces using space API.
 * 
 * @author idan
 * @since 8.0
 *
 */
public class GSStoreManager extends AbstractStoreManager {
    //
    private Transaction _transaction = null;
    private static final HashMap<Class<?>, Integer> _classesRelationStatus = new HashMap<Class<?>, Integer>();
    private static final HashSet<Class<?>> _processedClasses = new HashSet<Class<?>>();
    private static boolean _initializedClassRelationStatus = false;
    
    @Override
    protected void open() {
        // Specific gigaspaces initialization (space proxy)
        getConfiguration().initialize();
    }
    
    
    @Override
    protected Collection<String> getUnsupportedOptions() {
        @SuppressWarnings("unchecked")
        Collection<String> unsupportedOptions = (Collection<String>) super.getUnsupportedOptions();
        unsupportedOptions.remove(OpenJPAConfiguration.OPTION_ID_DATASTORE);
        return unsupportedOptions;
    }

    @Override
    public boolean syncVersion(OpenJPAStateManager sm, Object edata) {
        return true;
    }

    @Override
    public void begin() {
        try {
            if (_transaction != null)
                throw new TransactionException("Attempted to start a new transaction when there's already an active transaction.");
            long timeout = (getConfiguration().getLockTimeout() == 0)?
                    Lease.FOREVER : getConfiguration().getLockTimeout();
            _transaction = (TransactionFactory.create(getConfiguration().getTransactionManager(),
                    timeout)).transaction;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }            
    }

    @Override
    public void commit() {
        try {
            _transaction.commit(Long.MAX_VALUE);
        } catch (Exception e) {
           throw new RuntimeException(e.getMessage(), e);
        } finally {
            _transaction = null;
        }
    }

    @Override
    public void rollback() {
        try {
            _transaction.abort();
        } catch (Exception e) {
           throw new RuntimeException(e.getMessage(), e);
        } finally {
            _transaction = null;
        }
    }    
    
    @Override
    public StoreQuery newQuery(String language) {        
        ExpressionParser ep = QueryLanguages.parserForLanguage(language);
        return new SpaceStoreManagerQuery(ep, this);
    }

    @Override
    protected OpenJPAConfiguration newConfiguration() {
        return new SpaceConfiguration();
    }

    public SpaceConfiguration getConfiguration() {
        return (SpaceConfiguration) getContext().getConfiguration();
    }
    
    /**
     * Returns whether the state manager's managed object exists in space.
     */
    public boolean exists(OpenJPAStateManager sm, Object edata) {
        ClassMetaData cm = sm.getMetaData();
        final Object[] ids = ApplicationIds.toPKValues(sm.getObjectId(), cm);        
        ISpaceProxy proxy = (ISpaceProxy) getConfiguration().getSpace();
        try {
            Object result = proxy.readById(cm.getDescribedType().getName(), ids[0], null, _transaction,
                    0, ReadModifiers.DIRTY_READ, false, QueryResultTypeInternal.EXTERNAL_ENTRY);
            return result != null;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public boolean isCached(List<Object> oids, BitSet edata) {
        return false;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Collection loadAll(Collection sms, PCState state, int load, FetchConfiguration fetch, Object edata) {
            return super.loadAll(sms, state, load, fetch, edata);
        }

    @SuppressWarnings("deprecation")
    @Override
    public boolean initialize(OpenJPAStateManager sm, PCState state,
            FetchConfiguration fetchConfiguration, Object edata) {

        final ClassMetaData cm = sm.getMetaData();                                        
        try {
            IEntryPacket result = null;
            // If we already have the result and only need to initialize.. (relevant for JPQL)
            if (edata != null) {
                result = (IEntryPacket) edata;
            } else {
                final ISpaceProxy proxy = (ISpaceProxy) getConfiguration().getSpace();
                final ITypeDesc typeDescriptor = proxy.getDirectProxy().getTypeManager().getTypeDesc(cm.getDescribedType().getName());                                
                final Object[] ids = ApplicationIds.toPKValues(sm.getObjectId(), cm);
                ITemplatePacket template = TemplatePacketFactory.createIdPacket(ids[0], null, 0, typeDescriptor, TransportPacketType.ENTRY_PACKET);
                result = (IEntryPacket) proxy.read(template, _transaction, 0);                
                if (result == null)
                    return false;            
            }
            // TODO: Handle sub-classes etc...
            sm.initialize(cm.getDescribedType(), state);                        
            
            FieldMetaData[] fms = cm.getFields();
            for (int i = 0; i < fms.length; i++) {
                // Skip primary keys and non-persistent keys
                if (fms[i].isPrimaryKey() || sm.getLoaded().get(fms[i].getIndex()))
                    continue;                
                sm.store(i, result.getFieldValue(i));
            }            
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        
        return true;        
    }

    /**
     * This method loads specific fields from the data store for updating them.
     */
    @Override
    public boolean load(OpenJPAStateManager sm, BitSet fields, FetchConfiguration fetch, int lockLevel, Object context) {
        ClassMetaData cm = (ClassMetaData)sm.getMetaData();
        Object[] ids = ApplicationIds.toPKValues(sm.getObjectId(), cm);        
        final IJSpace space = getConfiguration().getSpace();
        final ITypeDesc typeDescriptor = ((ISpaceProxy) space).getDirectProxy().getTypeManager().getTypeDesc(cm.getDescribedType().getName());
        final ITemplatePacket template = TemplatePacketFactory.createIdPacket(ids[0], null, 0, typeDescriptor, TransportPacketType.ENTRY_PACKET);                      
        try {        
            // Read object from space                
            IEntryPacket result = (IEntryPacket) space.read(template, _transaction, 0); 
            if (result == null)
                return false;
            // Process result - store only the relevant fields in the state manager
            for (int i = 0; i < cm.getDeclaredFields().length; i++) {
                if (fields.get(i))                
                    sm.store(i, result.getFieldValue(i));                
            }                                    
            return true;            
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }                    
    }

    @Override
    public ResultObjectProvider executeExtent(ClassMetaData classmetadata, boolean flag,
            FetchConfiguration fetchconfiguration) {
        return null;
    }
    
    /**
     * Flushes changes to GigaSpaces.
     * Returns a list of exceptions that occurred.
     */
    @SuppressWarnings("rawtypes")
    @Override
    protected Collection flush(Collection pNew, Collection pNewUpdated, Collection pNewFlushedDeleted,
            Collection pDirty, Collection pDeleted) {
        
        IJSpace space = getConfiguration().getSpace();
                
        ArrayList<Exception> exceptions = new ArrayList<Exception>();

        if (!_initializedClassRelationStatus)
            initializeClassRelationStatus();
        
        handleNewObjects(pNew, exceptions, space);
        handleUpdatedObjects(pDirty, exceptions, space);
        handleDeletedObjects(pDeleted, exceptions, space);                     
        
        return exceptions;                   
    }

    /**
     * Clears the removed objects from the space.
     */
    private void handleDeletedObjects(Collection<OpenJPAStateManager> sms, ArrayList<Exception> exceptions, IJSpace space) {
        for (OpenJPAStateManager sm : sms) {
            ClassMetaData cm = sm.getMetaData();
            if (_classesRelationStatus.containsKey(cm.getDescribedType()))
                continue;
            try {
                // Remove object from space
                final Object[] ids = ApplicationIds.toPKValues(sm.getObjectId(), cm);
                final ISpaceProxy proxy = (ISpaceProxy) space;                              
                final ITypeDesc typeDescriptor = proxy.getDirectProxy().getTypeManager().getTypeDesc(sm.getMetaData().getDescribedType().getName());
                final Object routing = sm.fetch(typeDescriptor.getRoutingPropertyId());                             
                final ITemplatePacket template = TemplatePacketFactory.createIdPacket(ids[0], routing, 0, typeDescriptor, TransportPacketType.ENTRY_PACKET);                     
                int result = proxy.clear(template, _transaction, 0);
                if (result != 1)
                    throw new Exception("Unable to clear object from space.");
            } catch (Exception e) {
                exceptions.add(e);
            }
        }
    }

    /**
     * Partially updates dirty fields to the space.
     */    
    private void handleUpdatedObjects(Collection<OpenJPAStateManager> sms, ArrayList<Exception> exceptions, IJSpace space) {
        // Generate a template for each state manager and use partial update for updating..         
        for (OpenJPAStateManager sm : sms) {
            ClassMetaData cm = sm.getMetaData();
            if (_classesRelationStatus.containsKey(cm.getDescribedType()))
                throw new RuntimeException("Updating an instance which is a part of a relation is not supported.");
            try {
                // Create an entry packet from the updated pojo and set all the fields but the updated & primary key to null.
                final ISpaceProxy proxy = (ISpaceProxy) space;
                final IEntryPacket entry = proxy.getDirectProxy().getTypeManager().getEntryPacketFromObject(
                        sm.getManagedInstance(), ObjectType.POJO, proxy);                                                
                for (int i = 0; i < cm.getDeclaredFields().length; i++) {
                    if (!sm.getDirty().get(i) && !cm.getFields()[i].isPrimaryKey()) {
                        entry.setFieldValue(i, null);
                    }
                }
                // Write changes to the space
                space.write(entry, _transaction, Lease.FOREVER, 0, UpdateModifiers.PARTIAL_UPDATE);                                
            } catch (Exception e) { 
                exceptions.add(e);
            }                       
        }
    }

    /**
     * Writes new persistent objects to the space.
     */
    private void handleNewObjects(Collection<OpenJPAStateManager> sms, ArrayList<Exception> exceptions, IJSpace space) {
        final HashMap<Class<?>, ArrayList<Object>> objectsToWriteByType = new HashMap<Class<?>, ArrayList<Object>>();
        final ArrayList<OpenJPAStateManager> stateManagersToRestore = new ArrayList<OpenJPAStateManager>();
        Class<?> previousType = null;
        ArrayList<Object> currentList = null;               
        for (OpenJPAStateManager sm : sms) {
            // If the current object is in a relation skip it
            if (_classesRelationStatus.containsKey(sm.getMetaData().getDescribedType())) {
                // Remove the state manager from objects in relation for making their serialization not
                // handled by OpenJPA which can cause a deadlock.
                sm.getPersistenceCapable().pcReplaceStateManager(null);
                stateManagersToRestore.add(sm);
                continue;
            }
            if (!sm.getMetaData().getDescribedType().equals(previousType)) {
                currentList = objectsToWriteByType.get(sm.getMetaData().getDescribedType());
                if (currentList == null) {
                    currentList = new ArrayList<Object>();
                    objectsToWriteByType.put(sm.getMetaData().getDescribedType(), currentList);
                }
            }
            currentList.add(sm.getManagedInstance());            
        }
        try {
            for (Map.Entry<Class<?>, ArrayList<Object>> entry : objectsToWriteByType.entrySet()) {
                space.writeMultiple(entry.getValue().toArray(), _transaction, Lease.FOREVER, UpdateModifiers.NO_RETURN_VALUE);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            // Restore the removed state managers.
            for (OpenJPAStateManager sm : stateManagersToRestore) {
                sm.getPersistenceCapable().pcReplaceStateManager(sm);
            }
        }
    }

    private synchronized void initializeClassRelationStatus() {
        if (_initializedClassRelationStatus)
            return;
        // Collect information regarding relationships.
        // Eventually classes which are in a relation should not be saved to the space
        // since we only support owned relationship and these instances will saved as nested instances
        // of their owning instance.
        ClassMetaData[] cms = getConfiguration().getMetaDataRepositoryInstance().getMetaDatas();
        for (ClassMetaData cm : cms) {
            if (!_processedClasses.contains(cm.getDescribedType())) {
                // Process class
                if (!_processedClasses.contains(cm.getDescribedType())) {
                    for (FieldMetaData fmd : cm.getFields()) {
                        if (fmd.getAssociationType() == FieldMetaData.ONE_TO_ONE) {
                            if (!_classesRelationStatus.containsKey(fmd.getDeclaredType())) {
                                _classesRelationStatus.put(fmd.getDeclaredType(), FieldMetaData.ONE_TO_ONE);
                            }
                        } else if (fmd.getAssociationType() == FieldMetaData.ONE_TO_MANY) {
                            if (!_classesRelationStatus.containsKey(fmd.getDeclaredType())) {
                                _classesRelationStatus.put(fmd.getElement().getDeclaredType(), FieldMetaData.ONE_TO_MANY);
                            }
                        }
                    }
                    _processedClasses.add(cm.getDescribedType());
                }
            }
        }
        _initializedClassRelationStatus = true;
    }


    /**
     * Initializes an ExternalEntry result as a state managed Pojo.
     * (used by JPQL's query executor)
     */
    public Object loadObject(ClassMetaData classMetaData, IEntryPacket entry) {
        // Get object id
        Object[] ids = new Object[1];
        ids[0] = entry.getID();
        Object objectId = ApplicationIds.fromPKValues(ids, classMetaData);
        return getContext().find(objectId, null, null, entry, 0);
    }

    /**
     * Gets the current active transaction.
     */
    public Transaction getCurrentTransaction() {
        return _transaction;
    }    
    
}
