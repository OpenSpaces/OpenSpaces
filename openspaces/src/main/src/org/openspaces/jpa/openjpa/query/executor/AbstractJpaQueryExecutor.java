package org.openspaces.jpa.openjpa.query.executor;

import org.apache.openjpa.kernel.exps.QueryExpressions;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.meta.ClassMetaData;
import org.openspaces.jpa.StoreManager;
import org.openspaces.jpa.openjpa.query.ExpressionNode;

/**
 * A base implementation for JpaQueryExecutor interface.
 * 
 * @author idan
 * @since 8.0
 *
 */
abstract class AbstractJpaQueryExecutor implements JpaQueryExecutor {
    //
    protected QueryExpressions _expression;
    protected ClassMetaData _classMetaData;
    protected Object[] _parameters;
    protected StringBuilder _sql;
    
    protected AbstractJpaQueryExecutor(QueryExpressions expression, ClassMetaData cm, Object[] parameters) {
        _expression = expression;
        _classMetaData = cm;
        _parameters = parameters;
        build();
    }
    
    /**
     * Execute query.
     * @throws Exception 
     */
    public abstract ResultObjectProvider execute(StoreManager store) throws Exception;
    
    /**
     * Build query for execution.
     */
    protected void build() {
        _sql = new StringBuilder();
        appendWhereSql();
        appendGroupBySql();
        appendOrderBySql();        
    }
    
    /**
     * Append WHERE clause to the SQL string builder.
     */
    protected void appendWhereSql() {
        ((ExpressionNode) _expression.filter).appendSql(_sql);        
    }
    
    /**
     * Append ORDER BY to the SQL string builder.
     */
    protected void appendOrderBySql() {
        if (_expression.ordering.length > 0) {
            _sql.append(" ORDER BY ");
            for (int i = 0; i < _expression.ordering.length;) {
                _sql.append(_expression.ordering[i].getName());
                _sql.append(_expression.ascending[i] ? " asc" : " desc");
                if (++i != _expression.ordering.length)
                    _sql.append(", ");                
            }
        }                
    }

    /**
     * Append GROUP BY to the SQL string builder.
     */
    protected void appendGroupBySql() {
        if (_expression.grouping.length > 0) {
            _sql.append(" GROUP BY ");
            for (int i = 0; i < _expression.grouping.length;) {
                _sql.append(_expression.grouping[i].getName());
                if (++i != _expression.grouping.length)
                    _sql.append(", ");                
            }            
        }
    }    
    
    /**
     * Gets the executor's generated SQL buffer.
     */
    public StringBuilder getSqlBuffer() {
        return _sql;
    }

    

}
