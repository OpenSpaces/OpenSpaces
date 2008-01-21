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
package org.openspaces.itest.esb.mule.message;

import org.openspaces.esb.mule.message.AbstractMessageHeader;
import org.openspaces.itest.esb.mule.Message;

/**
 * A simple message object that is written to the space.
 * This object also carrying with it metadata.
 *
 * @see org.openspaces.esb.mule.message.AbstractMessageHeader
 *
 * @author yitzhaki
 */
public class ProcessedMessage extends AbstractMessageHeader implements Message {

    private String message;

    public ProcessedMessage() {
    }

    public ProcessedMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProcessedMessage message1 = (ProcessedMessage) o;

        if (message != null ? !message.equals(message1.message) : message1.message != null) return false;

        return true;
    }

}
