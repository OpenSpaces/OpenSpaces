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

package org.openspaces.esb.mule.queue;

import com.j_spaces.core.client.MetaDataEntry;
import org.mule.api.MuleMessage;

/**
 * An internal queue entry holding the endopint address and the actual message.
 *
 * @author kimchy
 */
public class InternalQueueEntry extends MetaDataEntry {

    public String endpointURI;

    public MuleMessage message;

    public static String[] __getSpaceIndexedFields() {
        return new String[]{"endpointURI"};
    }
}
