package org.openspaces.jpa.openjpa;

import java.lang.reflect.Field;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.PrimaryKey;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.sql.DBDictionary;

/**
 * Represents a GigaSpaces dictionary for OpenJPA.
 * 
 * @author idan
 * @since 8.0
 * 
 */
public class GSDictionary extends DBDictionary {

	protected boolean supportsRouting = true;
	
    public GSDictionary() {
        super();
        maxTableNameLength = 256;
        
        // Dynamic indexes are not supported
        maxIndexesPerTable = 0;
        
        joinSyntax = SYNTAX_TRADITIONAL;
        schemaCase = SCHEMA_CASE_PRESERVE;
        supportsComments = false;
        
        batchLimit = 100;
    }
    
    /**
     * Returns the table name as it is.
     */
    @Override
    public String getFullName(Table table, boolean logical) {    	
        if (table.toString().charAt(0) == '\"' &&
                table.toString().charAt(table.toString().length() - 1) == '\"') {                       
            table.setIdentifier(DBIdentifier.newTable(table.toString().substring(1, table.toString().length() - 1)));
        }
        return table.toString();
    }

    /**
     * Returns column declaration (w/o NOT NULL phrase).
     */
    @Override
    protected String getDeclareColumnSQL(Column col, boolean alter) {
        col.setNotNull(false);
        return super.getDeclareColumnSQL(col, alter);
    }
        
    /**
     * Return a series of SQL statements to create the given table, complete
     * with columns. Indexes and constraints will be created separately.
     */
    @Override
    public String[] getCreateTableSQL(Table table) {
        StringBuilder buf = new StringBuilder();
        String tableName = getFullName(table, false);                 
        buf.append("CREATE TABLE ").append(tableName);
        if (supportsComments && table.hasComment()) {
            buf.append(" ");
            comment(buf, table.getComment());
            buf.append("\n    (");
        } else {
            buf.append(" (");
        }

        // do this before getting the columns so we know how to handle
        // the last comma
        StringBuilder endBuf = new StringBuilder();
        PrimaryKey pk = table.getPrimaryKey();
        String pkStr;
        if (pk != null) {
            pkStr = getPrimaryKeyConstraintSQL(pk);
            if (pkStr != null)
                endBuf.append(pkStr);
        }

        // Add routing (partition by) syntax if necessary
        if (supportsRouting) {
        	String routingColumn = getRoutingColumn(table);
        	if (routingColumn != null) {
        		if (endBuf.length() > 0)
        			endBuf.append(", ");
        		endBuf.append("PARTITION BY (");
        		endBuf.append(routingColumn);
        		endBuf.append(")");
        	}
        }
        
        Column[] cols = table.getColumns();
        for (int i = 0; i < cols.length; i++) {
            buf.append(getDeclareColumnSQL(cols[i], false));
            if (i < cols.length - 1 || endBuf.length() > 0)
                buf.append(", ");
            if (supportsComments && cols[i].hasComment()) {
                comment(buf, cols[i].getComment());
                buf.append("\n    ");
            }
        }

        buf.append(endBuf.toString());
        buf.append(")");
        return new String[]{ buf.toString() };    	    	
    }

    /**
     * Trims quotes off the provided String.
     */
    private String trimQuotes(String str) {
    	if (str.startsWith("\"") && str.endsWith("\""))
    		return str.substring(1, str.length() - 1);
    	return str;
    }
    
    /**
     * Gets the column name which has a @SpaceRouting annotation otherwise returns null.
     */
    private String getRoutingColumn(Table table) {
        // Skip OpenJPA's system tables
        if (table.toString().startsWith("OPENJPA_"))
            return null;
		Class<?> cls = GSMappingDefaults.getClassForTable(table.toString());		
		for (Column col : table.getColumns()) {
			try {
				String colName = trimQuotes(col.toString());				
				Field field = cls.getDeclaredField(colName);
				if (field.getAnnotation(PartitionIndicator.class) != null) {
					return field.getName();
				}
			} catch (NoSuchFieldException e) {				
			}									
		}				
		return null;
	}
        
}
