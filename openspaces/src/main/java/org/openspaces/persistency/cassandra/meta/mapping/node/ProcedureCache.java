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
package org.openspaces.persistency.cassandra.meta.mapping.node;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentMap;
import com.gigaspaces.internal.reflection.IConstructor;
import com.gigaspaces.internal.reflection.IGetterMethod;
import com.gigaspaces.internal.reflection.ISetterMethod;
import com.gigaspaces.internal.reflection.ReflectionUtil;
import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

/**
 * Cache for holding fast reflection based getters/setters/constructors.
 * Instance will be created on demand and only once.
 * 
 * @since 9.1.1
 * @author Dan Kilman
 */
public class ProcedureCache {

    private final ConcurrentMap<Method, IGetterMethod<Object>> getters;
    private final ConcurrentMap<Method, ISetterMethod<Object>> setters;
    private final ConcurrentMap<Constructor<Object>, IConstructor<Object>> constructors;
    
    public ProcedureCache() {
        
        getters = new MapMaker().makeComputingMap(new Function<Method, IGetterMethod<Object>>() {
            public IGetterMethod<Object> apply(Method getterMethod) {
                return ReflectionUtil.createGetterMethod(getterMethod);
            }});
        
        setters = new MapMaker().makeComputingMap(new Function<Method, ISetterMethod<Object>>() {
            public ISetterMethod<Object> apply(Method getterMethod) {
                return ReflectionUtil.createSetterMethod(getterMethod);
            }});

        constructors = new MapMaker().makeComputingMap(new Function<Constructor<Object>, 
                                                                                     IConstructor<Object>>() {
            public IConstructor<Object> apply(Constructor<Object> constructor) {
                return ReflectionUtil.createCtor(constructor);
            }});
        
    }
    
    /**
     * @param getterMethod The {@link Method} instance that matches a property getter.
     * @return A matching {@link IGetterMethod} for this getter method.
     */
    public IGetterMethod<Object> getterMethodFor(Method getterMethod) {
        try {
            return getters.get(getterMethod);
        } catch (Exception e) {
            throw new IllegalStateException("Reflection factory failed unexpectedly", e);
        }
    }
    
    /**
     * 
     * @param setterMethod The {@link Method} instance that matches a property setter.
     * @return A matching {@link ISetterMethod} for this setter method.
     */
    public ISetterMethod<Object> setterMethodFor(Method setterMethod) {
        try {
            return setters.get(setterMethod);
        } catch (Exception e) {
            throw new IllegalStateException("Reflection factory failed unexpectedly", e);
        }
    }
    
    /**
     * 
     * @param constructor The default no-args {@link Constructor} matching a POJO type
     * @return A matching {@link IConstructor} for this contructor.
     */
    public IConstructor<Object> constructorFor(Constructor<Object> constructor) {
        try {
            return constructors.get(constructor);
        } catch (Exception e) {
            throw new IllegalStateException("Reflection factory failed unexpectedly", e);
        }
    }
    
}
