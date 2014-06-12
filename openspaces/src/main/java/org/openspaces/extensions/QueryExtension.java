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

import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
import com.gigaspaces.query.*;
import com.j_spaces.core.client.SQLQuery;
import org.openspaces.core.GigaSpace;

import java.util.List;

/**
 * @author Niv Ingberg
 * @since 10.0
 */
public class QueryExtension {

    public static <T> List<Object> aggregate(GigaSpace gigaSpace, SQLQuery<T> query, AggregationSet aggregations) {
        ISpaceProxy spaceProxy = (ISpaceProxy) gigaSpace.getSpace();

        try {
            return spaceProxy.aggregate(query, aggregations, gigaSpace.getCurrentTransaction(), gigaSpace.getDefaultReadModifiers().getCode()).getResults();
        } catch (Exception e) {
            throw gigaSpace.getExceptionTranslator().translate(e);
        }
    }

    public static <T> Number maxValue(GigaSpace gigaSpace, SQLQuery<T> query, String path) {
        return (Number) aggregate(gigaSpace, query, new AggregationSet().maxValue(path)).get(0);
    }

    public static <T> T maxEntry(GigaSpace gigaSpace, SQLQuery<T> query, String path) {
        return (T) aggregate(gigaSpace, query, new AggregationSet().maxEntry(path)).get(0);
    }

    public static <T> Number minValue(GigaSpace gigaSpace, SQLQuery<T> query, String path) {
        return (Number) aggregate(gigaSpace, query, new AggregationSet().minValue(path)).get(0);
    }

    public static <T> T minEntry(GigaSpace gigaSpace, SQLQuery<T> query, String path) {
        return (T) aggregate(gigaSpace, query, new AggregationSet().minEntry(path)).get(0);
    }

    public static <T> Number sum(GigaSpace gigaSpace, SQLQuery<T> query, String path) {
        return (Number) aggregate(gigaSpace, query, new AggregationSet().sum(path)).get(0);
    }

    public static <T> Double average(GigaSpace gigaSpace, SQLQuery<T> query, String path) {
        return (Double) aggregate(gigaSpace, query, new AggregationSet().average(path)).get(0);
    }
}
