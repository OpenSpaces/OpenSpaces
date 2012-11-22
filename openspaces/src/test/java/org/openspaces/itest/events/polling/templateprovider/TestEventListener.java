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
package org.openspaces.itest.events.polling.templateprovider;

import org.openspaces.core.GigaSpace;
import org.openspaces.events.SpaceDataEventListener;
import org.openspaces.itest.events.pojos.MockPojo;
import org.springframework.transaction.TransactionStatus;

public class TestEventListener implements SpaceDataEventListener<MockPojo> {

    private volatile int messageCounter = 0;

    public void reset() {
        this.messageCounter = 0;
    }

    public void onEvent(MockPojo data, GigaSpace gigaSpace, TransactionStatus txStatus, Object source) {
        messageCounter++;
    }

    public int getMessageCounter() {
        return messageCounter;
    }
}
