package org.openspaces.jpa.openjpa;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionFactory;

import org.apache.openjpa.abstractstore.AbstractStoreManager;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.PCState;
import org.apache.openjpa.kernel.StoreQuery;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.util.ApplicationIds;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.ExternalEntry;
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

    private Transaction _transaction = null;
    
    @Override
    protected Collection getUnsupportedOptions() {
        Collection<String> unsupportedOptions = (Collection<String>)super.getUnsupportedOptions();
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
            _transaction = (TransactionFactory.create(getConfiguration().getTransactionManager(), getConfiguration().getLockTimeout())).transaction;
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
    protected void open() {
        // Specific gigaspaces initialization (space proxy)
        getConfiguration().initialize();
    }

    
    @Override
    public StoreQuery newQuery(String language) {        
    	// Not implemented..
        //ExpressionParser ep = QueryLanguages.parserForLanguage(language);
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
    
    public boolean exists(OpenJPAStateManager sm, Object edata) {
        return true;
    }

    public boolean isCached(List<Object> oids, BitSet edata) {
        return false;
    }

    @Override
    public Collection loadAll(Collection sms, PCState state, int load,
            FetchConfiguration fetch, Object edata) {
            return null;
        }

    @Override
    public boolean initialize(OpenJPAStateManager sm, PCState state,
            FetchConfiguration fetchConfiguration, Object edata) {

        // TODO: Optimization when edata != null
        
        final IJSpace space = getConfiguration().getSpace();        
        final ClassMetaData cm = sm.getMetaData();        
        final Object[] ids = ApplicationIds.toPKValues(sm.getObjectId(), cm);        
        final ExternalEntry template = new ExternalEntry(cm.getDescribedType().getName(), new Object[cm.getFields().length]);

        for (int i = 0; i < ids.length; i++) {
            template.setFieldValue(cm.getPrimaryKeyFields()[i].getDeclaredIndex(), ids[i]);
        }
        
        try {                        
            ExternalEntry res = (ExternalEntry)space.read(template, _transaction, 0);            
            if (res == null)
                return false;            

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
            return false;
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
            try {
                // Remove object from space
                Object[] ids = ApplicationIds.toPKValues(sm.getObjectId(), cm);        
                ExternalEntry template = new ExternalEntry(cm.getDescribedType().getName(), new Object[cm.getFields().length]);
                for (int i = 0; i < ids.length; i++) {
                    template.setFieldValue(cm.getPrimaryKeyFields()[i].getDeclaredIndex(), ids[i]);
                }                
                int removed = space.clear(template, _transaction, 0); 
                if (removed != 1)
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
                ExternalEntry template = new ExternalEntry(cm.getDescribedType().getName(), new Object[cm.getFields().length]);
                for (int i = 0; i < ids.length; i++) {
                    template.setFieldValue(cm.getPrimaryKeyFields()[i].getDeclaredIndex(), ids[i]);
                }
                ExternalEntry result = (ExternalEntry)space.read(template, _transaction, 0);
                if (result == null)
                    throw new Exception("Updated object not found in space.");
                // Calculate dirty fields count
                int numberOfDirtyFields = 0;
                for (int i = 0; i < cm.getDeclaredFields().length; i++) {
                    if (sm.getDirty().get(i))
                        numberOfDirtyFields++;
                }
                // Generate an external entry template using the dirty fields
                String[] fieldNames = new String[numberOfDirtyFields];
                Object[] fieldValues = new Object[numberOfDirtyFields];
                int dirtyIndex = 0;
                for (int i = 0; i < cm.getDeclaredFields().length; i++) {
                    if (sm.getDirty().get(i)) {
                        fieldNames[dirtyIndex] = cm.getDeclaredFields()[i].getName();
                        fieldValues[dirtyIndex] = sm.fetch(i);
                        dirtyIndex++;
                    }
                }
                template = new ExternalEntry(cm.getDescribedType().getName(), fieldValues, fieldNames);
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

        HashSet<Object> instancesInRelation = new HashSet<Object>();

        for (OpenJPAStateManager sm : sms) {

            try {
                // Check if one of the fields is a relation instance and if so save the instance
                for (FieldMetaData fieldMetaData : sm.getMetaData().getFields()) {
                    if (fieldMetaData.getBackingMember() instanceof AnnotatedElement) {                            
                        AnnotatedElement ae = (AnnotatedElement)fieldMetaData.getBackingMember();                            
                        // Handle @OneToOne & @OneToMany annotations (its not necessary to take care
                        // of @Embedded since these instances don't have their own state manager)
                        if (ae.isAnnotationPresent(OneToOne.class)) {                                
                            Object instanceInRelation = sm.fetch(fieldMetaData.getIndex());
                            if (instanceInRelation != null) {
                                instancesInRelation.add(instanceInRelation);
                            }                                
                        } else if (ae.isAnnotationPresent(OneToMany.class)) {
                            Collection<?> oneToManyInstances = (Collection<?>)sm.fetch(fieldMetaData.getIndex());
                            if (oneToManyInstances != null) {
                                for (Object obj : oneToManyInstances) {
                                    instancesInRelation.add(obj);
                                }
                            }
                        }
                    }
                }

                // If the current instance is not in a relation write it to the space
                if (!instancesInRelation.contains(sm.getManagedInstance())) {
                    space.write(sm.getManagedInstance(), _transaction, 0);
                }

            } catch (Exception e) {
                exceptions.add(e);
            }
        }
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
