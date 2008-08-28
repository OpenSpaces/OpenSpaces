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

package org.openspaces.interop;

import org.openspaces.pu.container.servicegrid.PUServiceDetails;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A service that holds a dot net.
 *
 * @author kimchy
 */
public class DotnetServiceDetails implements PUServiceDetails, Externalizable {

    private static final long serialVersionUID = 1L;
    
    private String type;
    private String description;
    private String serviceType;

    public DotnetServiceDetails() {
    }

    public DotnetServiceDetails(String type, String serviceType, String description) {
        this.type = type;
        this.serviceType = serviceType;
        this.description = description;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getDescription() {
        return description;
    }

    public String getLongDescription() {
        return "eitan, please feel me again...";
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(type);
        out.writeUTF(serviceType);
        out.writeUTF(description);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        type = in.readUTF();
        serviceType = in.readUTF();
        description = in.readUTF();
    }
}