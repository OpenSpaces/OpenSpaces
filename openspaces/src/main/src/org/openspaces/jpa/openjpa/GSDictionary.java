package org.openspaces.jpa.openjpa;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.schema.Column;
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

    public GSDictionary() {
        super();
        maxTableNameLength = 256;
        joinSyntax = SYNTAX_TRADITIONAL;
        schemaCase = SCHEMA_CASE_PRESERVE;
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
}
