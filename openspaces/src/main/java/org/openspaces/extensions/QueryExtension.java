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

import com.gigaspaces.query.ISpaceQuery;
import com.gigaspaces.query.aggregators.AggregationSet;
import com.gigaspaces.query.aggregators.GroupByAggregator;
import com.gigaspaces.query.aggregators.GroupByResult;
import org.openspaces.core.GigaSpace;

/**
 * @author Niv Ingberg
 * @since 10.0
 */
public class QueryExtension {

    public static <T> Number count(GigaSpace gigaSpace, ISpaceQuery<T> query, String path) {
        return (Number) gigaSpace.aggregate(query, new AggregationSet().count(path)).get(0);
    }

    public static <T> Number sum(GigaSpace gigaSpace, ISpaceQuery<T> query, String path) {
        return (Number) gigaSpace.aggregate(query, new AggregationSet().sum(path)).get(0);
    }

    public static <T> Double average(GigaSpace gigaSpace, ISpaceQuery<T> query, String path) {
        return (Double) gigaSpace.aggregate(query, new AggregationSet().average(path)).get(0);
    }

    public static <T> Number maxValue(GigaSpace gigaSpace, ISpaceQuery<T> query, String path) {
        return (Number) gigaSpace.aggregate(query, new AggregationSet().maxValue(path)).get(0);
    }

    public static <T> T maxEntry(GigaSpace gigaSpace, ISpaceQuery<T> query, String path) {
        return (T) gigaSpace.aggregate(query, new AggregationSet().maxEntry(path)).get(0);
    }

    public static <T> Number minValue(GigaSpace gigaSpace, ISpaceQuery<T> query, String path) {
        return (Number) gigaSpace.aggregate(query, new AggregationSet().minValue(path)).get(0);
    }

    public static <T> T minEntry(GigaSpace gigaSpace, ISpaceQuery<T> query, String path) {
        return (T) gigaSpace.aggregate(query, new AggregationSet().minEntry(path)).get(0);
    }

    public static <T> GroupByResult groupBy(GigaSpace gigaSpace, ISpaceQuery<T> query, GroupByAggregator aggregator) {
        return (GroupByResult) gigaSpace.aggregate(query, new AggregationSet().groupBy(aggregator)).get(0);
    }
}
