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

package org.openspaces.admin.esm;

import org.openspaces.admin.AgentGridComponent;
import org.openspaces.admin.LogProviderGridComponent;
import org.openspaces.admin.dump.DumpProvider;

/**
 * The base Elastic Service Manager interface for deploying an elastic data-grid service. 
 * 
 * <blockquote>
 * <pre>
 * <b>Disclaimer:</b> This interface and the elastic data grid functionality is provided as a technology preview in XAP 7.1. 
 * As such, it is subject to API and behavior changes in the next XAP releases without going through the usual deprecation process 
 * of the XAP API.
 * </pre>
 * </blockquote>
 * 
 * @author Moran Avigdor
 */
public interface ElasticServiceManager extends AgentGridComponent, LogProviderGridComponent, DumpProvider {

   
}
