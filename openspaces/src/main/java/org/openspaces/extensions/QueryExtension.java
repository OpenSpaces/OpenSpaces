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
import com.gigaspaces.internal.transport.IEntryPacket;
import com.gigaspaces.query.tasks.AggregateTask;
import com.j_spaces.core.client.SQLQuery;
import org.openspaces.core.GigaSpace;

import java.io.Serializable;

/**
 * @author Niv Ingberg
 * @since 10.0
 */
public class QueryExtension {

    public static <T,N extends Number & Comparable> N max(GigaSpace gigaSpace, SQLQuery<T> query, String path) {
        return execute(gigaSpace, new AggregateTask.Max<N>(), query, path);
    }

    public static <T> T maxBy(GigaSpace gigaSpace, SQLQuery<T> query, String path) {
        IEntryPacket entry = execute(gigaSpace, new AggregateTask.MaxBy(), query, path);
        return entry != null ? (T)entry.toObject(QueryResultTypeInternal.NOT_SET) : null;
    }

    public static <T,N extends Number & Comparable> N min(GigaSpace gigaSpace, SQLQuery<T> query, String path) {
        return execute(gigaSpace, new AggregateTask.Min<N>(), query, path);
    }

    public static <T> T minBy(GigaSpace gigaSpace, SQLQuery<T> query, String path) {
        IEntryPacket entry = execute(gigaSpace, new AggregateTask.MinBy(), query, path);
        return entry != null ? (T)entry.toObject(QueryResultTypeInternal.NOT_SET) : null;
    }

    public static <T,N extends Number & Comparable> N sum(GigaSpace gigaSpace, SQLQuery<T> query, String path) {
        return execute(gigaSpace, new AggregateTask.Sum<N>(), query, path);
    }

    public static <T> Double average(GigaSpace gigaSpace, SQLQuery<T> query, String path) {
        AggregateTask.AvgTuple averageTuple = execute(gigaSpace, new AggregateTask.Average(), query, path);
        return averageTuple != null ? averageTuple.getAverage() : null;
    }

    private static <T, N extends Serializable> N execute(GigaSpace gigaSpace, AggregateTask<N> task, SQLQuery<T> query, String path) {
        task.setQuery(query);
        task.setPath(path);
        task.setModifiers(gigaSpace.getDefaultReadModifiers().getCode());
        ISpaceProxy spaceProxy = (ISpaceProxy) gigaSpace.getSpace();
        try {
            AsyncFuture<N> future = spaceProxy.execute(task, null, gigaSpace.getCurrentTransaction(), null);
            return future.get();
        } catch (Exception e) {
            throw gigaSpace.getExceptionTranslator().translate(e);
        }
    }
}
