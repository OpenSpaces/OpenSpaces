package org.openspaces.jpa.openjpa.query;

import org.apache.openjpa.kernel.exps.Expression;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;
import org.apache.openjpa.kernel.exps.Value;

/**
 * Represents a JPQL contains (MEMBER OF) expression.
 *  
 * @author idan
 * @since 8.0
 *
 */
public class ContainsExpression implements Expression, ExpressionNode {
    //
    private static final long serialVersionUID = 1L;
    private ExpressionNode _fieldPath = null;
    private ExpressionNode _value = null;
    
    public ContainsExpression(Value fieldPath, Value value) {
        this._fieldPath = (ExpressionNode) fieldPath;
        this._value = (ExpressionNode) value;
    }
    
    public void appendSql(StringBuilder sql) {
        _fieldPath.appendSql(sql);
        sql.append("[*] = ");
        _value.appendSql(sql);
    }

    public NodeType getNodeType() {
        return NodeType.CONTAINS_EXPRESSION;
    }

    public void acceptVisit(ExpressionVisitor visitor) {
    }

}
