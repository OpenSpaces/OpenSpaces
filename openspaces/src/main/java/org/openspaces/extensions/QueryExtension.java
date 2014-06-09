/*******************************************************************************
 *
 * Copyright (c) 2014 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.openspaces.extensions;

import com.gigaspaces.async.AsyncFuture;
import com.gigaspaces.internal.client.QueryResultTypeInternal;
import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
import com.gigaspaces.internal.query.tasks.AggregateTaskResult;
import com.gigaspaces.internal.transport.IEntryPacket;
import com.gigaspaces.internal.query.tasks.AggregateTask;
import com.gigaspaces.query.AggregationModifiers;
import com.gigaspaces.query.AggregationResult;
import com.j_spaces.core.client.SQLQuery;
import org.openspaces.core.GigaSpace;

/**
 * @author Niv Ingberg
 * @since 10.0
 */
public class QueryExtension {

    public static <T> AggregationResult<T> aggregate(GigaSpace gigaSpace, SQLQuery<T> query, String path, AggregationModifiers modifiers) {
        if (modifiers == null)
            throw new IllegalArgumentException("modifiers cannot be null");
        AggregateTaskResult result = execute(gigaSpace, query, path, modifiers);
        if (result == null)
            return null;
        return new AggregationResult<T>()
                .setMaxValue(result.getMaxValue())
                .setMinValue(result.getMinValue())
                .setSum(result.getSum())
                .setAverage(result.getAverage())
                .setMaxEntry(toObject(gigaSpace, result.getMaxEntry()))
                .setMinEntry(toObject(gigaSpace, result.getMinEntry()));
    }

    public static <T> Number maxValue(GigaSpace gigaSpace, SQLQuery<T> query, String path) {
        AggregateTaskResult result = execute(gigaSpace, query, path, AggregationModifiers.MAX_VALUE);
        return result == null ? null : result.getMaxValue();
    }

    public static <T> T maxEntry(GigaSpace gigaSpace, SQLQuery<T> query, String path) {
        AggregateTaskResult result = execute(gigaSpace, query, path, AggregationModifiers.MAX_ENTRY);
        return toObject(gigaSpace, result != null ? result.getMaxEntry() : null);
    }

    public static <T> Number minValue(GigaSpace gigaSpace, SQLQuery<T> query, String path) {
        AggregateTaskResult result = execute(gigaSpace, query, path, AggregationModifiers.MIN_VALUE);
        return result == null ? null : result.getMinValue();
    }

    public static <T> T minEntry(GigaSpace gigaSpace, SQLQuery<T> query, String path) {
        AggregateTaskResult result = execute(gigaSpace, query, path, AggregationModifiers.MIN_ENTRY);
        return toObject(gigaSpace, result != null ? result.getMinEntry() : null);
    }

    public static <T> Number sum(GigaSpace gigaSpace, SQLQuery<T> query, String path) {
        AggregateTaskResult result = execute(gigaSpace, query, path, AggregationModifiers.SUM);
        return result == null ? null : result.getSum();
    }

    public static <T> Double average(GigaSpace gigaSpace, SQLQuery<T> query, String path) {
        AggregateTaskResult result = execute(gigaSpace, query, path, AggregationModifiers.AVERAGE);
        return result == null ? null : result.getAverage();
    }

    private static <T> AggregateTaskResult execute(GigaSpace gigaSpace, SQLQuery<T> query, String path, AggregationModifiers modifiers) {
        ISpaceProxy spaceProxy = (ISpaceProxy) gigaSpace.getSpace();
        AggregateTask task = new AggregateTask();
        task.setQuery(query, spaceProxy, QueryResultTypeInternal.NOT_SET);
        task.setPath(path);
        task.setModifiers(gigaSpace.getDefaultReadModifiers().getCode());
        task.setAggregateModifiers(modifiers);
        try {
            AsyncFuture<AggregateTaskResult> future = spaceProxy.execute(task, null, gigaSpace.getCurrentTransaction(), null);
            return future.get();
        } catch (Exception e) {
            throw gigaSpace.getExceptionTranslator().translate(e);
        }
    }

    private static <T> T toObject(GigaSpace gigaSpace, IEntryPacket entry) {
        ISpaceProxy spaceProxy = (ISpaceProxy) gigaSpace.getSpace();
        return (T)spaceProxy.getDirectProxy().getTypeManager().getObjectFromEntryPacket(entry, QueryResultTypeInternal.NOT_SET, false);
    }
}
