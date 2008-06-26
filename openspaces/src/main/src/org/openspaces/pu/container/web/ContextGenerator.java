/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.pu.container.web;

import org.springframework.beans.factory.FactoryBean;

/**
 * A simple builder that can append a list of Strings into a single context path.
 *
 * @author kimchy
 */
public class ContextGenerator implements FactoryBean {

    private String[] contexParts = new String[0];

    public ContextGenerator(String context) {
        contexParts = new String[]{context};
    }

    /**
     * Constucts a new ContextGenerator that is composed of all the provided
     * context parts.
     *
     * @see #getContext()
     */
    public ContextGenerator(String[] contexParts) {
        this.contexParts = contexParts;
    }

    /**
     * Return "/" and then appends all the provided context parts.
     */
    public String getContext() {
        StringBuilder sb = new StringBuilder();
        for (String contextPart : contexParts) {
            sb.append(contextPart);
        }
        return sb.toString();
    }

    public Object getObject() throws Exception {
        return getContext();
    }

    public Class getObjectType() {
        return String.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
