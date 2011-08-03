/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.jdbc.datasource;

import com.j_spaces.core.IJSpace;
import org.apache.commons.dbcp.AbandonedConfig;
import org.apache.commons.dbcp.AbandonedObjectPool;
import org.apache.commons.dbcp.DataSourceConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.dbcp.SQLNestedException;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;


/**
 * <p>An adaption of {@link org.apache.commons.dbcp.BasicDataSource} from jakarta commons dbcp
 * that uses a space to provide with a pooled data source implementation.
 *
 * <p>Provides all the different basic data source parameters. Requires an {@link com.j_spaces.core.IJSpace}
 * instance.
 *
 * @author kimchy
 */
public class DbcpBasicDataSource implements DataSource, InitializingBean, DisposableBean {

    private IJSpace space;

    public void setSpace(IJSpace space) {
        this.space = space;
    }

    public void setGigaSpace(GigaSpace gigaSpace) {
        this.space = gigaSpace.getSpace();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(space, "space property is required");
        createDataSource();
    }

    @Override
    public void destroy() throws Exception {
        close();
    }

    /**
     * The default auto-commit state of connections created by this pool.
     */
    protected boolean defaultAutoCommit = true;

    public synchronized boolean getDefaultAutoCommit() {
        return this.defaultAutoCommit;
    }

    public synchronized void setDefaultAutoCommit(boolean defaultAutoCommit) {
        this.defaultAutoCommit = defaultAutoCommit;
        this.restartNeeded = true;
    }


    /**
     * The default read-only state of connections created by this pool.
     */
    protected Boolean defaultReadOnly = null;

    public synchronized boolean getDefaultReadOnly() {
        if (this.defaultReadOnly != null) {
            return this.defaultReadOnly.booleanValue();
        }
        return false;
    }

    public synchronized void setDefaultReadOnly(boolean defaultReadOnly) {
        this.defaultReadOnly = defaultReadOnly ? Boolean.TRUE : Boolean.FALSE;
        this.restartNeeded = true;
    }

    /**
     * The default TransactionIsolation state of connections created by this pool.
     */
    protected int defaultTransactionIsolation = -1;

    public synchronized int getDefaultTransactionIsolation() {
        return this.defaultTransactionIsolation;
    }

    public synchronized void setDefaultTransactionIsolation(int defaultTransactionIsolation) {
        this.defaultTransactionIsolation = defaultTransactionIsolation;
        this.restartNeeded = true;
    }


    /**
     * The default "catalog" of connections created by this pool.
     */
    protected String defaultCatalog = null;

    public synchronized String getDefaultCatalog() {
        return this.defaultCatalog;
    }

    public synchronized void setDefaultCatalog(String defaultCatalog) {
        if ((defaultCatalog != null) && (defaultCatalog.trim().length() > 0)) {
            this.defaultCatalog = defaultCatalog;
        } else {
            this.defaultCatalog = null;
        }
        this.restartNeeded = true;
    }


    /**
     * The maximum number of active connections that can be allocated from
     * this pool at the same time, or zero for no limit.
     */
    protected int maxActive = GenericObjectPool.DEFAULT_MAX_ACTIVE;

    public synchronized int getMaxActive() {
        return this.maxActive;
    }

