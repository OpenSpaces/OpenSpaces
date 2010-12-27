package org.openspaces.jpa.openjpa.query;

import org.apache.openjpa.kernel.exps.Expression;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;

/**
 * Represents a JPQL bound variable expression node.
 * OpenJPA adds this dummy node containing the mapping info when a property is JOINED.
 * The node is translated to a "1 = 1" expression.
 * 
 * @author Idan Moyal
 * @since 8.0
 *
 */
public class BindVariableExpression implements Expression, ExpressionNode {
    //
    private static final long serialVersionUID = 1L;
    
    public BindVariableExpression() {
    }
    
    public void appendSql(StringBuilder sql) {
        sql.append("1 = 1");            
    }

    public NodeType getNodeType() {
        return NodeType.VARIABLE_BINDING;
    }

    public void acceptVisit(ExpressionVisitor arg0) {
    }

}
