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
package org.openspaces.admin.alert;

import org.openspaces.admin.AdminException;
import org.openspaces.admin.bean.BeanConfigException;

/**
 * An alert configuration exception; the {@link #getCause()} can be one the subclasses of
 * {@link BeanConfigException}.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class AlertConfigurationException extends AdminException {

    private static final long serialVersionUID = 1L;

    public AlertConfigurationException(String message) {
        super(message);
    }

    public AlertConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }


}
