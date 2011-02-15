package org.openspaces.jpa;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.TransactionFactory;

import org.apache.openjpa.abstractstore.AbstractStoreManager;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.PCState;
import org.apache.openjpa.kernel.QueryLanguages;
import org.apache.openjpa.kernel.StateManager;
import org.apache.openjpa.kernel.StoreQuery;
import org.apache.openjpa.kernel.exps.ExpressionParser;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.util.ApplicationIds;
import org.openspaces.jpa.openjpa.SpaceConfiguration;
import org.openspaces.jpa.openjpa.StoreManagerQuery;

import com.gigaspaces.annotation.pojo.SpaceId;
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
import com.j_spaces.jdbc.QueryProcessorFactory;
import com.j_spaces.jdbc.driver.GConnection;

/**
 * A GigaSpaces back-end implementation for OpenJPA.
 * Responsible for storing and fetching data from GigaSpaces using space API.
 * 
 * @author idan
 * @since 8.0
 *
 */
@SuppressWarnings("unchecked")
public class StoreManager extends AbstractStoreManager {
    //
    private Transaction _transaction = null;
    private static final Map<Class<?>, Integer> _classesRelationStatus = new HashMap<Class<?>, Integer>();
    private static final HashSet<Class<?>> _processedClasses = new HashSet<Class<?>>();
    private GConnection _connection;
    private RelationsManager _relationsManager;
    
    public StoreManager() {
        _relationsManager = new RelationsManager();
    }
    
    @Override
    protected void open() {
        // Specific gigaspaces initialization (space proxy)
        getConfiguration().initialize();
    }
    
    @Override
    protected Collection<String> getUnsupportedOptions() {
        Collection<String> unsupportedOptions = (Collection<String>) super.getUnsupportedOptions();
        unsupportedOptions.remove(OpenJPAConfiguration.OPTION_ID_DATASTORE);        
        return unsupportedOptions;
    }

    @Override
    public boolean syncVersion(OpenJPAStateManager sm, Object edata) {
        try {
            // Read object from space
            IEntryPacket result = readObjectFromSpace(sm);
            if (result == null)
                return false;
            // Populate fields
            loadFields(sm, result, sm.getMetaData().getFields());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
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
            _transaction.abort(Long.MAX_VALUE);
        } catch (Exception e) {
           throw new RuntimeException(e.getMessage(), e);
        } finally {
            _transaction = null;
        }
    }    
    
