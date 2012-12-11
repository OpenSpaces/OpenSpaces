/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.itest.archive.wrong;

import org.openspaces.archive.Archive;
import org.openspaces.archive.ArchiveOperationHandler;
import org.openspaces.events.EventTemplate;
import org.openspaces.itest.events.pojos.MockPojoFifoGrouping;
import org.springframework.transaction.annotation.Transactional;

/**
 * Archives {@link MockPojoFifoGrouping} to the {@link ArchiveOperationHandler}
 * 
 * Tests a wrong archiveHandler attribute of the @Archive annotation
 * @author Itai Frenkel
 * @since 9.1.1
 * 
 */
@Archive(batchSize=2, archiveHandler="WRONG-mockArchiveHandler")
@Transactional
public class MockArchiveContainerWrongHandler {
    
    @EventTemplate
    MockPojoFifoGrouping getTemplate() {
        MockPojoFifoGrouping template = new MockPojoFifoGrouping();
        template.setProcessed(false);
        return template;
    }
}