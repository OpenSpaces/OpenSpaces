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
package org.openspaces.itest.remoting.simple.plain;

import org.openspaces.remoting.AutowireArguments;
import org.openspaces.remoting.Routing;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author kimchy
 */
@AutowireArguments
public interface SimpleService extends SuperSimpleService{

    String say(@Routing String message);

    Future<String> asyncSay(String message);

    boolean wire(WiredParameter wiredParameter);

    void testException() throws MyException;

    Future asyncTestException() throws MyException;

    String overloaded(List list);

    Future<String> asyncOverloaded(List list);

    String overloaded(Map map);

    Future<String> asyncOverloaded(Map map);

    class MyException extends RuntimeException {

        private static final long serialVersionUID = 4286999982167551193L;

    }
}
