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

package org.openspaces.esb.mule.seda;

import com.gigaspaces.annotation.pojo.SpacePersist;
import com.gigaspaces.annotation.pojo.SpaceProperty;
import org.mule.api.MuleEvent;

/**
 * An intenral entry holding the name of the serice (that has the SEDA queue) and the
 * actual mule event to be used.
 *
 * @author kimchy
 */
public class InternalEventEntry {

    protected String name;

    protected MuleEvent event;

    protected boolean persist = false;


    public InternalEventEntry() {
    }

    public InternalEventEntry(MuleEvent event, String name) {
        this.event = event;
        this.name = name;
    }

    @SpaceProperty(index = SpaceProperty.IndexType.BASIC)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @SpacePersist
    public boolean isPersist() {
        return persist;
    }

    public void setPersist(boolean persist) {
        this.persist = persist;
    }

    public MuleEvent getEvent() {
        return event;
    }

    public void setEvent(MuleEvent event) {
        this.event = event;
    }
}