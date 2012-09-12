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
package org.openspaces.admin.alert.config.parser;

import org.openspaces.admin.AdminException;

/**
 * An exception thrown on a parsing error from {@link AlertConfigurationParser}.
 * @author Moran Avigdor
 * @since 8.0
 */
public class AlertConfigurationParserException extends AdminException {

    private static final long serialVersionUID = 1L;

    public AlertConfigurationParserException(String message) {
        super(message);
    }
    
    public AlertConfigurationParserException(String message, Throwable cause) {
        super(message, cause);
    }

}