    @Override
    public StoreQuery newQuery(String language) {        
        ExpressionParser ep = QueryLanguages.parserForLanguage(language);
        return new StoreManagerQuery(ep, this);
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

    @SuppressWarnings({ "rawtypes" })
    @Override
    public Collection loadAll(Collection sms, PCState state, int load, FetchConfiguration fetch, Object edata) {
            return super.loadAll(sms, state, load, fetch, edata);
        }

    @Override
    public boolean initialize(OpenJPAStateManager sm, PCState state,
            FetchConfiguration fetchConfiguration, Object edata) {

        final ClassMetaData cm = sm.getMetaData();
        try {
            // If we already have the result and only need to initialize.. (relevant for nested objects & JPQL)
            IEntryPacket result =
                (edata == null) ? readObjectFromSpace(sm) : (IEntryPacket) edata;
            if (result == null)
                return false;  
            
            // Initialize
            sm.initialize(cm.getDescribedType(), state);
            loadFields(sm, result, cm.getFields());            
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        
        return true;        
    }

    /**
     * Loads the provided IEntryPacket field values to the provided StateManager.
     * @param sm The state manager.
     * @param entry The IEntryPacket containing the field values.
     * @param fms The fields meta data.
     */
    private void loadFields(OpenJPAStateManager sm, IEntryPacket entry, FieldMetaData[] fms) {
        for (int i = 0; i < fms.length; i++) {
            // Skip primary keys and non-persistent keys
            if (fms[i].isPrimaryKey() || sm.getLoaded().get(fms[i].getIndex()))
                continue;
            Integer associationType = _classesRelationStatus.get(fms[i].getElement().getDeclaredType());
            if (associationType != null)
                fms[i].setAssociationType(associationType);

            // Handle one-to-one
            if (fms[i].getAssociationType() == FieldMetaData.ONE_TO_ONE) {
                loadOneToOneObject(fms[i], sm, entry.getFieldValue(i));
                
            // Handle one-to-many
            } else if (fms[i].getAssociationType() == FieldMetaData.ONE_TO_MANY) {
                loadOneToManyObjects(fms[i], sm, entry.getFieldValue(i));
                
            // Handle embedded property
            } else if (fms[i].isEmbeddedPC()) {
                loadEmbeddedObject(fms[i], sm, entry.getFieldValue(i));
                
            // Otherwise, store the value as is
            } else {
                sm.store(i, entry.getFieldValue(i));
            }
        }
    }

    /**
     * Loads a One-to-one relationship object to the provided owner's state manager.
     * @param fmd The owner's field meta data.
     * @param sm The owner's state manager.
     * @param fieldValue The One-to-one field value to load into the owner's state manager.
     */
    private void loadOneToOneObject(FieldMetaData fmd, OpenJPAStateManager sm, Object fieldValue) {
        if (fieldValue == null) {
            sm.storeObject(fmd.getIndex(), null);
        } else {
            final ISpaceProxy proxy = (ISpaceProxy) getConfiguration().getSpace();
            final IEntryPacket entry = proxy.getDirectProxy().getTypeManager().getEntryPacketFromObject(
                    fieldValue, ObjectType.POJO, proxy);
            final ClassMetaData cmd = fmd.getDeclaredTypeMetaData();
            final Object oid = ApplicationIds.fromPKValues(new Object[] { entry.getID() }, cmd);
            final BitSet exclude = new BitSet(cmd.getFields().length);                               
            final Object managedObject = getContext().find(oid, null, exclude, entry, 0);
            _relationsManager.setOwnerStateManagerForPersistentInstance(managedObject, sm);
            sm.storeObject(fmd.getIndex(), managedObject);
        }
    }

    /**
     * 
     * @param fmd
     * @param sm
     * @param fieldValue
     */
    private void loadEmbeddedObject(FieldMetaData fmd, OpenJPAStateManager sm, Object fieldValue) {
        if (fieldValue == null) {
            sm.storeObject(fmd.getIndex(), null);
        } else {
            if (fieldValue != null) {                
                final OpenJPAStateManager em = ctx.embed(null, null, sm, fmd);
                sm.storeObject(fmd.getIndex(), em.getManagedInstance());
                final ISpaceProxy proxy = (ISpaceProxy) getConfiguration().getSpace();                      
                final IEntryPacket entry = proxy.getDirectProxy().getTypeManager().getEntryPacketFromObject(
                        fieldValue, ObjectType.POJO, proxy);
                loadFields(em, entry, fmd.getDeclaredTypeMetaData().getFields());
            }
        }
    }

    /**
     * Loads One-to-many relationship objects to the owner's state manager.
     * 
     * @param fmd The One-to-many field's meta data. 
     * @param sm The owner's state manager.
     * @param fieldValue The value to be stored for the current field.
     */
    private void loadOneToManyObjects(FieldMetaData fmd, OpenJPAStateManager sm, Object fieldValue) {
        final Object collection = sm.newProxy(fmd.getIndex());

        if (fieldValue != null) {
            final ISpaceProxy proxy = (ISpaceProxy) getConfiguration().getSpace();
            final ClassMetaData cmd = fmd.getElement().getDeclaredTypeMetaData();
            final BitSet exclude = new BitSet(cmd.getFields().length);

            // Initialize each of the collection's items
            for (Object item : (Collection<?>) fieldValue) {
                final IEntryPacket entry = proxy.getDirectProxy().getTypeManager().getEntryPacketFromObject(
                        item, ObjectType.POJO, proxy);
                final Object oid = ApplicationIds.fromPKValues(new Object[] { entry.getID() }, cmd);
                // Initialize a state manager for the current item
                final Object managedObject = getContext().find(oid, null, exclude, entry, 0);
                _relationsManager.setOwnerStateManagerForPersistentInstance(managedObject, sm);
                ((Collection<Object>) collection).add(managedObject);
            }
        }
        sm.storeObject(fmd.getIndex(), collection);
    }

    /**
     * Reads an IEntryPacket implementation from space according to the provided StateManager.
     * @param sm The state manager.
     * @return The IEntryPacket implementation for the provided StateManager.
     */
    private IEntryPacket readObjectFromSpace(OpenJPAStateManager sm)
            throws UnusableEntryException, TransactionException, InterruptedException, RemoteException {
        IEntryPacket result;
        final ISpaceProxy proxy = (ISpaceProxy) getConfiguration().getSpace();
        final ITypeDesc typeDescriptor = proxy.getDirectProxy().getTypeManager().getTypeDescByName(
                sm.getMetaData().getDescribedType().getName());                                
        final Object[] ids = ApplicationIds.toPKValues(sm.getObjectId(), sm.getMetaData());
        final int readModifier = (_transaction != null)? getConfiguration().getReadModifier()
                : ReadModifiers.REPEATABLE_READ;

        ITemplatePacket template;
        if (typeDescriptor.isAutoGenerateId())
            template = TemplatePacketFactory.createUidPacket((String) ids[0], null, 0, TransportPacketType.ENTRY_PACKET);
        else
            template = TemplatePacketFactory.createIdPacket(ids[0], null, 0, typeDescriptor, TransportPacketType.ENTRY_PACKET);
        result = (IEntryPacket) proxy.read(template, _transaction, 0, readModifier);
        return result;
    }

    /**
     * This method loads specific fields from the data store for updating them.
     * Note: The state manager's fields are cleared.
     */
    @Override
    public boolean load(OpenJPAStateManager sm, BitSet fields, FetchConfiguration fetch, int lockLevel, Object context) {
        final ClassMetaData cm = (ClassMetaData) sm.getMetaData();
        if (_classesRelationStatus.containsKey(cm.getDescribedType()))
            return true;
        
        final Object[] ids = ApplicationIds.toPKValues(sm.getObjectId(), cm);        
        final IJSpace space = getConfiguration().getSpace();
        final ITypeDesc typeDescriptor = ((ISpaceProxy) space).getDirectProxy().getTypeManager().getTypeDescByName(cm.getDescribedType().getName());
        ITemplatePacket template;
        if (typeDescriptor.isAutoGenerateId())
            template = TemplatePacketFactory.createUidPacket((String) ids[0], null, 0, TransportPacketType.ENTRY_PACKET);
        else
            template = TemplatePacketFactory.createIdPacket(ids[0], null, 0, typeDescriptor, TransportPacketType.ENTRY_PACKET);
        try {        
            // Read object from space                
            IEntryPacket result = (IEntryPacket) space.read(template, _transaction, 0); 
            if (result == null)
                return false;
            // Process result - store only the relevant fields in the state manager
            loadFields(sm, result, cm.getFields());
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
    @SuppressWarnings({ "rawtypes" })
    @Override
    protected Collection flush(Collection pNew, Collection pNewUpdated, Collection pNewFlushedDeleted,
            Collection pDirty, Collection pDeleted) {
        
        IJSpace space = getConfiguration().getSpace();
                
        ArrayList<Exception> exceptions = new ArrayList<Exception>();

        if (_relationsManager.shouldInitializeClassesRelationStatus())
            _relationsManager.initializeClassesRelationStatus();
        
        if (pNew.size() > 0)
            handleNewObjects(pNew, space);
        
        if (pDirty.size() > 0)
            handleUpdatedObjects(pDirty, exceptions, space);
        
        if (pDeleted.size() > 0)
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
                final ITypeDesc typeDescriptor = proxy.getDirectProxy().getTypeManager().getTypeDescByName(sm.getMetaData().getDescribedType().getName());
                final Object routing = sm.fetch(typeDescriptor.getRoutingPropertyId());                             
                ITemplatePacket template;
                if (typeDescriptor.isAutoGenerateId())
                    template = TemplatePacketFactory.createUidPacket((String) ids[0], routing, 0, TransportPacketType.ENTRY_PACKET);
                else
                    template = TemplatePacketFactory.createIdPacket(ids[0], routing, 0, typeDescriptor, TransportPacketType.ENTRY_PACKET);
                
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
            final ClassMetaData cm = sm.getMetaData();
            try {
                // Find relationship owner and flush it to space
                if (_classesRelationStatus.containsKey(cm.getDescribedType())) {
                    final StateManager stateManagerToUpdate = _relationsManager.getStateManagerToUpdate((StateManager) sm);
                    if (sms.contains(stateManagerToUpdate))
                        continue;
                    
                    final ISpaceProxy proxy = (ISpaceProxy) space;
                    final IEntryPacket entry = proxy.getDirectProxy().getTypeManager().getEntryPacketFromObject(
                            stateManagerToUpdate.getManagedInstance(), ObjectType.POJO, proxy);                                                
                    // Write changes to the space
                    for (FieldMetaData fmd : cm.getFields()) {
                        _relationsManager.initializeOwnerReferencesForField((StateManager) sm, fmd);
                    }
                    space.write(entry, _transaction, Lease.FOREVER, 0, UpdateModifiers.UPDATE_ONLY);
                    
                } else {
                    // Create an entry packet from the updated POJO and set all the fields
                    // but the updated & primary key to null.
                    final ISpaceProxy proxy = (ISpaceProxy) space;
                    final IEntryPacket entry = proxy.getDirectProxy().getTypeManager().getEntryPacketFromObject(
                            sm.getManagedInstance(), ObjectType.POJO, proxy);                                                
                    FieldMetaData[] fmds = cm.getFields();
                    for (int i = 0; i < fmds.length; i++) {
                        if (!sm.getDirty().get(i) && !fmds[i].isPrimaryKey()) {
                            entry.setFieldValue(i, null);
                        } else {
                            _relationsManager.initializeOwnerReferencesForField((StateManager) sm, fmds[i]);
                        }
                    }
                    // Write changes to the space
                    space.write(entry, _transaction, Lease.FOREVER, 0, UpdateModifiers.PARTIAL_UPDATE);
                }
            } catch (Exception e) { 
                exceptions.add(e);
            }
        }
    }

    /**
     * Writes new persistent objects to the space.
     */
    private void handleNewObjects(Collection<OpenJPAStateManager> sms, IJSpace space) {
        final HashMap<Class<?>, ArrayList<Object>> objectsToWriteByType = new HashMap<Class<?>, ArrayList<Object>>();
        final ArrayList<OpenJPAStateManager> stateManagersToRestore = new ArrayList<OpenJPAStateManager>();
        Class<?> previousType = null;
        ArrayList<Object> currentList = null;               
        for (OpenJPAStateManager sm : sms) {
            // If the current object is in a relation skip it
            if (_classesRelationStatus.containsKey(sm.getMetaData().getDescribedType())) {
                continue;
            }
            // If the object has managed instances in its fields we need to remove the state manager from these instances
            // since they are serialized when written to space and can cause a deadlock when written
            // by writeMultiple.
            _relationsManager.removeOwnedEntitiesStateManagers(stateManagersToRestore, sm);            
            
            // In order to use writeMultiple we need to gather each type's instances to its own list
            if (!sm.getMetaData().getDescribedType().equals(previousType)) {
                currentList = objectsToWriteByType.get(sm.getMetaData().getDescribedType());
                if (currentList == null) {
                    currentList = new ArrayList<Object>();
                    objectsToWriteByType.put(sm.getMetaData().getDescribedType(), currentList);
                }
                previousType = sm.getMetaData().getDescribedType();
            }
            
            // Each persisted class should have its state manager removed
            // before being written to space since gigaspaces reflection conflicts with
            // OpenJPA's class monitoring.
            sm.getPersistenceCapable().pcReplaceStateManager(null);
            stateManagersToRestore.add(sm);
            currentList.add(sm.getManagedInstance());            
        }
        // Write objects to space in batches by type
        try {
            for (Map.Entry<Class<?>, ArrayList<Object>> entry : objectsToWriteByType.entrySet()) {
                space.writeMultiple(entry.getValue().toArray(), _transaction, Lease.FOREVER, UpdateModifiers.WRITE_ONLY);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            // Restore the removed state managers.
            _relationsManager.restoreRemovedStateManagers(stateManagersToRestore);
        }
    }

    /**
     * Validates the provided class' annotations.
     * Currently the only validation performed is for @Id & @SpaceId annotations
     * that must be declared on the same getter.  
     */
    private void validateClassAnnotations(Class<?> type) {
        // Validation is only relevant for Entities
        if (type.getAnnotation(Entity.class) == null)
            return;
        
        for (Method getter : type.getMethods()) {
            
            if (!getter.getName().startsWith("get"))
                continue;
            
            SpaceId spaceId = getter.getAnnotation(SpaceId.class);
            boolean hasJpaId = getter.getAnnotation(Id.class) != null || getter.getAnnotation(EmbeddedId.class) != null; 
            if (spaceId != null || hasJpaId) {                
                if (!hasJpaId || spaceId == null)
                    throw new IllegalArgumentException("SpaceId and Id annotations must both be declared on the same property in JPA entities in type: " + type.getName());
                if (spaceId.autoGenerate()) {
                    GeneratedValue generatedValue = getter.getAnnotation(GeneratedValue.class);
                    if (generatedValue == null)
                        throw new IllegalArgumentException("SpaceId with autoGenerate=true annotated property should also have a JPA GeneratedValue annotation.");
                }
                break;
            }
        }        
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
    
    /**
     * Gets a JDBC connection using the configuration's space instance.
     * Each store manager has its own Connection for Multithreaded reasons. 
     */
    public GConnection getJdbcConnection() throws SQLException {
        if (_connection == null) {
            if (_connection == null) {
                Properties connectionProperties = new Properties();
                connectionProperties.put(
                        QueryProcessorFactory.COM_GIGASPACES_EMBEDDED_QP_ENABLED, "true");
                _connection = GConnection.getInstance(getConfiguration().getSpace(), connectionProperties);
                if (_connection.getAutoCommit())
                    _connection.setAutoCommit(false);
            }
        }
        return _connection;        
    }
    
    /**
     * Gets the class relation status (one-to-one etc..) for the provided type.
     */
    public synchronized int getClassRelationStatus(Class<?> type) {
        // In case relations status was not initialized already..
        if (_relationsManager.shouldInitializeClassesRelationStatus())
            _relationsManager.initializeClassesRelationStatus();
        // Get relation status..
        Integer relationStatus = _classesRelationStatus.get(type);
        return (relationStatus == null) ? FieldMetaData.MANAGE_NONE : relationStatus;
    }

    
    /**
     * StoreManager's relationships manager.
     * Provides methods for handling relationships in GigaSpaces owned relationships model. 
     */
    private class RelationsManager {

        public RelationsManager() {
        }
        
        /**
         * Removes owned entities state managers (before writing them to space due to serialization deadlock problem).
         * The removed state managers are kept in the provided collection for restoring them later.
         * 
         * @param stateManagersToRestore The collection for storing the removed state managers.
         * @param sm The owning entity's state manager.
         */
        public void removeOwnedEntitiesStateManagers(Collection<OpenJPAStateManager> stateManagersToRestore,
                OpenJPAStateManager sm) {
            // Remove the state manager from objects in relation for making their serialization not
            // handled by OpenJPA which can cause a deadlock when writing to space.
            // The deadlock is caused because when serializing a monitored instance, OpenJPA takes over
            // and attempts to access an already locked layer in OpenJPA's hierarchy which causes
            // a deadlock.
            for (FieldMetaData fmd : sm.getMetaData().getFields()) {
                if (fmd.isEmbeddedPC()) {
                    Object value = sm.fetch(fmd.getDeclaredIndex());
                    if (value != null) {
                        PersistenceCapable pc = (PersistenceCapable) value;
                        OpenJPAStateManager stateManager = (OpenJPAStateManager) pc.pcGetStateManager();
                        removeOwnedEntitiesStateManagers(stateManagersToRestore, stateManager);
                        pc.pcReplaceStateManager(null);
                        stateManagersToRestore.add(stateManager);
                    }
                } else if (fmd.getAssociationType() == FieldMetaData.ONE_TO_MANY) {
                    Collection<?> collection = (Collection<?>) sm.fetch(fmd.getIndex());
                    if (collection != null) {
                        for (Object item : collection) {
                            // Set relationship owner
                            setOwnerStateManagerForPersistentInstance(item, sm);
                            PersistenceCapable pc = (PersistenceCapable) item;
                            OpenJPAStateManager stateManager = (OpenJPAStateManager) pc.pcGetStateManager();
                            removeOwnedEntitiesStateManagers(stateManagersToRestore, stateManager);
                            stateManagersToRestore.add(stateManager);
                            pc.pcReplaceStateManager(null);
                        }
                    }
                } else if (fmd.getAssociationType() == FieldMetaData.ONE_TO_ONE) {
                    Object value = sm.fetch(fmd.getIndex());
                    if (value != null) {
                        setOwnerStateManagerForPersistentInstance(value, sm);
                        PersistenceCapable pc = (PersistenceCapable) value;
                        OpenJPAStateManager stateManager = (OpenJPAStateManager) pc.pcGetStateManager();
                        removeOwnedEntitiesStateManagers(stateManagersToRestore, stateManager);
                        stateManagersToRestore.add(stateManager);
                        pc.pcReplaceStateManager(null);
                    }
                }
            }
        }
        
        /**
         * Sets the provided state manager as the managed object's owner.
         * @param managedObject The managed object to set the owner for.
         * @param sm The owner's state manager.
         */
        public void setOwnerStateManagerForPersistentInstance(Object managedObject, OpenJPAStateManager sm) {
            StateManager stateManager = (StateManager)((PersistenceCapable) managedObject).pcGetStateManager();
            stateManager.setOwnerStateManager((StateManager) sm);
        }
        
        /**
         * Sets the provided state manager as the owner for the provided field value.
         * @param sm The owner's state manager.
         * @param fmd The field's value the owner will be set for.
         */
        public void initializeOwnerReferencesForField(StateManager sm, FieldMetaData fmd) {
            if (fmd.getAssociationType() == FieldMetaData.ONE_TO_MANY) {
                Collection<?> collection = (Collection<?>) sm.fetch(fmd.getIndex());
                if (collection != null) {
                    for (Object item : collection) {
                        if (item != null) {
                            _relationsManager.setOwnerStateManagerForPersistentInstance(item, sm);
                        }
                    }
                }
            } else if (fmd.getAssociationType() == FieldMetaData.ONE_TO_ONE) {
                Object value = sm.fetch(fmd.getIndex());
                if (value != null) {
                    _relationsManager.setOwnerStateManagerForPersistentInstance(value, sm);
                }
            }
        }
        
        /**
         * Attempts to find the super-owner of the provided state manager in a relationship to update.
         * Throws an exception if such a state manager doesn't exist.
         * @param sm The owned relationship state manager.
         * @return The super-owner state manager of the relationship.
         */
        public StateManager getStateManagerToUpdate(StateManager sm) {
            final Integer associationType = _classesRelationStatus.get(sm.getMetaData().getDescribedType());
            if (associationType == null)
                throw new IllegalStateException("Error updating: " + sm.getMetaData().getClass().getName()
                        + " with id: " + sm.getId());
            final StateManager ownerStateManager = sm.getOwnerStateManager();
            if (ownerStateManager != null) {
                if (associationType == FieldMetaData.ONE_TO_MANY) {
                    for (FieldMetaData fmd : ownerStateManager.getMetaData().getFields()) {
                        if (fmd.getElement().getDeclaredType().equals(sm.getMetaData().getDescribedType())) {
                            Collection<?> collection = (Collection<?>) ownerStateManager.fetch(fmd.getIndex());
                            if (collection == null || !collection.contains(sm.getManagedInstance()))
                                break;
                            if (ownerStateManager.getOwnerStateManager() != null)
                                return getStateManagerToUpdate(ownerStateManager);
                            return ownerStateManager;
                        }
                    }
                } else if (associationType == FieldMetaData.ONE_TO_ONE) {
                    for (FieldMetaData fmd : ownerStateManager.getMetaData().getFields()) {
                        if (fmd.getDeclaredType().equals(sm.getMetaData().getDescribedType())) {
                            Object value = ownerStateManager.fetch(fmd.getIndex());
                            if (value == null || !value.equals(sm.getManagedInstance()))
                                break;
                            if (ownerStateManager.getOwnerStateManager() != null)
                                return getStateManagerToUpdate(ownerStateManager);
                            return ownerStateManager;
                        }
                    }            
                }
            }
            throw new IllegalStateException("Attempted to update an owned entity: "
                    + sm.getMetaData().getClass().getName() + " with Id: " + sm.getId() + " which has no owner.");
        }
        
        /**
         * Restores state managers for the provided collection of state managers.
         * @param stateManagersToRestore State managers collection to restore.
         */
        public void restoreRemovedStateManagers(Collection<OpenJPAStateManager> stateManagersToRestore) {
            for (OpenJPAStateManager sm : stateManagersToRestore) {
                sm.getPersistenceCapable().pcReplaceStateManager(sm);
            }
        }
        
        /**
         * Collects information on current OpenJPA listed class meta data list.
         * On every call to flush() the method is called & checks if there are new classes to initialize.
         */
        public synchronized void initializeClassesRelationStatus() {
            if (!shouldInitializeClassesRelationStatus())
                return;
            // Collect information regarding relationships.
            // Eventually classes which are in a relation should not be saved to the space
            // since we only support owned relationships and these instances will be saved as nested instances
            // of their owning instance.
            ClassMetaData[] cms = getConfiguration().getMetaDataRepositoryInstance().getMetaDatas();
            for (ClassMetaData cm : cms) {
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
                        } else if (fmd.getAssociationType() == FieldMetaData.MANY_TO_MANY) {
                            throw new IllegalArgumentException("Many-to-many is not supported.");
                        }
                    }
                    validateClassAnnotations(cm.getDescribedType());
                    _processedClasses.add(cm.getDescribedType());
                }
            }
        }

        /**
         * Gets whether classes relations status is not complete and should be synchronized.
         * OpenJPA creates class meta data only after an entity is persisted for the first time.
         */
        public boolean shouldInitializeClassesRelationStatus() {            
            return getConfiguration().getMetaDataRepositoryInstance().getMetaDatas().length != _processedClasses.size();
        }
        
    }
    
}
