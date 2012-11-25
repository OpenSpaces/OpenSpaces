/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.persistency.cassandra.meta.mapping;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import com.gigaspaces.internal.io.IOUtils;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.metadata.SpaceTypeDescriptorVersionedSerializationUtils;

/**
 * A {@link SpaceTypeDescriptor} holder which serializes/deserializes its underlying {@link SpaceTypeDescriptor}
 * In a version compatible manner.
 * 
 * @since 9.5
 * @author Dan Kilman
 */
public class SpaceTypeDescriptorHolder implements Externalizable {
    
    private static final long serialVersionUID = 1L;

    private SpaceTypeDescriptor spaceTypeDescriptor;
    
    /* Externalizable */
    public SpaceTypeDescriptorHolder() {
        
    }
    
    public SpaceTypeDescriptorHolder(SpaceTypeDescriptor typeDescriptor) {
        spaceTypeDescriptor = typeDescriptor;
    }
    
    public String getTypeName() {
        return spaceTypeDescriptor.getTypeName();
    }

    public String getSuperTypeName() {
        return spaceTypeDescriptor.getSuperTypeName();
    }

    public SpaceTypeDescriptor getTypeDescriptor() {
        return spaceTypeDescriptor;
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        IOUtils.writeObject(out, SpaceTypeDescriptorVersionedSerializationUtils
                                         .toSerializableForm(spaceTypeDescriptor));
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        Serializable typeDescriptorVersionedSerializableWrapper = IOUtils.readObject(in);
        spaceTypeDescriptor = SpaceTypeDescriptorVersionedSerializationUtils
                                      .fromSerializableForm(typeDescriptorVersionedSerializableWrapper);
        
    }
    
}
