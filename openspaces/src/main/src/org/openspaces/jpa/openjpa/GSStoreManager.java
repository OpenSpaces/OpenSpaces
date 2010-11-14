package org.openspaces.jpa.openjpa;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import net.jini.core.transaction.Transaction;
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
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.ExternalEntry;
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
            _transaction = (TransactionFactory.create(getConfiguration().getTransactionManager(),
                    getConfiguration().getLockTimeout())).transaction;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }            
    }

    @Override
    public void commit() {
        try {
            _transaction.commit();
        } catch (Exception e) {
           throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void rollback() {
        try {
            _transaction.abort();
        } catch (Exception e) {
           throw new RuntimeException(e.getMessage(), e);
        }        
    }    
    
    @Override
    public StoreQuery newQuery(String language) {        
        ExpressionParser ep = QueryLanguages.parserForLanguage(language);
        // Not implemented...
        //return new GSStoreQuery(ep, this);
        return null;
    }

    @Override
    protected OpenJPAConfiguration newConfiguration() {
        return new GSConfiguration();
    }

    public GSConfiguration getConfiguration() {
        return (GSConfiguration) getContext().getConfiguration();
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

    @Override
    public boolean initialize(OpenJPAStateManager sm, PCState state,
            FetchConfiguration fetchConfiguration, Object edata) {

        final ClassMetaData cm = sm.getMetaData();                                        
        try {
            ExternalEntry res = null;
            // If we already have the result and only need to initialize.. (relevant for JPQL)
            if (edata != null) {
                res = (ExternalEntry) edata;
            } else {
                final ISpaceProxy proxy = (ISpaceProxy) getConfiguration().getSpace();
                final Object[] ids = ApplicationIds.toPKValues(sm.getObjectId(), cm);        
                res = (ExternalEntry) proxy.readById(cm.getDescribedType().getName(), ids[0],
                        null, _transaction, 0, 0, false, QueryResultTypeInternal.EXTERNAL_ENTRY);

                if (res == null)
                    return false;            
            }
            // TODO: Handle sub-classes etc...
            sm.initialize(cm.getDescribedType(), state);                        
            
            FieldMetaData[] fms = cm.getFields();
            for (int i = 0; i < fms.length; i++) {
                // Skip primary keys and non-persistent keys
                if (fms[i].isPrimaryKey() || sm.getLoaded().get(fms[i].getIndex()))
                    continue;                
                sm.store(i, res.getFieldValue(i));
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
        // Prepare the external entry template using the objects id
        ClassMetaData cm = (ClassMetaData)sm.getMetaData();
        Object[] ids = ApplicationIds.toPKValues(sm.getObjectId(), cm);        
        ExternalEntry template = new ExternalEntry(cm.getDescribedType().getName(), new Object[cm.getFields().length]);
        for (int i = 0; i < ids.length; i++) {
            template.setFieldValue(cm.getPrimaryKeyFields()[i].getDeclaredIndex(), ids[i]);
        }
        // Read object from space
        IJSpace space = getConfiguration().getSpace();        
        try {        
            ExternalEntry result = (ExternalEntry)space.read(template, _transaction, 0);
            if (result == null)
                return false;
            // Process result - store only the relevant fields in the state manager
            for (int i = 0; i < cm.getDeclaredFields().length; i++) {
                if (fields.get(i))                
                    sm.store(i, result.getFieldValue(i));                
            }                                    
            return true;            
        } catch (Exception e) {
            System.out.println();
        }                    
        return false;
    }

    @Override
    public ResultObjectProvider executeExtent(ClassMetaData classmetadata, boolean flag,
            FetchConfiguration fetchconfiguration) {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * Flushes changes to GigaSpaces.
     * Returns a list of exceptions that occurred.
     */
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
                Object result = proxy.takeById(cm.getDescribedType().getName(), ids[0], null, _transaction,
                        0, 0, false, QueryResultTypeInternal.EXTERNAL_ENTRY);                
                if (result == null)
                    throw new Exception("Removed object not found in space.");                
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
            try {                                
                // Read object from space
                Object[] ids = ApplicationIds.toPKValues(sm.getObjectId(), cm);
                ISpaceProxy proxy = (ISpaceProxy) space;
                ExternalEntry result = (ExternalEntry) proxy.readById(
                        cm.getDescribedType().getName(), ids[0], null, _transaction, 0, ReadModifiers.EXCLUSIVE_READ_LOCK,
                        false, QueryResultTypeInternal.EXTERNAL_ENTRY);
                if (result == null)
                    throw new Exception("Updated object not found in space.");
                // Calculate dirty fields count
                int numberOfDirtyFields = 0;
                for (int i = 0; i < cm.getDeclaredFields().length; i++) {
                    if (sm.getDirty().get(i))
                        numberOfDirtyFields++;
                }
                // Generate an external entry template using the dirty fields & the id field
                String[] fieldNames = new String[numberOfDirtyFields + 1];
                Object[] fieldValues = new Object[numberOfDirtyFields + 1];
                int dirtyIndex = 1;
                for (int i = 0; i < cm.getDeclaredFields().length; i++) {
                    if (sm.getDirty().get(i)) {
                        fieldNames[dirtyIndex] = cm.getDeclaredFields()[i].getName();
                        fieldValues[dirtyIndex] = sm.fetch(i);
                        dirtyIndex++;
                    }
                }
                fieldNames[0] = result.getPrimaryKeyName();
                fieldValues[0] = ids[0];
                ExternalEntry template = new ExternalEntry(cm.getDescribedType().getName(), fieldValues, fieldNames);
                template.setUID(result.getUID());
                // Write changes to the space
                space.write(template, _transaction, 0, 0, UpdateModifiers.PARTIAL_UPDATE);                                
            } catch (Exception e) { 
                exceptions.add(e);
            }                       
        }
    }

    /**
     * Writes new persistent objects to the space.
     */
    private void handleNewObjects(Collection<OpenJPAStateManager> sms, ArrayList<Exception> exceptions, IJSpace space) {    
        for (OpenJPAStateManager sm : sms) {
            // If the current object is in a relation skip it
            if (_classesRelationStatus.containsKey(sm.getMetaData().getDescribedType()))
                continue;            
            try {                                
                space.write(sm.getManagedInstance(), _transaction, 0);
            } catch (Exception e) {
                exceptions.add(e);
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
    public Object loadObject(ClassMetaData classMetaData, ExternalEntry entry) {
        // Get object id
        Object[] primaryKeys = new Object[classMetaData.getPrimaryKeyFields().length];
        for (int i = 0; i < primaryKeys.length; i++) {
            primaryKeys[i] = entry.getFieldValue(classMetaData.getPrimaryKeyFields()[i].getIndex());
        }
        Object objectId = ApplicationIds.fromPKValues(primaryKeys, classMetaData);
        return getContext().find(objectId, null, null, entry, 0);
    }


}
