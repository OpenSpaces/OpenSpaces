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

import java.util.Map;

import org.hibernate.mapping.Collection;
import org.openspaces.persistency.cassandra.meta.types.SerializerProvider;

import com.gigaspaces.internal.metadata.pojo.PojoPropertyInfo;
import com.gigaspaces.internal.metadata.pojo.PojoTypeInfo;
import com.gigaspaces.internal.metadata.pojo.PojoTypeInfoRepository;

/**
 * A {@link FlattenedPropertiesFilter} used internally to determine whether 
 * the given type is not primitive or a common java type and if so, does this
 * type have a default no-args constructors and at lease 1 property with both
 * getter and setter methods.
 * 
 * @since 9.5
 * @author Dan Kilman
 */
public class PojoTypeFlattenPropertiesFilter implements FlattenedPropertiesFilter {
    @Override
    public boolean shouldFlatten(
            String pathToProperty, 
            String propertyName, 
            Class<?> propertyType, 
            boolean isDynamicProperty) {
        // quick reject
        boolean isCandidate = !SerializerProvider.isCommonJavaType(propertyType) &&
                              !propertyType.isSynthetic() &&
                              !propertyType.isArray() &&
                              !Map.class.isAssignableFrom(propertyType) &&
                              !Collection.class.isAssignableFrom(propertyType);
        
        if (!isCandidate) {
            return false;
        }
        
        // verify type has a public default no-args constructor
        try {
            propertyType.getConstructor();
        } catch (NoSuchMethodException e) {
            return false;
        }
        
        // no need to worry about keeping a reference to this instance.
        // if number of properties is > 0 than next call will be from cache
        PojoTypeInfo typeInfo = PojoTypeInfoRepository.getPojoTypeInfo(propertyType);

        // verify that type has at least 1 property with both getter and setter methods
        for (PojoPropertyInfo propertyInfo : typeInfo.getProperties().values()) {
            if (propertyInfo.getGetterMethod() != null &&
                propertyInfo.getSetterMethod() != null) {
                return true;
            }
        }
        
        return false;
    }
}
