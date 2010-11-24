package org.openspaces.jpa.openjpa.query.executor;

import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.openspaces.jpa.openjpa.GSStoreManager;

/**
 * An interface for JPA's translated expression tree executor.
 * 
 * @author idan
 * @since 8.0
 * 
 */
public interface JpaQueryExecutor {
    /**
     * Execute the JPA translated expression tree.
     * @param store The store manager.
     * @return Read entries from space.
     * @throws Exception
     */
    public ResultObjectProvider execute(GSStoreManager store) throws Exception;
    
    /**
     * Gets the executor's generated SQL buffer.
     */
    public StringBuilder getSqlBuffer();
    
}
