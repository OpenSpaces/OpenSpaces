package org.openspaces.jpa.openjpa;


import java.util.ArrayList;
import java.util.Map;

import net.jini.core.lease.Lease;

import org.apache.openjpa.kernel.ExpressionStoreQuery;
import org.apache.openjpa.kernel.StoreQuery;
import org.apache.openjpa.kernel.exps.ExpressionFactory;
import org.apache.openjpa.kernel.exps.ExpressionParser;
import org.apache.openjpa.kernel.exps.Path;
import org.apache.openjpa.kernel.exps.QueryExpressions;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.openspaces.jpa.StoreManager;
import org.openspaces.jpa.openjpa.query.ExpressionNode;
import org.openspaces.jpa.openjpa.query.LiteralValueNode;
import org.openspaces.jpa.openjpa.query.ParameterNode;
import org.openspaces.jpa.openjpa.query.QueryExpressionFactory;
import org.openspaces.jpa.openjpa.query.ExpressionNode.NodeType;
import org.openspaces.jpa.openjpa.query.executor.JpaQueryExecutor;
import org.openspaces.jpa.openjpa.query.executor.JpaQueryExecutorFactory;

import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
import com.gigaspaces.internal.client.spaceproxy.metadata.ObjectType;
import com.gigaspaces.internal.transport.IEntryPacket;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.ReadModifiers;
import com.j_spaces.core.client.SQLQuery;
import com.j_spaces.core.client.UpdateModifiers;

/**
 * Executes select, update & delete SQL operations.
 * The provided OpenJPA expression tree is translated to either GigaSpaces' SQLQuery or JDBC.
 * 
 * @author idan
 * @since 8.0
 *
 */
public class StoreManagerQuery extends ExpressionStoreQuery {

    private static final long serialVersionUID = 1L;

    private StoreManager _store;
    
    public StoreManagerQuery(ExpressionParser parser, StoreManager store) {
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
     * @param factories the expression factory used to build the query for
     * each base type
     * @param parsed the parsed query values
     * @param parameters parameter values, or empty array
     * @param range result range
     * @return a provider for matching objects
     */
    protected ResultObjectProvider executeQuery(Executor ex, ClassMetaData classMetaData,
            ClassMetaData[] types, boolean subClasses,  ExpressionFactory[] factories,
            QueryExpressions[] expressions, Object[] parameters, Range range)
    {
        final JpaQueryExecutor executor = JpaQueryExecutorFactory.newExecutor(expressions[0], classMetaData, parameters); 
        try {
            return executor.execute(_store);
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
    protected Number executeDelete(Executor ex, ClassMetaData classMetaData, ClassMetaData[] types, boolean subClasses,
            ExpressionFactory[] facts, QueryExpressions[] exps, Object[] params)
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
            final Object[] result = space.takeMultiple(sqlQuery, _store.getCurrentTransaction(), Integer.MAX_VALUE);            
            return result.length;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }        
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
    protected Number executeUpdate(Executor ex, ClassMetaData classMetaData, ClassMetaData[] types, boolean subClasses,
            ExpressionFactory[] facts, QueryExpressions[] expressions, Object[] params)
    {
        final ExpressionNode expression = (ExpressionNode) expressions[0].filter;        
        final StringBuilder sql = new StringBuilder();
        expression.appendSql(sql);                        
        final SQLQuery<Object> sqlQuery = new SQLQuery<Object>(classMetaData.getDescribedType().getName(), sql.toString());
        // Set query parameters (if needed) - the parameters are ordered by index
        ArrayList<UpdateValue> updates = new ArrayList<UpdateValue>();
        // Keeps the first parameter index related to the WHERE clause.
        // The parameter before that index are related to the update set values.
        int firstWhereParameterIndex = 0;
        // Create a list of updated values
        for (Map.Entry<Path, Value> entry : expressions[0].updates.entrySet()) {
            FieldMetaData fmd = entry.getKey().last();
            if (((ExpressionNode) entry.getValue()).getNodeType() == NodeType.PARAMETER) {
                ParameterNode parameter = (ParameterNode) entry.getValue();
                updates.add(new UpdateValue(fmd.getIndex(), params[parameter.getIndex()]));
                firstWhereParameterIndex++;
            } else {
                LiteralValueNode literal = (LiteralValueNode) entry.getValue();
                updates.add(new UpdateValue(fmd.getIndex(), literal.getValue()));
            }                               
        }
        for (int i = firstWhereParameterIndex; i < params.length; i++) {
            sqlQuery.setParameter(i + 1, params[i]);
        }
        try {
            final ISpaceProxy proxy = (ISpaceProxy) _store.getConfiguration().getSpace();        
            final Object[] result = proxy.readMultiple(sqlQuery, _store.getCurrentTransaction(), Integer.MAX_VALUE,
                    ReadModifiers.EXCLUSIVE_READ_LOCK);
            if (result.length > 0) {
                final IEntryPacket[] entries = new IEntryPacket[result.length];
                for (int i = 0; i < result.length; i++) {
                    entries[i] = proxy.getDirectProxy().getTypeManager().getEntryPacketFromObject(result[i],
                            ObjectType.POJO, proxy);
                    // Update results with query update values
                    for (UpdateValue updateValue : updates) {
                        entries[i].setFieldValue(updateValue.getFieldIndex(), updateValue.getFieldValue());
                    }
                }
                proxy.writeMultiple(entries, _store.getCurrentTransaction(), Lease.FOREVER,
                        UpdateModifiers.UPDATE_ONLY);
            }
            return result.length;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }        
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
    
    
    
    /**
     * A structure to hold an update query's update value with its pojo field index.
     * @author idan
     * @since 8.0
     */
    private static class UpdateValue {
        private int _fieldIndex;
        private Object _fieldValue;
        
        public UpdateValue(int fieldIndex, Object fieldValue) {
            _fieldIndex = fieldIndex;
            _fieldValue = fieldValue;
        }
        
        /**
         * Gets the updated field index.
         */
        public int getFieldIndex() {
            return _fieldIndex;
        }
        
        /**
         * Gets the updated field value.
         */
        public Object getFieldValue() {
            return _fieldValue;
        }
        
    }
    
}
