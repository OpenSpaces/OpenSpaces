/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
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
 *******************************************************************************/
package org.openspaces.persistency.cassandra.datasource;

import java.util.Iterator;

import com.gigaspaces.datasource.DataIterator;

/**
 * A simple adapter from {@link Iterator} to {@link DataIterator}
 * 
 * @since 9.1.1
 * @author Dan Kilman
 */
public class DataIteratorAdapter<T>
        implements DataIterator<T> {
    private final Iterator<T> underlying;

    public DataIteratorAdapter(Iterator<T> iterator) {
        underlying = iterator;
    }
    
    @Override
    public boolean hasNext() {
        return underlying.hasNext();
    }

    @Override
    public T next() {
        return underlying.next();
    }

    @Override
    public void remove() {
         underlying.remove();
    }

    @Override
    public void close() {
        // do nothing
    }
}