    public synchronized void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
        if (connectionPool != null) {
            connectionPool.setMaxActive(maxActive);
        }
    }


    /**
     * The maximum number of active connections that can remain idle in the
     * pool, without extra ones being released, or zero for no limit.
     */
    protected int maxIdle = GenericObjectPool.DEFAULT_MAX_IDLE;

    public synchronized int getMaxIdle() {
        return this.maxIdle;
    }

    public synchronized void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
        if (connectionPool != null) {
            connectionPool.setMaxIdle(maxIdle);
        }
    }

    /**
     * The minimum number of active connections that can remain idle in the
     * pool, without extra ones being created, or 0 to create none.
     */
    protected int minIdle = GenericObjectPool.DEFAULT_MIN_IDLE;

    public synchronized int getMinIdle() {
        return this.minIdle;
    }

    public synchronized void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
        if (connectionPool != null) {
            connectionPool.setMinIdle(minIdle);
        }
    }

    /**
     * The initial number of connections that are created when the pool
     * is started.
     *
     * @since 1.2
     */
    protected int initialSize = 0;

    public synchronized int getInitialSize() {
        return this.initialSize;
    }

    public synchronized void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
        this.restartNeeded = true;
    }

    /**
     * The maximum number of milliseconds that the pool will wait (when there
     * are no available connections) for a connection to be returned before
     * throwing an exception, or -1 to wait indefinitely.
     */
    protected long maxWait = GenericObjectPool.DEFAULT_MAX_WAIT;

    public synchronized long getMaxWait() {
        return this.maxWait;
    }

    public synchronized void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
        if (connectionPool != null) {
            connectionPool.setMaxWait(maxWait);
        }
    }

    /**
     * Prepared statement pooling for this pool.
     */
    protected boolean poolPreparedStatements = false;

    /**
     * Returns true if we are pooling statements.
     *
     * @return boolean
     */
    public synchronized boolean isPoolPreparedStatements() {
        return this.poolPreparedStatements;
    }

    /**
     * Sets whether to pool statements or not.
     *
     * @param poolingStatements pooling on or off
     */
    public synchronized void setPoolPreparedStatements(boolean poolingStatements) {
        this.poolPreparedStatements = poolingStatements;
        this.restartNeeded = true;
    }

    /**
     * The maximum number of open statements that can be allocated from
     * the statement pool at the same time, or zero for no limit.  Since
     * a connection usually only uses one or two statements at a time, this is
     * mostly used to help detect resource leaks.
     */
    protected int maxOpenPreparedStatements = GenericKeyedObjectPool.DEFAULT_MAX_TOTAL;

    public synchronized int getMaxOpenPreparedStatements() {
        return this.maxOpenPreparedStatements;
    }

    public synchronized void setMaxOpenPreparedStatements(int maxOpenStatements) {
        this.maxOpenPreparedStatements = maxOpenStatements;
        this.restartNeeded = true;
    }

    /**
     * The indication of whether objects will be validated before being
     * borrowed from the pool.  If the object fails to validate, it will be
     * dropped from the pool, and we will attempt to borrow another.
     */
    protected boolean testOnBorrow = true;

    public synchronized boolean getTestOnBorrow() {
        return this.testOnBorrow;
    }

    public synchronized void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
        if (connectionPool != null) {
            connectionPool.setTestOnBorrow(testOnBorrow);
        }
    }

    /**
     * The indication of whether objects will be validated before being
     * returned to the pool.
     */
    protected boolean testOnReturn = false;

    public synchronized boolean getTestOnReturn() {
        return this.testOnReturn;
    }

    public synchronized void setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
        if (connectionPool != null) {
            connectionPool.setTestOnReturn(testOnReturn);
        }
    }


    /**
     * The number of milliseconds to sleep between runs of the idle object
     * evictor thread.  When non-positive, no idle object evictor thread will
     * be run.
     */
    protected long timeBetweenEvictionRunsMillis =
            GenericObjectPool.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;

    public synchronized long getTimeBetweenEvictionRunsMillis() {
        return this.timeBetweenEvictionRunsMillis;
    }

    public synchronized void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        if (connectionPool != null) {
            connectionPool.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        }
    }


    /**
     * The number of objects to examine during each run of the idle object
     * evictor thread (if any).
     */
    protected int numTestsPerEvictionRun =
            GenericObjectPool.DEFAULT_NUM_TESTS_PER_EVICTION_RUN;

    public synchronized int getNumTestsPerEvictionRun() {
        return this.numTestsPerEvictionRun;
    }

    public synchronized void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
        if (connectionPool != null) {
            connectionPool.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
        }
    }


    /**
     * The minimum amount of time an object may sit idle in the pool before it
     * is eligible for eviction by the idle object evictor (if any).
     */
    protected long minEvictableIdleTimeMillis =
            GenericObjectPool.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;

    public synchronized long getMinEvictableIdleTimeMillis() {
        return this.minEvictableIdleTimeMillis;
    }

    public synchronized void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
        if (connectionPool != null) {
            connectionPool.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        }
    }

    /**
     * The indication of whether objects will be validated by the idle object
     * evictor (if any).  If an object fails to validate, it will be dropped
     * from the pool.
     */
    protected boolean testWhileIdle = false;

    public synchronized boolean getTestWhileIdle() {
        return this.testWhileIdle;
    }

    public synchronized void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
        if (connectionPool != null) {
            connectionPool.setTestWhileIdle(testWhileIdle);
        }
    }

    /**
     * [Read Only] The current number of active connections that have been
     * allocated from this data source.
     */
    public synchronized int getNumActive() {
        if (connectionPool == null) 
            return 0;

        return connectionPool.getNumActive();
    }


    /**
     * [Read Only] The current number of idle connections that are waiting
     * to be allocated from this data source.
     */
    public synchronized int getNumIdle() {
        if (connectionPool == null) 
            return 0;

        return connectionPool.getNumIdle();
    }

    /**
     * The SQL query that will be used to validate connections from this pool
     * before returning them to the caller.  If specified, this query
     * <strong>MUST</strong> be an SQL SELECT statement that returns at least
     * one row.
     */
    protected String validationQuery = null;

    public synchronized String getValidationQuery() {
        return this.validationQuery;
    }

    public synchronized void setValidationQuery(String validationQuery) {
        if ((validationQuery != null) && (validationQuery.trim().length() > 0)) {
            this.validationQuery = validationQuery;
        } else {
            this.validationQuery = null;
        }
        this.restartNeeded = true;
    }

    /**
     * Controls access to the underlying connection
     */
    private boolean accessToUnderlyingConnectionAllowed = false;

    /**
     * Returns the value of the accessToUnderlyingConnectionAllowed property.
     *
     * @return true if access to the underlying is allowed, false otherwise.
     */
    public synchronized boolean isAccessToUnderlyingConnectionAllowed() {
        return this.accessToUnderlyingConnectionAllowed;
    }

    /**
     * Sets the value of the accessToUnderlyingConnectionAllowed property.
     * It controls if the PoolGuard allows access to the underlying connection.
     * (Default: false)
     *
     * @param allow Access to the underlying connection is granted when true.
     */
    public synchronized void setAccessToUnderlyingConnectionAllowed(boolean allow) {
        this.accessToUnderlyingConnectionAllowed = allow;
        this.restartNeeded = true;
    }

    // ----------------------------------------------------- Instance Variables

    // TODO: review & make isRestartNeeded() public, restartNeeded protected

    private boolean restartNeeded = false;

    /**
     * Returns whether or not a restart is needed.
     *
     * @return true if a restart is needed
     */
    private synchronized boolean isRestartNeeded() {
        return restartNeeded;
    }

    /**
     * The object pool that internally manages our connections.
     */
    protected GenericObjectPool connectionPool = null;

    /**
     * The connection properties that will be sent to our JDBC driver when
     * establishing new connections.  <strong>NOTE</strong> - The "user" and
     * "password" properties will be passed explicitly, so they do not need
     * to be included here.
     */
    protected Properties connectionProperties = new Properties();

    /**
     * The data source we will use to manage connections.  This object should
     * be acquired <strong>ONLY</strong> by calls to the
     * <code>createDataSource()</code> method.
     */
    protected DataSource dataSource = null;

    /**
     * The PrintWriter to which log messages should be directed.
     */
    protected PrintWriter logWriter = new PrintWriter(System.out);

    // ----------------------------------------------------- DataSource Methods


    /**
     * Create (if necessary) and return a connection to the database.
     *
     * @throws SQLException if a database access error occurs
     */
    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }


    /**
     * Create (if necessary) and return a connection to the database.
     *
     * @param username Database user on whose behalf the Connection
     *                 is being made
     * @param password The database user's password
     * @throws SQLException if a database access error occurs
     */
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return dataSource.getConnection(username, password);
    }


    /**
     * Return the login timeout (in seconds) for connecting to the database.
     *
     * @throws SQLException if a database access error occurs
     */
    @Override
    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }


    /**
     * Return the log writer being used by this data source.
     *
     * @throws SQLException if a database access error occurs
     */
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }


    /**
     * Set the login timeout (in seconds) for connecting to the database.
     *
     * @param loginTimeout The new login timeout, or zero for no timeout
     * @throws SQLException if a database access error occurs
     */
    @Override
    public void setLoginTimeout(int loginTimeout) throws SQLException {
        dataSource.setLoginTimeout(loginTimeout);
    }


    /**
     * Set the log writer being used by this data source.
     *
     * @param logWriter The new log writer
     * @throws SQLException if a database access error occurs
     */
    @Override
    public void setLogWriter(PrintWriter logWriter) throws SQLException {
        dataSource.setLogWriter(logWriter);
        this.logWriter = logWriter;
    }

    private AbandonedConfig abandonedConfig;

    /**
     * Flag to remove abandoned connections if they exceed the
     * removeAbandonedTimout.
     *
     * Set to true or false, default false.
     * If set to true a connection is considered abandoned and eligible
     * for removal if it has been idle longer than the removeAbandonedTimeout.
     * Setting this to true can recover db connections from poorly written
     * applications which fail to close a connection.
     *
     * @deprecated
     */
    @Deprecated
    public boolean getRemoveAbandoned() {
        if (abandonedConfig != null) {
            return abandonedConfig.getRemoveAbandoned();
        }
        return false;
    }

    /**
     * @param removeAbandoned
     * @deprecated
     */
    @Deprecated
    public void setRemoveAbandoned(boolean removeAbandoned) {
        if (abandonedConfig == null) {
            abandonedConfig = new AbandonedConfig();
        }
        abandonedConfig.setRemoveAbandoned(removeAbandoned);
        this.restartNeeded = true;
    }

    /**
     * Timeout in seconds before an abandoned connection can be removed.
     *
     * Defaults to 300 seconds.
     *
     * @deprecated
     */
    @Deprecated
    public int getRemoveAbandonedTimeout() {
        if (abandonedConfig != null) {
            return abandonedConfig.getRemoveAbandonedTimeout();
        }
        return 300;
    }

    /**
     * @param removeAbandonedTimeout
     * @deprecated
     */
    @Deprecated
    public void setRemoveAbandonedTimeout(int removeAbandonedTimeout) {
        if (abandonedConfig == null) {
            abandonedConfig = new AbandonedConfig();
        }
        abandonedConfig.setRemoveAbandonedTimeout(removeAbandonedTimeout);
        this.restartNeeded = true;
    }

    /**
     * Flag to log stack traces for application code which abandoned
     * a Statement or Connection.
     *
     * Defaults to false.
     *
     * Logging of abandoned Statements and Connections adds overhead
     * for every Connection open or new Statement because a stack
     * trace has to be generated.
     *
     * @deprecated
     */
    @Deprecated
    public boolean getLogAbandoned() {
        if (abandonedConfig != null) {
            return abandonedConfig.getLogAbandoned();
        }
        return false;
    }

    /**
     * @param logAbandoned
     * @deprecated
     */
    @Deprecated
    public void setLogAbandoned(boolean logAbandoned) {
        if (abandonedConfig == null) {
            abandonedConfig = new AbandonedConfig();
        }
        abandonedConfig.setLogAbandoned(logAbandoned);
        this.restartNeeded = true;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Add a custom connection property to the set that will be passed to our
     * JDBC driver.    This <strong>MUST</strong> be called before the first
     * connection is retrieved (along with all the other configuration
     * property setters).
     *
     * @param name  Name of the custom connection property
     * @param value Value of the custom connection property
     */
    public void addConnectionProperty(String name, String value) {
        connectionProperties.put(name, value);
        this.restartNeeded = true;
    }

    public void removeConnectionProperty(String name) {
        connectionProperties.remove(name);
        this.restartNeeded = true;
    }

    /**
     * Close and release all connections that are currently stored in the
     * connection pool associated with our data source.
     *
     * @throws SQLException if a database error occurs
     */
    public synchronized void close() throws SQLException {
        GenericObjectPool oldpool = connectionPool;
        connectionPool = null;
        dataSource = null;
        try {
            if (oldpool != null) {
                oldpool.close();
            }
        } catch (SQLException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLNestedException("Cannot close connection pool", e);
        }
    }

    // ------------------------------------------------------ Protected Methods


    /**
     * <p>Create (if necessary) and return the internal data source we are
     * using to manage our connections.</p>
     *
     * <p><strong>IMPLEMENTATION NOTE</strong> - It is tempting to use the
     * "double checked locking" idiom in an attempt to avoid synchronizing
     * on every single call to this method.  However, this idiom fails to
     * work correctly in the face of some optimizations that are legal for
     * a JVM to perform.</p>
     *
     * @throws SQLException if the object pool cannot be created.
     */
    protected void createDataSource()
            throws SQLException {

        // Can't test without a validationQuery
        if (validationQuery == null) {
            setTestOnBorrow(false);
            setTestOnReturn(false);
            setTestWhileIdle(false);
        }

        // Create an object pool to contain our active connections
        if ((abandonedConfig != null) && (abandonedConfig.getRemoveAbandoned() == true)) {
            connectionPool = new AbandonedObjectPool(null, abandonedConfig);
        } else {
            connectionPool = new GenericObjectPool();
        }
        connectionPool.setMaxActive(maxActive);
        connectionPool.setMaxIdle(maxIdle);
        connectionPool.setMinIdle(minIdle);
        connectionPool.setMaxWait(maxWait);
        connectionPool.setTestOnBorrow(testOnBorrow);
        connectionPool.setTestOnReturn(testOnReturn);
        connectionPool.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        connectionPool.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
        connectionPool.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        connectionPool.setTestWhileIdle(testWhileIdle);

        // Set up statement pool, if desired
        GenericKeyedObjectPoolFactory statementPoolFactory = null;
        if (isPoolPreparedStatements()) {
            statementPoolFactory = new GenericKeyedObjectPoolFactory(null,
                    -1, // unlimited maxActive (per key)
                    GenericKeyedObjectPool.WHEN_EXHAUSTED_FAIL,
                    0, // maxWait
                    1, // maxIdle (per key)
                    maxOpenPreparedStatements);
        }

        DataSourceConnectionFactory dataSourceConnectionFactory = new DataSourceConnectionFactory(new SpaceDriverManagerDataSource(space));

        // Set up the poolable connection factory we will use
        try {
            PoolableConnectionFactory connectionFactory =
                    new PoolableConnectionFactory(dataSourceConnectionFactory,
                            connectionPool,
                            statementPoolFactory,
                            validationQuery,
                            defaultReadOnly,
                            defaultAutoCommit,
                            defaultTransactionIsolation,
                            defaultCatalog,
                            abandonedConfig);
            validateConnectionFactory(connectionFactory);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLNestedException("Cannot create PoolableConnectionFactory (" + e.getMessage() + ")", e);
        }

        // Create and return the pooling data source to manage the connections
        dataSource = new PoolingDataSource(connectionPool);
        ((PoolingDataSource) dataSource).setAccessToUnderlyingConnectionAllowed(isAccessToUnderlyingConnectionAllowed());
        dataSource.setLogWriter(logWriter);

        try {
            for (int i = 0; i < initialSize; i++) {
                connectionPool.addObject();
            }
        } catch (Exception e) {
            throw new SQLNestedException("Error preloading the connection pool", e);
        }
    }

    private static void validateConnectionFactory(PoolableConnectionFactory connectionFactory) throws Exception {
        Connection conn = null;
        try {
            conn = (Connection) connectionFactory.makeObject();
            connectionFactory.activateObject(conn);
            connectionFactory.validateConnection(conn);
            connectionFactory.passivateObject(conn);
        }
        finally {
            connectionFactory.destroyObject(conn);
        }
    }

    private void restart() {
        try {
            close();
        } catch (SQLException e) {
            log("Could not restart DataSource, cause: " + e.getMessage());
        }
    }

    private void log(String message) {
        if (logWriter != null) {
            logWriter.println(message);
        }
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException();
    }
}
