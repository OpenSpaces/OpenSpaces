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

package org.openspaces.hibernate.cache;

import java.util.Properties;

import javax.transaction.TransactionManager;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.transaction.TransactionManagerLookup;
import org.hibernate.transaction.TransactionManagerLookupFactory;

/**
 * Transactional Map cache provider allowing to use GigaSpaces as a second level
 * cache within a JTA environment. Uses GigaSpaces XA support to register with the
 * JTA transaction manager. Uses {@link org.openspaces.hibernate.cache.TransactionalMapCache}
 * as the cache implementation.
 *
 * @author kimchy
 */
public class TransactionalMapCacheProvider extends AbstractMapCacheProvider {

    private TransactionManager transactionManager;

    /**
     * Finds JTA transaction manager.
     */
    protected void doStart(Properties properties) throws CacheException {
        TransactionManagerLookup transactionManagerLookup = TransactionManagerLookupFactory.getTransactionManagerLookup(properties);
        if (transactionManagerLookup == null) {
            throw new CacheException("Transaction Cache Provider must work with JTA");
        }
        transactionManager = transactionManagerLookup.getTransactionManager(properties);
    }

    /**
     * Returns {@link org.openspaces.hibernate.cache.TransactionalMapCache}.
     */
    public Cache buildCache(String regionName, Properties properties) throws CacheException {
        return new TransactionalMapCache(regionName, getMap(), getTimeToLive(), getWaitForResponse(),
                transactionManager);
    }
}