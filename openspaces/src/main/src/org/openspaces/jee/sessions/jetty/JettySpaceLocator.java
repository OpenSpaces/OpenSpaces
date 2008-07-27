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

package org.openspaces.jee.sessions.jetty;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.FinderException;
import com.j_spaces.core.client.SpaceFinder;
import org.openspaces.core.GigaSpace;
import org.openspaces.pu.container.jee.jetty.JettyJeeProcessingUnitContainerProvider;
import org.springframework.context.ApplicationContext;

/**
 * Locates a Space based on an enhanced Space URL that also supports bean:// notation.
 *
 * @author kimchy
 */
public class JettySpaceLocator {

    public static final String BEAN_PREFIX = "bean://";

    public static IJSpace locate(String spaceUrl) throws FinderException {
        IJSpace space;
        if (spaceUrl.startsWith(BEAN_PREFIX)) {
            ApplicationContext applicationContext = JettyJeeProcessingUnitContainerProvider.getCurrentApplicationContext();
            if (applicationContext == null) {
                throw new IllegalStateException("Failed to find thread bounded application context");
            }
            Object bean = applicationContext.getBean(spaceUrl.substring(BEAN_PREFIX.length()));
            if (bean instanceof GigaSpace) {
                space = ((GigaSpace) bean).getSpace();
            } else if (bean instanceof IJSpace) {
                space = (IJSpace) bean;
            } else {
                throw new IllegalArgumentException("Bean [" + bean + "] is not of either GigaSpace type or IJSpace type");
            }
        } else {
            space = (IJSpace) SpaceFinder.find(spaceUrl);
        }
        return space;
    }
}
