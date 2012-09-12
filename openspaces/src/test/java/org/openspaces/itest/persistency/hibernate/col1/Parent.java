/*******************************************************************************
 * 
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
 *  
 ******************************************************************************/
package org.openspaces.itest.persistency.hibernate.col1;

import java.util.HashSet;
import java.util.Set;

public class Parent {
    private String name;
    private Set children = new HashSet();

    Parent() {
    }

    public Parent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    void setName(String name) {
        this.name = name;
    }

    public Set getChildren() {
        return children;
    }

    private void setChildren(Set children) {
        this.children = children;
    }

    public void addChild(Child child) {
        children.add(child);
    }

    public String toString() {
        return name;
    }
}
