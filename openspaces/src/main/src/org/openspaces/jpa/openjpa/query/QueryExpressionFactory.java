package org.openspaces.jpa.openjpa.query;

import java.util.Date;

import org.apache.openjpa.kernel.exps.AggregateListener;
import org.apache.openjpa.kernel.exps.Arguments;
import org.apache.openjpa.kernel.exps.Expression;
import org.apache.openjpa.kernel.exps.ExpressionFactory;
import org.apache.openjpa.kernel.exps.FilterListener;
import org.apache.openjpa.kernel.exps.Literal;
import org.apache.openjpa.kernel.exps.Parameter;
import org.apache.openjpa.kernel.exps.Path;
import org.apache.openjpa.kernel.exps.Subquery;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.meta.ClassMetaData;
import org.openspaces.jpa.openjpa.query.BinaryExpression.ExpressionType;

/**
 * Defines the way the translated expression tree is built by OpenJPA.
 * 
 * @author idan
 * @since 8.0
 *
 */
public class QueryExpressionFactory implements ExpressionFactory {

    public Value abs(Value arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value add(Value arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value all(Value arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression and(Expression expression1, Expression expression2) {
        return new LogicalExpression(expression1, expression2, ExpressionType.AND);
    }

    public Value any(Value arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression asExpression(Value arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value avg(Value arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression bindKeyVariable(Value arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression bindValueVariable(Value arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression bindVariable(Value arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value cast(Value arg0, Class arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value coalesceExpression(Value[] arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value concat(Value arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression contains(Value arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression containsKey(Value arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression containsValue(Value arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value count(Value arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value distinct(Value arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value divide(Value arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression emptyExpression() {
        return new EmptyExpression();
    }

    public Expression endsWith(Value arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression equal(Value val1, Value val2) {
        return new BinaryExpression(val1, val2, ExpressionType.EQUAL);
    }

    public Value generalCaseExpression(Expression[] arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public <T extends Date> Value getCurrentDate(Class<T> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public <T extends Date> Value getCurrentTime(Class<T> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public <T extends Date> Value getCurrentTimestamp(Class<T> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value getKey(Value arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value getMapValue(Value arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value getNull() {
        return new NullValueNode();
    }

    public Value getObjectId(Value arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value getThis() {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression greaterThan(Value value1, Value value2) {
        return new BinaryExpression(value1, value2, ExpressionType.GREATER_THAN);
    }

    public Expression greaterThanEqual(Value value1, Value value2) {
        return new BinaryExpression(value1, value2, ExpressionType.GREATER_THAN_OR_EQUAL);
    }

    public Value index(Value arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value indexOf(Value arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression isEmpty(Value arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression isInstance(Value arg0, Class arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression isNotEmpty(Value arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression lessThan(Value value1, Value value2) {
        return new BinaryExpression(value1, value2, ExpressionType.LESS_THAN);
    }

    public Expression lessThanEqual(Value value1, Value value2) {
        return new BinaryExpression(value1, value2, ExpressionType.LESS_THAN_OR_EQUAL);
    }

    public Value mapEntry(Value arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value mapKey(Value arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression matches(Value arg0, Value arg1, String arg2, String arg3, String arg4) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value max(Value arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value min(Value arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value mod(Value arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value multiply(Value arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value newAggregate(AggregateListener arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Arguments newArgumentList(Value... arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Arguments newArgumentList(Value arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value newBoundVariable(String name, Class type) {
        return new Variable(name, type);
    }

    public Parameter newCollectionValuedParameter(Object arg0, Class arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value newExtension(FilterListener arg0, Value arg1, Value arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value newFunction(String arg0, Class<?> arg1, Value... arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    public Literal newLiteral(Object value, int ptype) {
        return new LiteralValueNode(value, ptype);
    }

    @SuppressWarnings("rawtypes")
    public Parameter newParameter(Object name, Class type) {
        return new ParameterNode(type);
    }

    public Path newPath() {
        return new FieldPathNode();
    }

    public Path newPath(Value arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Subquery newSubquery(ClassMetaData arg0, boolean arg1, String arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    public Literal newTypeLiteral(Object arg0, int arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value newUnboundVariable(String arg0, Class arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression not(Expression arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression notEqual(Value value1, Value value2) {
        return new BinaryExpression(value1, value2, ExpressionType.NOT_EQUAL);
    }

    public Expression notMatches(Value arg0, Value arg1, String arg2, String arg3, String arg4) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value nullIfExpression(Value arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression or(Expression expression1, Expression expression2) {
        return new LogicalExpression(expression1, expression2, ExpressionType.OR);
    }

    public Value simpleCaseExpression(Value arg0, Expression[] arg1, Value arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value size(Value arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value sqrt(Value arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression startsWith(Value arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value stringLength(Value arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value substring(Value arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value subtract(Value arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value sum(Value arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value toLowerCase(Value arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value toUpperCase(Value arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value trim(Value arg0, Value arg1, Boolean arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    public Value type(Value arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression whenCondition(Expression arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Expression whenScalar(Value arg0, Value arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    
}
