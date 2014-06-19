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
import com.gigaspaces.query.aggregators.*;
import org.openspaces.core.GigaSpace;

/**
 * @author Niv Ingberg
 * @since 10.0
 */
public class QueryExtension {

    public static long count(GigaSpace gigaSpace, ISpaceQuery query, String path) {
        return gigaSpace.aggregate(query, new AggregationSet().count(path)).getLong(0);
    }

    public static <T extends Number> T sum(GigaSpace gigaSpace, ISpaceQuery query, String path) {
        return (T) gigaSpace.aggregate(query, new AggregationSet().sum(path)).get(0);
    }

    public static <T extends Number> T average(GigaSpace gigaSpace, ISpaceQuery query, String path) {
        return (T) gigaSpace.aggregate(query, new AggregationSet().average(path)).get(0);
    }

    public static <T extends Comparable> T max(GigaSpace gigaSpace, ISpaceQuery query, String path) {
        return (T) gigaSpace.aggregate(query, new AggregationSet().maxValue(path)).get(0);
    }

    public static <T> T maxEntry(GigaSpace gigaSpace, ISpaceQuery<T> query, String path) {
        return (T) gigaSpace.aggregate(query, new AggregationSet().maxEntry(path)).get(0);
    }

    public static <T extends Comparable> T min(GigaSpace gigaSpace, ISpaceQuery query, String path) {
        return (T) gigaSpace.aggregate(query, new AggregationSet().minValue(path)).get(0);
    }

    public static <T> T minEntry(GigaSpace gigaSpace, ISpaceQuery<T> query, String path) {
        return (T) gigaSpace.aggregate(query, new AggregationSet().minEntry(path)).get(0);
    }

    public static GroupByResult groupBy(GigaSpace gigaSpace, ISpaceQuery query, GroupByAggregator aggregator) {
        return (GroupByResult) gigaSpace.aggregate(query, new AggregationSet().groupBy(aggregator)).get(0);
    }

    public static SpaceEntriesAggregator count() {
        return new CountEntryAggregator();
    }

    public static SpaceEntriesAggregator count(String path) {
        return new CountAggregator().setPath(path);
    }

    public static SpaceEntriesAggregator sum(String path) {
        return new SumAggregator().setPath(path);
    }

    public static SpaceEntriesAggregator average(String path) {
        return new AverageAggregator().setPath(path);
    }

    public static SpaceEntriesAggregator max(String path) {
        return new MaxValueAggregator().setPath(path);
    }

    public static SpaceEntriesAggregator maxEntry(String path) {
        return new MaxEntryAggregator().setPath(path);
    }

    public static SpaceEntriesAggregator min(String path) {
        return new MinValueAggregator().setPath(path);
    }

    public static SpaceEntriesAggregator minEntry(String path) {
        return new MinEntryAggregator().setPath(path);
    }
}
