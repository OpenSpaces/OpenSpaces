/*******************************************************************************
 * 
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
 *  
 ******************************************************************************/
package org.openspaces.enhancer.support;

import org.openspaces.libraries.asm.Type;

/**
 * @author kimchy
 */
public abstract class CommonTypes {

    public final static Type BYTE_TYPE = Type.getObjectType("java/lang/Byte");

    public final static Type BOOLEAN_TYPE = Type.getObjectType("java/lang/Boolean");

    public final static Type SHORT_TYPE = Type.getObjectType("java/lang/Short");

    public final static Type CHARACTER_TYPE = Type.getObjectType("java/lang/Character");

    public final static Type INTEGER_TYPE = Type.getObjectType("java/lang/Integer");

    public final static Type FLOAT_TYPE = Type.getObjectType("java/lang/Float");

    public final static Type LONG_TYPE = Type.getObjectType("java/lang/Long");

    public final static Type DOUBLE_TYPE = Type.getObjectType("java/lang/Double");

    public final static Type NUMBER_TYPE = Type.getObjectType("java/lang/Number");

    public final static Type STRING_TYPE = Type.getObjectType("java/lang/String");

    public final static Type OBJECT_TYPE = Type.getObjectType("java/lang/Object");

    public final static Type BIG_DECIMAL_TYPE = Type.getObjectType("java/math/BigDecimal");

    public final static Type OBJECT_INPUT_TYPE = Type.getObjectType("java/io/ObjectInput");

    public final static Type OBJECT_OUTPUT_TYPE = Type.getObjectType("java/io/ObjectOutput");

    public final static Type OBJECT_INPUT_STEAM_TYPE = Type.getObjectType("java/io/ObjectInputStream");

    public final static Type OBJECT_OUTPUT_STREAM_TYPE = Type.getObjectType("java/io/ObjectOutputStream");

    public final static Type BINARY_ARRAY = Type.getType(byte[].class);
}
