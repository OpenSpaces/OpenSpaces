package org.openspaces.jpa.openjpa.query;

/**
 * Represents a node in the translated query expression tree.
 * 
 * @author idan
 * @since 8.0
 * 
 */
public interface ExpressionNode {
    /**
     * Represents the node type in the translated query expression tree.
     */
    public enum NodeType { BINARY_EXPRESSION, LOGICAL_EXPRESSION, NULL_VALUE, PARAMETER, VARIABLE,
        FIELD_PATH, LITERAL_VALUE, EMPTY_EXPRESSION, INNER_QUERY, AGGREGATION_FUNCTION, CONTAINS_EXPRESSION, VARIABLE_BINDING, LIKE_EXPRESSION
    }

    /**
     * Appends the node SQL string to the string builder. 
     * @param sql The SQL string builder to append to.
     */
    void appendSql(StringBuilder sql);
    
    /**
     * Gets the node type
     * @return The node type.
     */
    NodeType getNodeType();
}
