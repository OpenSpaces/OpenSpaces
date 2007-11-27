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

package org.openspaces.remoting.scripting;

import java.util.Map;

/**
 * A script that will be excuted.
 *
 * @author kimchy
 */
public interface Script {

    /**
     * The name of the script. Should uniquely identify scripts.
     */
    String getName();

    /**
     * The type of the script (or actually, language). For example, <code>groovy</code> or <code>ruby</code>.
     */
    String getType();

    /**
     * The script source as a string.
     */
    String getScriptAsString();

    /**
     * One or more parameters that will be passes to the script.
     */
    Map<String, Object> getParameters();
}
