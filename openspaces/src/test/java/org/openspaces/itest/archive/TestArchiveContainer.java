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
package org.openspaces.itest.archive;

import junit.framework.Assert;

import org.junit.Test;
import org.openspaces.archive.ArchivePollingContainerConfigurer;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.itest.events.pojos.MockPojo;
import org.openspaces.itest.utils.TestUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.j_spaces.core.IJSpace;

/**
 * GS-10785 Test Archive Container
 * 
 * In order to run this test in eclise, edit the JUnit Test Configuration: Click the ClassPath tab,
 * click User Entries. Add Folders: Gigaspaces/ and Gigaspaces/src/java/
 * 
 * @author Itai Frenkel
 * @since 9.1.1
 */
public class TestArchiveContainer {

    private final String TEST_RAW_XML = "/org/openspaces/itest/archive/test-archive-raw.xml";
    private final String TEST_NAMESPACE_XML = "/org/openspaces/itest/archive/test-archive-namespace.xml";
    private final String TEST_ANNOTATION_XML = "/org/openspaces/itest/archive/test-archive-annotation.xml";

    /**
     * Tests archiver with raw spring bean xml
     */
    @Test
    public void testRawXml() throws InterruptedException {
        xmlTest(TEST_RAW_XML);
    }

    /**
     * Tests archiver with namespace spring bean xml
     */
    @Test 
    public void testNamespaceXml() throws InterruptedException {
        xmlTest(TEST_NAMESPACE_XML); 
    }
    
    /**
     * Tests archiver with mostly annotations such as @Archive (minimal xml)
     */
    @Test 
    public void testAnnotationXml() throws InterruptedException {
        xmlTest(TEST_ANNOTATION_XML); 
    }
  

    /**
     * Tests archiver with configurer (no Spring beans)
     */
    @Test
    public void testConfigurer() throws Exception {
        boolean atomic = false;
        configurerTest(atomic);
    }

    /**
     * Tests archiver with atomic archive handler
     */
    @Test
    public void testConfigurerAtomic() throws Exception {
        boolean atomic = true;
        configurerTest(atomic);
    }

    private void configurerTest(boolean atomic) throws Exception {
        final UrlSpaceConfigurer urlSpaceConfigurer = new UrlSpaceConfigurer("/./space");
        try {
            final IJSpace space = urlSpaceConfigurer.create();
            final GigaSpace gigaSpace = new GigaSpaceConfigurer(space).create();

            final MockArchiveOperationsHandler archiveHandler = new MockArchiveOperationsHandler();
            archiveHandler.setAtomicArchiveOfMultipleObjects(atomic);

            final MockPojo template = new MockPojo();
            template.setProcessed(false);

            // autostart is enabled by default
            new ArchivePollingContainerConfigurer(gigaSpace)           
            .archiveHandler(archiveHandler).template(template).create();

            test(archiveHandler, gigaSpace, atomic);
        } finally {
            urlSpaceConfigurer.destroy();
        }
    }

    private void xmlTest(String relativeXmlName) throws InterruptedException {

        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(relativeXmlName);
        try {
            final MockArchiveOperationsHandler archiveHandler = context.getBean(MockArchiveOperationsHandler.class);
            final GigaSpace gigaSpace = context.getBean(org.openspaces.core.GigaSpace.class);
            boolean atomic = false;
            test(archiveHandler, gigaSpace, atomic);
        } finally {
            context.close();
        }
    }

    private void test(final MockArchiveOperationsHandler archiveHandler, GigaSpace gigaSpace, final boolean atomic)
            throws InterruptedException {

        // TODO: Make it transactional by default?
        final MockPojo[] entries = new MockPojo[] { new MockPojo(false, 1), new MockPojo(false, 2), new MockPojo(false, 3), new MockPojo(false, 4)  };
        final int numberOfEntries = entries.length;
        gigaSpace.writeMultiple(entries);
        TestUtils.repetitive(new Runnable() {

            @Override
            public void run() {
                Assert.assertEquals(numberOfEntries, countEntries(archiveHandler));
            }

        }, 10000);

        if (atomic) {
            // MultiTake uses take() and then takeMultiple
            Assert.assertTrue(countOperations(archiveHandler) <= 2);
        } else {
            Assert.assertEquals(numberOfEntries, countOperations(archiveHandler));
        }
    }

    private int countOperations(final MockArchiveOperationsHandler archiveHandler) {
        return archiveHandler.getArchivedObjectsOperations().size();
    }

    private int countEntries(final MockArchiveOperationsHandler archiveHandler) {
        int numberOfArchivedEntries = 0;
        for (final Object[] objects : archiveHandler.getArchivedObjectsOperations()) {
            numberOfArchivedEntries += objects.length;
        }
        return numberOfArchivedEntries;
    }
}
