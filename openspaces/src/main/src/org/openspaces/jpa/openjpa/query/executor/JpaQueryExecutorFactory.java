package org.openspaces.jpa.openjpa.query.executor;

import org.apache.openjpa.kernel.exps.QueryExpressions;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * A factory for creating a JpaQueryExecutor instance.
 * 
 * @author idan
 * @since 8.0
 *
 */
public class JpaQueryExecutorFactory {

    /**
     * Returns a new JpaQueryExecutor implementation instance based on the provided parameters.
     * If the provided expression tree requires aggregation - a JDBC executor is returned.
     * Otherwise an SQLQuery executor is returned.
     * @param expressions The expression tree.
     * @param cm The queried class meta data.
     * @param parameters The user set parameters.
     * @param configuration The space configuration which holds connection resources.
     * @return A JpaQueryExecutor implementation instance.
     */
    public static JpaQueryExecutor newExecutor(QueryExpressions expression, ClassMetaData cm, Object[] parameters) {
        if (expression.projections.length > 0)
            return new JpaJdbcQueryExecutor(expression, cm, parameters);
        return new JpaSqlQueryExecutor(expression, cm, parameters);
    }
    
}
