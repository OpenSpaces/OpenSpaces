package org.openspaces.jpa.openjpa.query;

import org.apache.openjpa.kernel.exps.ExpressionVisitor;
import org.apache.openjpa.kernel.exps.Parameter;
import org.apache.openjpa.kernel.exps.Path;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * Represents a parameter node in the translated query expression tree.
 * 
 * @author idan
 * @since 8.0
 *
 */
public class ParameterNode implements Parameter, ExpressionNode {
    //
    private static final long serialVersionUID = 1L;
    private int _index;
    private Class<?> _type;
    private ClassMetaData _classMetaData;
    
    @SuppressWarnings("rawtypes")
    public ParameterNode(Class type) {
        _type = type;
        _index = -1;
    }
    
    public void acceptVisit(ExpressionVisitor visitor) {
    }

    public String getAlias() {
        return null;
    }

    public ClassMetaData getMetaData() {
        return _classMetaData;
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
    }

    @SuppressWarnings("rawtypes")
    public void setImplicitType(Class class1) {
    }

    public void setMetaData(ClassMetaData cm) {
        _classMetaData = cm;
    }

    public Object getValue(Object[] aobj) {
        return null;
    }

    public Object getParameterKey() {
        return null;
    }

    public void setIndex(int index) {
        _index = index;
    }
    
    public int getIndex() {
        return _index;
    }

    public void appendSql(StringBuilder sql) {
        // We convert JPQL named parameters to "?" parameters which SQLQuery/JDBC supports.
        sql.append("?");
    }

    public NodeType getNodeType() {
        return NodeType.PARAMETER;
    }

}
