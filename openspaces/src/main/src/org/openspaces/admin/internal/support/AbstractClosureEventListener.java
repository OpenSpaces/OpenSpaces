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
package org.openspaces.admin.internal.support;

import groovy.lang.Closure;

/**
 * @author kimchy
 */
public class AbstractClosureEventListener {

    private final Closure closure;

    public AbstractClosureEventListener(Object closure) {
        this.closure = (Closure) closure;
    }

    protected Closure getClosure() {
        return this.closure;
    }

    @Override
    public int hashCode() {
        return closure.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return closure.equals(((AbstractClosureEventListener) obj).closure);
    }
}
