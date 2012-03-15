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
package org.openspaces.itest.remoting.broadcast;

import java.util.concurrent.Future;

/**
 * @author kimchy
 */
public interface SimpleService {

    int sum(int value);

    Future<Integer> asyncSum(int value);

    void testException() throws MyException;

    Future asyncTestException() throws MyException;

    class MyException extends RuntimeException {

        private static final long serialVersionUID = 5612556672198167469L;

    }
}
