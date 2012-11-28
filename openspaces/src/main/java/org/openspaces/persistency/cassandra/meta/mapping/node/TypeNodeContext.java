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

/**
 * Context used during type introspection and during read/write operations
 * 
 * @since 9.1.1
 * @author Dan Kilman
 */
public class TypeNodeContext {
    
    private final TypeNodeIntrospector typeNodeIntrospector;
    private final boolean              useDynamicPropertySerializerForDynamicColumns;

    private boolean                    isDynamic;
    private int                        currentNestingLevel = 0;


    public TypeNodeContext() {
        this(null, true);
    }
    
    public TypeNodeContext(
            TypeNodeIntrospector typeNodeIntrospector, 
            boolean useDynamicPropertySerializerForDynamicColumns) {
        this.typeNodeIntrospector = typeNodeIntrospector;
        this.useDynamicPropertySerializerForDynamicColumns = useDynamicPropertySerializerForDynamicColumns;
        
    }
    
    /**
     * @return Current nesting level (used during type introspection and during write operations).
     */
    public int getCurrentNestingLevel() {
        return currentNestingLevel; 
    }
    
    /**
     * Increase the current nesting level.
     */
    public void increaseNestingLevel() {
        currentNestingLevel++;
    }
    
    /**
     * Decrease the current nesting level.
     */
    public void descreaseNestingLevel() {
        currentNestingLevel--;
    }

    /**
     * @return <code>true</code> if the current nesting level is bigger than
     * the configured maximum allowed nesting level.
     */
    public boolean surpassedLastAllowedNestingLevel() {
        return currentNestingLevel > typeNodeIntrospector.getMaxNestingLevel();
    }
    
    /**
     * @return Whether the current location in the type introspaction hierarcy is a descendent of a dynamic type node
     * property.
     */
    public boolean isDynamic() {
        return isDynamic;
    }

    /**
     * Sets the current dynamic context value.
     */
    public void setDynamic(boolean isDynamic) {
        this.isDynamic = isDynamic;
    }

    /**
     * @return The type node introspector.
     */
    public TypeNodeIntrospector getTypeNodeIntrospector() {
        return typeNodeIntrospector;
    }

    /**
     * @return <code>true</code> if dynamic columns should be serialized using the dynamic property serializer.
     */
    public boolean isUseDynamicPropertySerializerForDynamicColumns() {
        return useDynamicPropertySerializerForDynamicColumns;
    }
}
