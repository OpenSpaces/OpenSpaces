package org.openspaces.jpa.openjpa.query;

import org.apache.openjpa.kernel.exps.Expression;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;

/**
 * Represents an empty expression where there's no WHERE clause.
 * 
 * @author idan
 * @since 8.0
 * 
 */
public class EmptyExpression implements Expression, ExpressionNode {
    //
    private static final long serialVersionUID = 1L;

    public void acceptVisit(ExpressionVisitor visitor) {
    }

    public void appendSql(StringBuilder sql) {        
    }

    public NodeType getNodeType() {
        return NodeType.EMPTY_EXPRESSION;
    }    

}
