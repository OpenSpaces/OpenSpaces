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
package org.openspaces.admin.pu.elastic.config;

import java.util.Map;

import org.openspaces.core.util.StringProperties;

/**
 * Base class for Statistics configurations
 * @author itaif
 * @since 9.0.0
 */
public abstract class AbstractStatisticsConfig {

    private StringProperties properties;

    protected AbstractStatisticsConfig(Map<String,String> properties) {
        this.setProperties(properties);
    }

    protected StringProperties getStringProperties() {
        return this.properties;
    }
    
    public Map<String, String> getProperties() {
        return properties.getProperties();
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = new StringProperties(properties);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractStatisticsConfig other = (AbstractStatisticsConfig) obj;
        if (properties == null) {
            if (other.properties != null)
                return false;
        } else if (!properties.equals(other.properties))
            return false;
        return true;
    }

    
    @Override
    public String toString() {
        return getSimpleName() + " " + properties;
    }

    private String getSimpleName() {
        final String clazzSimpleName = this.getClass().getSimpleName().replace("Config", "");
        final char[] charArray = clazzSimpleName.toCharArray();
        charArray[0] = Character.toLowerCase(charArray[0]);
        final String simpleName = new String(charArray);
        return simpleName;
    }
}
