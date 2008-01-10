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

import com.j_spaces.core.client.MetaDataEntry;
import org.mule.umo.UMOEvent;

/**
 * An intenral entry holding the name of the serice (that has the SEDA queue) and the
 * actual mule event to be used.
 *
 * @author kimchy
 */
public class InternalEventEntry extends MetaDataEntry {

    public String name;

    public UMOEvent event;

    public InternalEventEntry() {
    }

    public InternalEventEntry(UMOEvent event, String name) {
        this.event = event;
        this.name = name;
    }

    public static String[] __getSpaceIndexedFields() {
        return new String[]{"name"};
    }
}