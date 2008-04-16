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
package org.openspaces.persistency;

/**
 * Use this interface with {@link org.openspaces.persistency.hibernate.HibernateExternalDataSource} in order
 * to filter out exceptions thrown from the implementation of the
 * {@link com.gigaspaces.datasource.ExternalDataSource#executeBulk(java.util.List)} method.
 * This can be useful in case you identify an exception that is likely to recur over and over again
 * and if thrown back to the source space will cause it to replay the last bulk over and over again.
 * This can in turn generate an ever growing backlog of bulks at the space side and cause out of
 * memory.
 *
 * @author Uri Cohen
 */
public interface BulkDataPersisterExceptionFilter {
    /**
     * Return true if the exception should be filtered and not thrown from within the
     * {@link org.openspaces.persistency.hibernate.HibernateExternalDataSource#executeBulk}
     * method
     * @param exception the exception thrown from the underlying persistence engine
     * @return true if the exception should be filtered, false otherwise
     */
    boolean shouldFilter(Exception exception);
}
