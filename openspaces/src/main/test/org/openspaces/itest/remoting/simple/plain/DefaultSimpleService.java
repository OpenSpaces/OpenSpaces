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

import org.openspaces.remoting.RemotingService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author kimchy
 */
@RemotingService
public class DefaultSimpleService implements SimpleService {

    public String say(String message) {
        return "SAY " + message;
    }

    public Future asyncSay(String message) {
        return null;
    }

    public boolean wire(WiredParameter wiredParameter) {
        return wiredParameter.gigaSpace != null;
    }

    public void testException() throws MyException {
        throw new MyException();
    }

    public Future asyncTestException() throws MyException {
        return null;
    }

    public String overloaded(List list) {
        return "L" + list.size();
    }

    public String overloaded(Map map) {
        return "M" + map.size();
    }
    
    public Future<String> asyncOverloaded(List list) {
        return null;
    }

    public Future<String> asyncOverloaded(Map map) {
        return null;
    }

    public String superSay(String message) {
        return "Super SAY " + message;
    }

}
