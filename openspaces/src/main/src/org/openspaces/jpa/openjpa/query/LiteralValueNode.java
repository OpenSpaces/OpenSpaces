package org.openspaces.jpa.openjpa.query;

import org.apache.openjpa.kernel.exps.ExpressionVisitor;
import org.apache.openjpa.kernel.exps.Literal;
import org.apache.openjpa.kernel.exps.Path;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * Represents a literal value node in the transalated query expression tree.
 * 
 * @author idan
 * @since 8.0
 * 
 */
public class LiteralValueNode implements Literal, ExpressionNode {
    //
    private static final long serialVersionUID = 1L;
    private int _ptype;
    private Object _value;

    public LiteralValueNode(Object value, int ptype) {
        _value = value;
        _ptype = ptype;
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        visitor.exit(this);
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
        return _value.getClass();
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
    public void setImplicitType(Class type) {        
    }

    public void setMetaData(ClassMetaData cm) {
    }

    public Object getValue(Object[] params) {
        return getValue();
    }

    public int getParseType() {
        return _ptype;
    }

    public Object getValue() {
        return _value;
    }

    public void setValue(Object value) {
        _value = value;
    }
    
    @Override
    public String toString() {
        if (_ptype == Literal.TYPE_STRING || _ptype == Literal.TYPE_SQ_STRING)
            return String.format("'%s'", _value.toString());
        return _value.toString();
    }

    public void appendSql(StringBuilder sql) {
        sql.append(toString());        
    }

    public NodeType getNodeType() {
        return NodeType.LITERAL_VALUE;
    }

}
