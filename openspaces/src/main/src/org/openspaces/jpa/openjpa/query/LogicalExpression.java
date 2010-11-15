package org.openspaces.jpa.openjpa.query;

import org.apache.openjpa.kernel.exps.Expression;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;
import org.openspaces.jpa.openjpa.query.BinaryExpression.ExpressionType;

/**
 * Represents a logical expression (AND/OR) in the translated query expression tree.
 * 
 * @author idan
 * @since 8.0
 *
 */
public class LogicalExpression implements Expression, ExpressionNode {
    //
    private static final long serialVersionUID = 1L;
    private ExpressionNode _expression1;
    private ExpressionNode _expression2;
    private ExpressionType _expressionType;
            
    public LogicalExpression(Expression expression1, Expression expression2, ExpressionType expressionType) {
        _expression1 = (ExpressionNode) expression1;
        _expression2 = (ExpressionNode) expression2;
        _expressionType = expressionType;
    }

    public void appendSql(StringBuilder sql) {
        boolean paren = _expressionType == ExpressionType.OR;
        if (paren)
            sql.append("(");
        _expression1.appendSql(sql);
        sql.append(this.toString());
        _expression2.appendSql(sql);
        if (paren)
            sql.append(")");
    }    
    
    public void acceptVisit(ExpressionVisitor visitor) {
    }

    @Override
    public String toString() {
        switch (_expressionType) {
        case AND:
            return " AND ";
        case OR:
            return " OR ";
        }
        return "";
    }

    public NodeType getNodeType() {
        return NodeType.LOGICAL_EXPRESSION;
    }

    
}
