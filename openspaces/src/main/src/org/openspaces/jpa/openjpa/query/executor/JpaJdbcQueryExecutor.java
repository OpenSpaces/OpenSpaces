package org.openspaces.jpa.openjpa.query.executor;

import java.sql.PreparedStatement;

import org.apache.openjpa.kernel.exps.QueryExpressions;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.meta.ClassMetaData;
import org.openspaces.jpa.openjpa.GSStoreManager;
import org.openspaces.jpa.openjpa.query.AggregationFunction;
import org.openspaces.jpa.openjpa.query.SpaceProjectionResultObjectProvider;

import com.j_spaces.jdbc.driver.GConnection;
import com.j_spaces.jdbc.driver.GResultSet;

/**
 * Executes JPA's translated expression tree as a JDBC query.
 * 
 * @author idan
 * @since 8.0
 * 
 */
class JpaJdbcQueryExecutor extends AbstractJpaQueryExecutor {

    public JpaJdbcQueryExecutor(QueryExpressions expression, ClassMetaData cm, Object[] parameters) {
        super(expression, cm, parameters);
    }

    @Override
    public ResultObjectProvider execute(GSStoreManager store) throws Exception {
        GConnection conn = store.getConfiguration().getJdbcConnection();
        // TODO: attach the current transaction to the JDBC connection.
        //conn.setTransaction(store.getCurrentTransaction());
        PreparedStatement pstmt = conn.prepareStatement(_sql.toString());
        for (int i = 0; i < _parameters.length; i++) {
            pstmt.setObject(i+1, _parameters[i]);
        }
        GResultSet rs = (GResultSet) pstmt.executeQuery();
        return new SpaceProjectionResultObjectProvider(rs.getResult().getFieldValues());
    }

    @Override
    protected void build() {
        _sql = new StringBuilder();
        appendSelectFromSql();
        appendWhereSql();
        appendOrderBySql();
    }

    /**
     * Append SELECT FROM to SQL string builder.
     */
    protected void appendSelectFromSql() {
        _sql.append("SELECT ");
        if (_expression.projections.length > 1)
            throw new IllegalArgumentException("Multiple aggregation projection is not supported.");
        for (int i = 0; i < _expression.projections.length; i++) {
            AggregationFunction aggregation = (AggregationFunction) _expression.projections[i];
            _sql.append(aggregation.getName());
            _sql.append("(");
            String path = aggregation.getPath().getName();
            if (path.length() == 0)
                path = "*";
            _sql.append(path);
            if (i + 1 == _expression.projections.length)
                _sql.append(") ");
            else
                _sql.append("), ");
        }
        _sql.append("FROM ");
        _sql.append(_classMetaData.getDescribedType().getName());
        _sql.append(" ");
    }

}
