package org.openspaces.jpa.openjpa.query;

import org.apache.openjpa.kernel.exps.ExpressionVisitor;
import org.apache.openjpa.kernel.exps.Path;
import org.apache.openjpa.kernel.exps.QueryExpressions;
import org.apache.openjpa.kernel.exps.Subquery;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * Represents an inner query (subquery) in OpenJPA's expression tree.
 * 
 * @author idan
 * @since 8.0
 * 
 */
public class InnerQuery implements Subquery, ExpressionNode {
    //
    private static final long serialVersionUID = 1L;
    private ClassMetaData _candidate;
    private QueryExpressions _expressions;
    private Class<?> _type;
    private String _queryAlias;

    public InnerQuery(ClassMetaData classMetaData) {
        _candidate = classMetaData;
    }

    /**
     * Return the candidate alias for this subquery.
     */
    public String getCandidateAlias() {
        return "";
    }

    /**
     * Set the candidate alias for this subquery.
     */
    public void setSubqAlias(String subqAlias) {
        _queryAlias = subqAlias;
    }

    /**
     * Return the subqAlias
     */
    public String getSubqAlias() {
        return _queryAlias;
    }

    /**
     * Set the parsed subquery.
     */
    public void setQueryExpressions(QueryExpressions query) {
        _expressions = query;
    }

    public Object getSelect() {
        return null;
    }

    /**
     * Return the expected type for this value, or <code>Object</code> if the type is unknown.
     */
    @SuppressWarnings("rawtypes")
    public Class getType() {
        if (_expressions != null && _type == null) {
            if (_expressions.projections.length == 0)
                return _candidate.getDescribedType();
            if (_expressions.projections.length == 1)
                return _expressions.projections[0].getType();
        }
        return _type;
    }

    /**
     * Set the implicit type of the value, based on how it is used in the filter. This method is
     * only called on values who return <code>Object</code> from {@link #getType}.
     */
    @SuppressWarnings("rawtypes")
    public void setImplicitType(Class type) {
        if (_expressions != null && _expressions.projections.length == 1)
            _expressions.projections[0].setImplicitType(type);
        _type = type;
    }

    /**
     * Return true if this value is a variable.
     */
    public boolean isVariable() {
        return false;
    }

    /**
     * Return true if this value is an aggregate.
     */
    public boolean isAggregate() {
        return false;
    }

    /**
     * Return true if this value is an XML Path.
     */
    public boolean isXPath() {
        return false;
    }

    /**
     * Return any associated persistent type.
     */
    public ClassMetaData getMetaData() {
        return _candidate;
    }

    /**
     * Associate a persistent type with this value.
     */
    public void setMetaData(ClassMetaData meta) {
        _candidate = meta;
    }

    /**
     * Accept a visit from a tree visitor.
     */
    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _expressions.filter.acceptVisit(visitor);
        visitor.exit(this);
    }

    /**
     * Return select item alias
     */
    public String getAlias() {
        return "";
    }

    /**
     * Set select item alias
     */
    public void setAlias(String alias) {
    }

    /**
     * Return 'this' concrete class if alias is set, otherwise null
     */
    public Value getSelectAs() {
        return null;
    }

    public Path getPath() {
        return null;
    }

    public String getName() {
        return "";
    }

    public void appendSql(StringBuilder sql) {
        sql.append("(");
        appendSelectFromSql(sql);
        ((ExpressionNode) _expressions.filter).appendSql(sql);
        sql.append(")");
    }

    /**
     * Append SELECT FROM to SQL string builder.
     */
    protected void appendSelectFromSql(StringBuilder sql) {
        sql.append("SELECT ");
        if (_expressions.projections.length > 1)
            throw new IllegalArgumentException("Multiple aggregation projection is not supported.");
        for (int i = 0; i < _expressions.projections.length; i++) {
            AggregationFunction aggregation = (AggregationFunction) _expressions.projections[i];
            sql.append(aggregation.getName());
            sql.append("(");
            String path = aggregation.getPath().getName();
            if (path.length() == 0)
                path = "*";
            sql.append(path);
            if (i + 1 == _expressions.projections.length)
                sql.append(") ");
            else
                sql.append("), ");
        }
        sql.append("FROM ");
        sql.append(_candidate.getDescribedType().getName());
        sql.append(" ");
    }    
    
    public NodeType getNodeType() {
        return NodeType.INNER_QUERY;
    }

}
