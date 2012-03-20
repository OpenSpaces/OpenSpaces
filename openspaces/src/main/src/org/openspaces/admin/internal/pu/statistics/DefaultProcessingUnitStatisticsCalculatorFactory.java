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
package org.openspaces.admin.internal.pu.statistics;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.openspaces.admin.AdminException;

/**
 * A cached repository of objects that implement {@link InternalProcessingUnitStatisticsCalculator}
 * These objects need to be cached since they are created many times when aggregating statistics
 * and they are needed only once.
 * @author itaif
 * @since 9.0.0
 */
public class DefaultProcessingUnitStatisticsCalculatorFactory {

    private final HashMap<Class<? extends InternalProcessingUnitStatisticsCalculator>,InternalProcessingUnitStatisticsCalculator> cache;
    
    public DefaultProcessingUnitStatisticsCalculatorFactory() {
        cache = new HashMap<Class<? extends InternalProcessingUnitStatisticsCalculator>, InternalProcessingUnitStatisticsCalculator>();
    }
    
    public InternalProcessingUnitStatisticsCalculator create (InternalProcessingUnitStatisticsCalculatorClassProvider classProvider) {
        
        Class<? extends InternalProcessingUnitStatisticsCalculator> clazz = classProvider.getProcessingUnitStatisticsCalculator();
        InternalProcessingUnitStatisticsCalculator calculator = cache.get(clazz);
        if (calculator == null) {
            calculator = newInstance(clazz);
            cache.put(clazz, calculator);
        }
        return calculator;
    }

    private InternalProcessingUnitStatisticsCalculator newInstance(
            Class<? extends InternalProcessingUnitStatisticsCalculator> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (final IllegalArgumentException e) {
            throw new AdminException("Failed to create statistics calculator " + clazz, e);
        } catch (final SecurityException e) {
            throw new AdminException("Failed to create statistics calculator " + clazz, e);
        } catch (final InstantiationException e) {
            throw new AdminException("Failed to create statistics calculator " + clazz, e);
        } catch (final IllegalAccessException e) {
            throw new AdminException("Failed to create statistics calculator " + clazz, e);
        } catch (final InvocationTargetException e) {
            throw new AdminException("Failed to create statistics calculator " + clazz, e);
        } catch (final NoSuchMethodException e) {
            throw new AdminException("Failed to create statistics calculator " + clazz, e);
        }
    }
}
