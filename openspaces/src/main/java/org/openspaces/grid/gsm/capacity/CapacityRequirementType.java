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
package org.openspaces.grid.gsm.capacity;

import java.lang.reflect.InvocationTargetException;

import org.openspaces.admin.AdminException;

public class CapacityRequirementType<T extends CapacityRequirement> {

    private final Class<? extends T> capacityRequirementClass;
    private final String firstConstructorArgument;
    
    CapacityRequirementType(Class<? extends T> capacityRequirementClass) {
        this(capacityRequirementClass,null);
    }
    
    CapacityRequirementType(Class<? extends T> capacityRequirementClass, String constructorArgument) {
        this.capacityRequirementClass = capacityRequirementClass;
        this.firstConstructorArgument =constructorArgument;
    }
    
    public boolean equals(Object other) {
        return 
            other instanceof CapacityRequirementType<?> &&
            capacityRequirementClass.equals(((CapacityRequirementType<?>)other).capacityRequirementClass) && 
            (
             (firstConstructorArgument == null && 
             ((CapacityRequirementType<?>)other).firstConstructorArgument == null) ||
             
             (firstConstructorArgument != null && 
              ((CapacityRequirementType<?>)other).firstConstructorArgument != null &&
             firstConstructorArgument.equals(((CapacityRequirementType<?>)other).firstConstructorArgument))
            );
    }

    public T newInstance() {
        if (firstConstructorArgument == null) {
            String errMessage = "Cannot construct an empty " + capacityRequirementClass.getName();
            try {
                return capacityRequirementClass.newInstance();
            } catch (InstantiationException e) {
                throw new AdminException(errMessage,e);
            } catch (IllegalAccessException e) {
                throw new AdminException(errMessage,e);
            }
        }
        else {
            String errMessage = "Cannot construct " + capacityRequirementClass.getName() + " with argument " + firstConstructorArgument;
            try {
                return capacityRequirementClass.getConstructor(String.class).newInstance(firstConstructorArgument);
            } catch (InstantiationException e) {
                throw new AdminException(errMessage,e);
            } catch (IllegalAccessException e) {
                throw new AdminException(errMessage,e);
            } catch (IllegalArgumentException e) {
                throw new AdminException(errMessage,e);
            } catch (SecurityException e) {
                throw new AdminException(errMessage,e);
            } catch (InvocationTargetException e) {
                throw new AdminException(errMessage,e);
            } catch (NoSuchMethodException e) {
                throw new AdminException(errMessage,e);
            }
        }
    }
    
    public T newInstance(Object value) {
        if (firstConstructorArgument == null) {
            String errMessage = "Cannot construct " + capacityRequirementClass.getName() + " with (" + value.getClass()+")"+value;
            try {
                return capacityRequirementClass.getConstructor(value.getClass()).newInstance(value);
            } catch (InstantiationException e) {
                throw new AdminException(errMessage,e);
            } catch (IllegalAccessException e) {
                throw new AdminException(errMessage,e);
            } catch (IllegalArgumentException e) {
                throw new AdminException(errMessage,e);
            } catch (SecurityException e) {
                throw new AdminException(errMessage,e);
            } catch (InvocationTargetException e) {
                throw new AdminException(errMessage,e);
            } catch (NoSuchMethodException e) {
                throw new AdminException(errMessage,e);
            }
        }
        else {
            String errMessage = "Cannot construct " + capacityRequirementClass.getName() + " with arguments (String)" + firstConstructorArgument + ",(" + value.getClass()+")"+ value;
            try {
                return capacityRequirementClass.getConstructor(String.class, value.getClass()).newInstance(firstConstructorArgument,value);
            } catch (InstantiationException e) {
                throw new AdminException(errMessage,e);
            } catch (IllegalAccessException e) {
                throw new AdminException(errMessage,e);
            } catch (IllegalArgumentException e) {
                throw new AdminException(errMessage,e);
            } catch (SecurityException e) {
                throw new AdminException(errMessage,e);
            } catch (InvocationTargetException e) {
                throw new AdminException(errMessage,e);
            } catch (NoSuchMethodException e) {
                throw new AdminException(errMessage,e);
            }
        }
    }
    
    public String toString() {
        String cotrArgument = firstConstructorArgument==null ? "" : firstConstructorArgument;
        String className = capacityRequirementClass.getName();
        if (className == null) {
            throw new IllegalStateException("className cannot be null");
        }
        return className+"("+ cotrArgument +")";
    }
}
