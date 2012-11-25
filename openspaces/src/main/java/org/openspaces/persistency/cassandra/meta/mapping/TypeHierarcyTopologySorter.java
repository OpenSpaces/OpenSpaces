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
package org.openspaces.persistency.cassandra.meta.mapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gigaspaces.metadata.SpaceTypeDescriptor;

/**
 * A utility class used for sorting {@link SpaceTypeDescriptor} instances.
 * @see #getSortedList(Map)
 * 
 * @since 9.5
 * @author Dan Kilman
 */
public class TypeHierarcyTopologySorter {
    
    private final TypeNameNode              root  = new TypeNameNode(Object.class.getName(), null);
    private final Map<String, TypeNameNode> nodes = new HashMap<String, TypeNameNode>();
    
    private TypeHierarcyTopologySorter() {
        nodes.put(root.typeName, root);
    }
    
    private void addTypeName(String typeName, String superTypeName) {
        TypeNameNode typeNameNode = nodes.get(typeName);
        TypeNameNode superTypeNameNode = nodes.get(superTypeName);
        
        if (typeNameNode == null) {
            typeNameNode = new TypeNameNode(typeName, superTypeName);
            nodes.put(typeName, typeNameNode);
        } else {
            typeNameNode.superTypeName = superTypeName;
        }
        
        if (superTypeNameNode == null) {
            superTypeNameNode = new TypeNameNode(superTypeName, Object.class.getName());
        }
        
        superTypeNameNode.children.add(typeName);
        nodes.put(superTypeName, superTypeNameNode);
    }
    
    private TypeNameNode fixAndGetRoot() {
        for (TypeNameNode typeNameNode : nodes.values()) {
            if (typeNameNode != root && typeNameNode.superTypeName.equals(root.typeName)) {
                root.children.add(typeNameNode.typeName);
            }
        }
        return root;
    }
    
    private class TypeNameNode {
        private final String      typeName;
        private String            superTypeName;
        private final Set<String> children = new HashSet<String>();
        
        private TypeNameNode(String typeName, String superTypeName) {
            this.typeName = typeName;
            this.superTypeName = superTypeName;
        }
    }

    /**
     * @param typeDescriptorHolders A map from type name to its matching {@link SpaceTypeDescriptorHolder}
     * @return A list of {@link SpaceTypeDescriptor} instances sorted in such way that super types will 
     * precede their sub types. 
     */
    public static List<SpaceTypeDescriptor> getSortedList(
            Map<String, SpaceTypeDescriptorHolder> typeDescriptorHolders) {
        TypeHierarcyTopologySorter sorter = new TypeHierarcyTopologySorter();
        for (SpaceTypeDescriptorHolder typeDescriptorHolder : typeDescriptorHolders.values())
            sorter.addTypeName(typeDescriptorHolder.getTypeName(),
                               typeDescriptorHolder.getSuperTypeName());

        Map<String, TypeNameNode> allTypeNameNodes = sorter.nodes;
        TypeNameNode root = sorter.fixAndGetRoot();

        List<SpaceTypeDescriptor> result = new LinkedList<SpaceTypeDescriptor>();

        // root is java.lang.Object so we skip him
        for (String typeName : root.children) {
            addSelfThenChildren(typeName,
                                typeDescriptorHolders,
                                allTypeNameNodes,
                                result);
        }
        
        return result;
    }

    private static void addSelfThenChildren(
            String typeName,
            Map<String, SpaceTypeDescriptorHolder> 
            typeDescriptorHolders,
            Map<String, TypeNameNode> nodes, 
            List<SpaceTypeDescriptor> result) {
        SpaceTypeDescriptorHolder typeDescriptorData = typeDescriptorHolders.get(typeName);
        SpaceTypeDescriptor typeDescriptor = typeDescriptorData.getTypeDescriptor();
        result.add(typeDescriptor);
        TypeNameNode typeNameNode = nodes.get(typeName);
        for (String childTypeName : typeNameNode.children) {
            addSelfThenChildren(childTypeName, 
                                typeDescriptorHolders, 
                                nodes, 
                                result);
        }
    }
    
}