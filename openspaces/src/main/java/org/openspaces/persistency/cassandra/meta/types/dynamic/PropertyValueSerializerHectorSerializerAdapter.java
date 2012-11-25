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
package org.openspaces.persistency.cassandra.meta.types.dynamic;

import java.nio.ByteBuffer;

import me.prettyprint.cassandra.serializers.AbstractSerializer;
import me.prettyprint.hector.api.Serializer;

/**
 * An adapter from {@link PropertyValueSerializer} to hector's {@link Serializer}.
 * 
 * @since 9.5
 * @author Dan Kilman
 */
public class PropertyValueSerializerHectorSerializerAdapter extends AbstractSerializer<Object> {
    private final PropertyValueSerializer valueSerializer;

    public PropertyValueSerializerHectorSerializerAdapter(PropertyValueSerializer valueSerializer) {
        this.valueSerializer = valueSerializer;
    }

    @Override
    public ByteBuffer toByteBuffer(Object obj) {
        return valueSerializer.toByteBuffer(obj);
    }

    @Override
    public Object fromByteBuffer(ByteBuffer byteBuffer) {
        return valueSerializer.fromByteBuffer(byteBuffer);
    }

}
