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

package org.openspaces.example.data.processor;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * A simple bean printing out messages when it is loaded and when it is destroyed. Used
 * to demonstrate {@link org.openspaces.core.space.mode.SpaceModeContextLoader} which
 * loads a spring context only if the specific processing unit became primary in a
 * primary backup cluster topologies.
 *
 * @author kimchy
 */
public class SpaceModeContextBean {

    @GigaSpaceContext(name = "gigaSpace")
    private GigaSpace gigaSpace;

    @PostConstruct
    public void start() {
        System.out.println("SPACE MODE BEAN LOADED, SPACE [" + gigaSpace + "]");
    }

    @PreDestroy
    public void stop() {
        System.out.println("SPACE MODE BEAN DESTROYED, SPACE [" + gigaSpace + "]");
    }
}
