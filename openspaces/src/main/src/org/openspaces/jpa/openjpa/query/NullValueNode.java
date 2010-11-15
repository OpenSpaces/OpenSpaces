package org.openspaces.jpa.openjpa.query;

import org.apache.openjpa.kernel.exps.ExpressionVisitor;
import org.apache.openjpa.kernel.exps.Path;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * Represents a NULL value node in the translated query expression tree.
 * 
 * @author idan
 * @since 8.0
 * 
 */
public class NullValueNode implements Value, ExpressionNode {
    //    
    private static final long serialVersionUID = 1L;
    
    @Override
    public String toString() {
        return "NULL";
    }

    public void acceptVisit(ExpressionVisitor visitor) {
    }

    public String getAlias() {
        return null;
    }

    public ClassMetaData getMetaData() {
        return null;
    }

    public String getName() {
        return null;
    }

    public Path getPath() {
        return null;
    }

    public Value getSelectAs() {
        return null;
    }

    @SuppressWarnings("rawtypes")
    public Class getType() {
        return null;
    }

    public boolean isAggregate() {
        return false;
    }

    public boolean isVariable() {
        return false;
    }

    public boolean isXPath() {
        return false;
    }

    public void setAlias(String alias) {        
    }

    @SuppressWarnings("rawtypes")
    public void setImplicitType(Class cls) {
    }

    public void setMetaData(ClassMetaData cmd) {
    }

    public void appendSql(StringBuilder sql) {
        sql.append("NULL");
    }

    public NodeType getNodeType() {
        return NodeType.NULL_VALUE;
    }


}
