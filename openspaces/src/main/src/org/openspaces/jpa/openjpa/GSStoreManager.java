package org.openspaces.jpa.openjpa;

import java.lang.reflect.AnnotatedElement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.apache.openjpa.jdbc.kernel.ConnectionInfo;
import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStoreManager;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.PCState;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.util.ApplicationIds;
import org.openspaces.core.space.UrlSpaceConfigurer;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.ExternalEntry;

/**
 * GigaSpaces OpenJPA StoreManager implementation.
 * 
 * The following implementation is the layer resposible for loading/saving objects from GigaSpaces.
 * Currently there's a mix between JDBC & SpaceAPI but eventually only SpaceAPI will be used.
 * 
 * Notes:
 *  Only flush (persist) & initialize (find) methods have been overriden.
 *  JPQL is functional for non relational queries & objects.
 * 
 * @author idan
 * @since 8.0
 * 
 */
public class GSStoreManager extends JDBCStoreManager {
    
    private static final String GS_JDBC_URL_PREFIX = "jdbc:gigaspaces:url:";
    private IJSpace _space;
    
    @Override
    public Connection getConnection() {
        // Set a space proxy according to the provided JDBC url
        String connectionUrl = getConfiguration().getConnectionURL();
        String spaceUrl = null;

        if (connectionUrl.startsWith(GS_JDBC_URL_PREFIX))
            spaceUrl = connectionUrl.substring(GS_JDBC_URL_PREFIX.length());
        else
            throw new IllegalArgumentException("Invalid connection url in JDBC configuration.");
        
        _space = new UrlSpaceConfigurer(spaceUrl).space();
        
        return super.getConnection();
    }
    
    
    @Override
    protected boolean initializeState(OpenJPAStateManager sm, PCState state,
            JDBCFetchConfiguration fetch, ConnectionInfo info)
            throws ClassNotFoundException, SQLException {

        // When using JPQL info.result != null which means that a JDBC query has already been
        // executed. This is not enough for owned relationships and therefore currently we call
        // space.read for getting the desired object with its nested objects.
        
        if (_space == null)
            getConnection();
        
        ClassMapping cm = (ClassMapping)sm.getMetaData();
        String className = cm.getDescribedType().getName();
        
        Object[] ids = ApplicationIds.toPKValues(sm.getObjectId(), cm);
        
        ExternalEntry ee = new ExternalEntry(className, new Object[cm.getFields().length]);

        for (int i = 0; i < ids.length; i++) {
            ee.setFieldValue(cm.getPrimaryKeyFields()[i].getDeclaredIndex(), ids[i]);
        }
        
        try {                        
            ExternalEntry res = (ExternalEntry)_space.read(ee, null, 0);            
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
        }
        
        return true;        
    }
    
    @Override
    public boolean exists(OpenJPAStateManager sm, Object context) {
        return super.exists(sm, context);
    }

    @Override
    public boolean load(OpenJPAStateManager sm, BitSet fields,
            FetchConfiguration fetch, int lockLevel, Object context) {

        return super.load(sm, fields, fetch, lockLevel, context);
    }
    
    @Override
    public Collection flush(Collection sms) {
        if (_space == null)
            getConnection();
        
        List<OpenJPAStateManager> unhandledStates = new ArrayList<OpenJPAStateManager>();                
        HashSet<Object> instancesInRelation = new HashSet<Object>();
        
        for (Iterator itr = sms.iterator(); itr.hasNext();) {
            OpenJPAStateManager sm = (OpenJPAStateManager)itr.next();
            if (sm.getPCState() == PCState.PNEW) {                
                Object o = sm.getManagedInstance();
                try {
                    // Check if one of the fields is a relation instance and if so save the instance
                    for (FieldMetaData fieldMetaData : sm.getMetaData().getFields()) {
                        FieldMapping fm = (FieldMapping)fieldMetaData;
                        if (fieldMetaData.getBackingMember() instanceof AnnotatedElement) {                            
                            AnnotatedElement ae = (AnnotatedElement)fieldMetaData.getBackingMember();                            
                            // Handle @OneToOne & @OneToMany annotations (its not necessary to take care
                            // of @Embedded since these instances don't have their own state manager)
                            if (ae.isAnnotationPresent(OneToOne.class)) {                                
                                Object instanceInRelation = sm.fetch(fm.getIndex());
                                if (instanceInRelation != null) {
                                    instancesInRelation.add(instanceInRelation);
                                }                                
                            } else if (ae.isAnnotationPresent(OneToMany.class)) {
                                Collection<?> oneToManyInstances = (Collection<?>)sm.fetch(fm.getIndex());
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
                        _space.write(sm.getManagedInstance(), null, 0);
                    }
                                        
                } catch (Exception e) {
                }
                
                
            } else {
                unhandledStates.add(sm);
            }
        }
                               
        return super.flush(unhandledStates);
    }

    
    
}