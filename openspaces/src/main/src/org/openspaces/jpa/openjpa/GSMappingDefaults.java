package org.openspaces.jpa.openjpa;

import java.util.HashMap;
import java.util.Map;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.schema.Schema;
import org.apache.openjpa.persistence.jdbc.PersistenceMappingDefaults;

/**
 * Supplies GigaSpaces default mapping information in accordance with the JPA specification.
 *
 * @author idan
 * @since 8.0
 * 
 */
public class GSMappingDefaults extends PersistenceMappingDefaults {

    private static Map<String, Class<?>> _tableToClassMappingTable = new HashMap<String, Class<?>>();
    
    /**
     * Gets the actual class represented by the provided table name.
     */
    public static Class<?> getClassForTable(String tableName) {
        return _tableToClassMappingTable.get(tableName);
    }
    
    /**
     * The class is added to a table mapped by the JDBC table name for later processing
     * the class' GigaSpaces specific annotations such as routing and adding the relevant syntax
     * to the CREATE TABLE syntax in the dictionary.
     */
    @Override
    public String getTableName(ClassMapping cls, Schema schema) {
        String tableName = super.getTableName(cls, schema);     
        _tableToClassMappingTable.put(tableName, cls.getDescribedType());       
        return tableName;
    }
    
}
