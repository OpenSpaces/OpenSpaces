package org.openspaces.jpa.openjpa.query;

import org.apache.openjpa.kernel.exps.Value;

/**
 * Represents a NOT LIKE expression in a JPQL query.
 * 
 * @author Idan Moyal
 * @since 8.0.1
 *
 */
public class NotMatchesExpression extends MatchesExpression {

    private static final long serialVersionUID = 1L;

    NotMatchesExpression(Value candidate, Value regularExpression) {
        super(candidate, regularExpression);
    }

    public void appendSql(StringBuilder sql) {
        _candidate.appendSql(sql);
        sql.append(" NOT LIKE ");
        _regularExpression.appendSql(sql);
    }

}
