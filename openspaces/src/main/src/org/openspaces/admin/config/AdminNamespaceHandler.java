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
package org.openspaces.admin.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * A spring namespace handler for the "admin" namespace.
 * 
 * @author itaif
 * @since 9.0.1
 *
 */
public class AdminNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        //TODO: registerBeanDefinitionParser("targets", new GatewayTargetsBeanDefinitionParser());
    }

}
