package org.openspaces.jpa.openjpa;


import org.apache.openjpa.kernel.ExpressionStoreQuery;
import org.apache.openjpa.kernel.StoreQuery;
import org.apache.openjpa.kernel.exps.ExpressionFactory;
import org.apache.openjpa.kernel.exps.ExpressionParser;
import org.apache.openjpa.kernel.exps.QueryExpressions;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.meta.ClassMetaData;
import org.openspaces.jpa.openjpa.query.ExpressionNode;
import org.openspaces.jpa.openjpa.query.QueryExpressionFactory;

import com.gigaspaces.internal.metadata.converter.Pojo2ExternalEntryConverter;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.ExternalEntry;
import com.j_spaces.core.client.SQLQuery;

/**
 * Executes select, update & delete SQL operations.
 * The provided OpenJPA expression tree is translated to either GigaSpaces' SQLQuery or JDBC.
 * 
 * @author idan
 * @since 8.0
 *
 */
public class SpaceStoreManagerQuery extends ExpressionStoreQuery {

    private static final long serialVersionUID = 1L;

    private GSStoreManager _store;
    
    public SpaceStoreManagerQuery(ExpressionParser parser, GSStoreManager store) {
        super(parser);
        _store = store;
    }    
    
    @Override
    public boolean supportsDataStoreExecution() {
        return true;
    }
    
    /**
     * Execute the given expression against the given candidate extent.
     *
     * @param ex current executor
     * @param base the base type the query should match
     * @param types the independent candidate types
     * @param subclasses true if subclasses should be included in the results
     * @param facts the expression factory used to build the query for
     * each base type
     * @param parsed the parsed query values
     * @param params parameter values, or empty array
     * @param range result range
     * @return a provider for matching objects
     */
    protected ResultObjectProvider executeQuery(Executor ex, ClassMetaData classMetaData,
            ClassMetaData[] types, boolean subClasses,  ExpressionFactory[] facts,
            QueryExpressions[] exps, Object[] params, Range range)
    {
        final ExpressionNode expression = (ExpressionNode) exps[0].filter;        
        final StringBuilder sql = new StringBuilder();
        expression.appendSql(sql);                        
        final SQLQuery<Object> sqlQuery = new SQLQuery<Object>(classMetaData.getDescribedType().getName(), sql.toString());
        // Set query parameters (if needed) - the parameters are ordered by index
        for (int i = 0; i < params.length; i++) {
            sqlQuery.setParameter(i + 1, params[i]);
        }
        try {
            final IJSpace space = _store.getConfiguration().getSpace();        
            final Object[] result = space.readMultiple(sqlQuery, null, Integer.MAX_VALUE);            
            final ExternalEntry[] eeResult = new ExternalEntry[result.length];
            Pojo2ExternalEntryConverter conv = new Pojo2ExternalEntryConverter();        
            for (int i = 0; i < result.length; i++) {
                eeResult[i] = (ExternalEntry) conv.pojoToEntry(result[i]);
            }
            return new SpaceResultObjectProvider(classMetaData, eeResult, _store);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }        
    }

    /**
     * Execute the given expression against the given candidate extent
     * and delete the instances.
     *
     * @param ex current executor
     * @param base the base type the query should match
     * @param types the independent candidate types
     * @param subclasses true if subclasses should be included in the results
     * @param facts the expression factory used to build the query for
     * each base type
     * @param parsed the parsed query values
     * @param params parameter values, or empty array
     * @return a number indicating the number of instances deleted,
     * or null to execute the delete in memory
     */
    protected Number executeDelete(Executor ex, ClassMetaData base,
        ClassMetaData[] types, boolean subclasses, ExpressionFactory[] facts,
        QueryExpressions[] parsed, Object[] params) {
        return null;
    }

    /**
     * Execute the given expression against the given candidate extent
     * and updates the instances.
     *
     * @param ex current executor
     * @param base the base type the query should match
     * @param types the independent candidate types
     * @param subclasses true if subclasses should be included in the results
     * @param facts the expression factory used to build the query for
     * each base type
     * @param parsed the parsed query values
     * @param params parameter values, or empty array
     * @return a number indicating the number of instances updated,
     * or null to execute the update in memory.
     */
    protected Number executeUpdate(Executor ex, ClassMetaData base,
        ClassMetaData[] types, boolean subclasses, ExpressionFactory[] facts,
        QueryExpressions[] parsed, Object[] params) {
        return null;
    }

    /**
     * Return the commands that will be sent to the datastore in order
     * to execute the query, typically in the database's native language.
     *
     * @param base the base type the query should match
     * @param types the independent candidate types
     * @param subclasses true if subclasses should be included in the results
     * @param facts the expression factory used to build the query for
     * each base type
     * @param parsed the parsed query values
     * @param params parameter values, or empty array
     * @param range result range
     * @return a textual description of the query to execute
     */
    protected String[] getDataStoreActions(ClassMetaData base,
        ClassMetaData[] types, boolean subclasses, ExpressionFactory[] facts,
        QueryExpressions[] parsed, Object[] params, Range range) {
        return StoreQuery.EMPTY_STRINGS;
    }

    /**
     * Return the assignable types for the given metadata whose expression
     * trees must be compiled independently.
     */
    protected ClassMetaData[] getIndependentExpressionCandidates
        (ClassMetaData type, boolean subclasses) {
        return new ClassMetaData[]{ type };
    }

    /**
     * Return an {@link ExpressionFactory} to use to create an expression to
     * be executed against an extent. Each factory will be used to compile
     * one filter only. The factory must be cachable.
     */
    protected ExpressionFactory getExpressionFactory(ClassMetaData type) {
        return new QueryExpressionFactory();
    }
    
    
    
}
