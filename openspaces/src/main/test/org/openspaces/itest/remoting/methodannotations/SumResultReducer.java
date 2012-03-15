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
package org.openspaces.itest.remoting.methodannotations;

import org.openspaces.remoting.RemoteResultReducer;
import org.openspaces.remoting.SpaceRemotingInvocation;
import org.openspaces.remoting.SpaceRemotingResult;

/**
 * @author uri
 */
public class SumResultReducer implements RemoteResultReducer<Integer, Integer>{
    public Integer reduce(SpaceRemotingResult<Integer>[] spaceRemotingResults, SpaceRemotingInvocation remotingInvocation) throws Exception {
        int sum = 0;
        for (SpaceRemotingResult<Integer> spaceRemotingResult : spaceRemotingResults) {
            Throwable exception = spaceRemotingResult.getException();
            if (exception != null) {
                throw new RuntimeException(exception);
            }
            Integer result = spaceRemotingResult.getResult();
            if (result != null) {
                sum += result;
            }
        }
        return sum;
    }
}
