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
 * A {@link FlattenedPropertiesFilter} implementation that will return <code>true</code> for 
 * all fixed properties and <code>false</code> for all dynamic properties.
 * 
 * @since 9.1.1
 * @author Dan Kilman
 */
public class DefaultFlattenedPropertiesFilter implements FlattenedPropertiesFilter {

    @Override
    public boolean shouldFlatten(String pathToProperty, String propertyName,
            Class<?> propertyType, boolean isDynamicProperty) {
        return !isDynamicProperty;
    }

}
