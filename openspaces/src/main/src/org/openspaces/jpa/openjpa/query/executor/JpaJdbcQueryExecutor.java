package org.openspaces.jpa.openjpa.query.executor;

import java.sql.PreparedStatement;

import org.apache.openjpa.kernel.exps.QueryExpressions;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.meta.ClassMetaData;
import org.openspaces.jpa.StoreManager;
import org.openspaces.jpa.openjpa.query.ExpressionNode;
import org.openspaces.jpa.openjpa.query.ExpressionNode.NodeType;
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
public class JpaJdbcQueryExecutor extends AbstractJpaQueryExecutor {

    public JpaJdbcQueryExecutor(QueryExpressions expression, ClassMetaData cm, Object[] parameters) {
        super(expression, cm, parameters);
    }

    @Override
    public ResultObjectProvider execute(StoreManager store) throws Exception {
        GConnection conn = store.getConfiguration().getJdbcConnection();
        conn.setTransaction(store.getCurrentTransaction());
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
        appendGroupBySql();
        appendOrderBySql();
    }
    
    @Override
    protected void appendWhereSql() {
        ExpressionNode node = (ExpressionNode) _expression.filter;
        if (node.getNodeType() != NodeType.EMPTY_EXPRESSION) {
            _sql.append("WHERE ");
            super.appendWhereSql();
        }
    }    
    
    /**
     * Append SELECT FROM to SQL string builder.
     */
    protected void appendSelectFromSql() {
        _sql.append("SELECT ");
        for (int i = 0; i < _expression.projections.length; i++) {
            ExpressionNode node = (ExpressionNode) _expression.projections[i];
            node.appendSql(_sql);
            if (i + 1 == _expression.projections.length)
                _sql.append(" ");
            else
                _sql.append(", ");
        }
        _sql.append("FROM ");
        _sql.append(_classMetaData.getDescribedType().getName());
        _sql.append(" ");
    }

}
