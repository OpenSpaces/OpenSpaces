package org.openspaces.jpa.openjpa.query;

import org.apache.openjpa.kernel.exps.ExpressionVisitor;
import org.apache.openjpa.kernel.exps.Path;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * Represents a variable (Pojo) in the translated query expression tree.
 * Initiated by the FROM clause parser.
 * 
 * @author idan
 * @since 8.0
 * 
 */
public class Variable implements Value, ExpressionNode {
    //
    private static final long serialVersionUID = 1L;
    private String _name;
    private String _alias;
    private Class<?> _type;
    private ClassMetaData _classMetaData;
    
    public Variable(String name, Class<?> type) {
        _name = name;
        _type = type;
        _classMetaData = null;
    }
    
    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        visitor.exit(this);
    }

    public String getAlias() {
        return _alias;
    }

    public ClassMetaData getMetaData() {
        return _classMetaData;
    }

    public String getName() {
        return _name;
    }

    public Path getPath() {
        return null;
    }

    public Value getSelectAs() {
        return null;
    }

    @SuppressWarnings("rawtypes")
    public Class getType() {
        return _type;
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
        _alias = alias;
    }

    @SuppressWarnings("rawtypes")
    public void setImplicitType(Class type) {
    }

    public void setMetaData(ClassMetaData classMetaData) {
        _classMetaData = classMetaData;
    }

    public void appendSql(StringBuilder sql) {
        sql.append(_classMetaData.getDescribedType().getName());
        
    }

    public NodeType getNodeType() {
        return NodeType.VARIABLE;
    }

}
