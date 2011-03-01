package org.openspaces.jpa.openjpa;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.kernel.AbstractStoreQuery;
import org.apache.openjpa.kernel.QueryContext;
import org.apache.openjpa.kernel.StoreQuery;
import org.apache.openjpa.lib.rop.ListResultObjectProvider;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.util.GeneralException;
import org.apache.openjpa.util.UserException;
import org.openspaces.core.executor.Task;
import org.openspaces.jpa.StoreManager;

import com.gigaspaces.async.AsyncFuture;

/**
 * Executes native SQLQueries and task
 * 
 * @author anna
 * @since 8.0.1
 * 
 */
public class StoreManagerSQLQuery extends AbstractStoreQuery {

    private static final long serialVersionUID = 1L;

    private StoreManager _store;

    public StoreManagerSQLQuery(StoreManager store) {

        _store = store;
    }

    public StoreManager getStore() {
        return _store;
    }

    public boolean supportsParameterDeclarations() {
        return false;
    }

    public boolean supportsDataStoreExecution() {
        return true;
    }

    public Executor newDataStoreExecutor(ClassMetaData meta, boolean subclasses) {
        return new SQLExecutor(this);
    }

    public boolean requiresCandidateType() {
        return false;
    }

    public boolean requiresParameterDeclarations() {
        return false;
    }

    /**
     * Executes the filter as a SQL query.
     */
    protected static class SQLExecutor extends AbstractExecutor {

        private final boolean _execute; // native call task execution
        private final StoreManagerSQLQuery _query;

        public SQLExecutor(StoreManagerSQLQuery q) {
            _query = q;
            QueryContext ctx = q.getContext();

            String sql = StringUtils.trimToNull(ctx.getQueryString());

            String executeCommand = "execute";
            _execute = sql.length() > executeCommand.length()
                    && sql.substring(0, executeCommand.length()).equalsIgnoreCase(executeCommand);

            if (!_execute)
                throw new UserException("Unsupported native query syntax - " + sql);
        }

        public int getOperation(StoreQuery q) {
            return (q.getContext().getCandidateType() != null || q.getContext().getResultType() != null
                    || q.getContext().getResultMappingName() != null || q.getContext().getResultMappingScope() != null) ? OP_SELECT : OP_UPDATE;
        }

        public ResultObjectProvider executeQuery(StoreQuery q, Object[] params, Range range) {

            if (_execute) 
            {
                if (params == null)
                    throw new UserException("Execute task is not supported for non-parameterized query.");
                if (params.length != 1)
                    throw new UserException("Illegal number of arguments <" + params.length + "> should be <1>.");

                if (!(params[0] instanceof Task))
                    throw new UserException("Illegal task execution parameter type - " + params[0].getClass().getName()
                            + " . " + Task.class.getName() + " is expected.");
                Task<?> task = (Task<?>) params[0];
                AsyncFuture<?> future = _query.getStore().getConfiguration().getGigaSpace().execute(task);
                Object taskResult;
                try {
                    taskResult = future.get();
                    List resultList = new LinkedList();
                    resultList.add(taskResult);
                    return new ListResultObjectProvider(resultList);
                } catch (Exception e) {
                    throw new GeneralException(e);
                }
            }

            return null;
        }

        public String[] getDataStoreActions(StoreQuery q, Object[] params, Range range) {
            return new String[] { q.getContext().getQueryString() };
        }

        public boolean isPacking(StoreQuery q) {
            return q.getContext().getCandidateType() == null;
        }

        /**
         * The given query is parsed to find the parameter tokens of the form <code>?n</code> which
         * is different than <code>?</code> tokens in actual SQL parameter tokens. These
         * <code>?n</code> style tokens are replaced in the query string by <code>?</code> tokens.
         * 
         * During the token parsing, the ordering of the tokens is recorded. The given userParam
         * must contain parameter keys as Integer and the same Integers must appear in the tokens.
         * 
         */
        public Object[] toParameterArray(StoreQuery q, Map userParams) {
            if (userParams == null || userParams.isEmpty())
                return StoreQuery.EMPTY_OBJECTS;
            String sql = q.getContext().getQueryString();
            List<Integer> paramOrder = new ArrayList<Integer>();
            try {
                sql = substituteParams(sql, paramOrder);
            } catch (IOException ex) {
                throw new UserException(ex.getLocalizedMessage());
            }

            Object[] result = new Object[paramOrder.size()];
            int idx = 0;
            for (Integer key : paramOrder) {
                if (!userParams.containsKey(key))
                    throw new UserException("Missing parameter " + key + " in " + sql);
                result[idx++] = userParams.get(key);
            }
            // modify original JPA-style SQL to proper SQL
            q.getContext().getQuery().setQuery(sql);
            return result;
        }
    }

    /**
     * Utility method to substitute '?num' for parameters in the given SQL statement, and fill-in
     * the order of the parameter tokens
     */
    public static String substituteParams(String sql, List<Integer> paramOrder) throws IOException {
        // if there's no "?" parameter marker, then we don't need to
        // perform the parsing process
        if (sql.indexOf("?") == -1)
            return sql;

        paramOrder.clear();
        StreamTokenizer tok = new StreamTokenizer(new StringReader(sql));
        tok.resetSyntax();
        tok.quoteChar('\'');
        tok.wordChars('0', '9');
        tok.wordChars('?', '?');

        StringBuilder buf = new StringBuilder(sql.length());
        for (int ttype; (ttype = tok.nextToken()) != StreamTokenizer.TT_EOF;) {
            switch (ttype) {
            case StreamTokenizer.TT_WORD:
                // a token is a positional parameter if it starts with
                // a "?" and the rest of the token are all numbers
                if (tok.sval.startsWith("?")) {
                    buf.append("?");
                    String pIndex = tok.sval.substring(1);
                    if (pIndex.length() > 0) {
                        paramOrder.add(Integer.valueOf(pIndex));
                    } else { // or nothing
                        paramOrder.add(paramOrder.size() + 1);
                    }
                } else
                    buf.append(tok.sval);
                break;
            case '\'':
                buf.append('\'');
                if (tok.sval != null) {
                    buf.append(tok.sval);
                    buf.append('\'');
                }
                break;
            default:
                buf.append((char) ttype);
            }
        }
        return buf.toString();
    }
}
