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
package org.openspaces.itest.esb.servicemix;

/**
 * @author yitzhaki
 */
public class Message {

    private String msg;

    private boolean read;

    public Message(String msg, boolean read) {
        this.msg = msg;
        this.read = read;
    }

    public Message() {
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String toString() {
        return msg + " " + read;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (read != message.read) return false;
        if (msg != null ? !msg.equals(message.msg) : message.msg != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (msg != null ? msg.hashCode() : 0);
        result = 31 * result + (read ? 1 : 0);
        return result;
    }
}
