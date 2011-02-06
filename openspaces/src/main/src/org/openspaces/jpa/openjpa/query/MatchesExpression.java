package org.openspaces.jpa.openjpa.query;

import org.apache.openjpa.kernel.exps.Expression;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;
import org.apache.openjpa.kernel.exps.Value;

/**
 * Represents a LIKE expression in a JPQL query.
 * 
 * @author Idan Moyal
 * @since 8.0.1
 *
 */
public class MatchesExpression implements Expression, ExpressionNode {

    private static final long serialVersionUID = 1L;
    protected ExpressionNode _candidate;
    protected ExpressionNode _regularExpression;

    MatchesExpression(Value candidate, Value regularExpression) {
        this._candidate = (ExpressionNode) candidate;
        this._regularExpression = (ExpressionNode) regularExpression;
    }

    public void acceptVisit(ExpressionVisitor visitor) {
    }

    public void appendSql(StringBuilder sql) {
        _candidate.appendSql(sql);
        sql.append(" LIKE ");
        _regularExpression.appendSql(sql);
    }

    public NodeType getNodeType() {
        return NodeType.LIKE_EXPRESSION;
    }

}
