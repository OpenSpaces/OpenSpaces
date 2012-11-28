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
package org.openspaces.persistency.cassandra.meta.mapping.filter;

/**
 * An interface used to denote whether a given property should be flattened.
 * @see #shouldFlatten(String, String, Class, boolean)
 * 
 * @since 9.1.1
 * @author Dan Kilman
 */
public interface FlattenedPropertiesFilter {
    
    /**
     * @param pathToProperty the path to this property. For example if the root entry contains a POJO property
     *  named <code>x</code> and this pojo contains another POJO property named <code>y</code> and 
     *  this property is part of the <code>y</code> property, then the path will be <code>x.y</code>
     * @param propertyName the property name as it appears in its parent.
     * @param propertyType the property type
     * @param isDynamicProperty true if this property is not part of the static fixed properties
     *  column family metadata
     * @return Should an attempt to flatten this entry be made.
     */
    boolean shouldFlatten(
            String pathToProperty, 
            String propertyName, 
            Class<?> propertyType, 
            boolean isDynamicProperty);
}
