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

import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.openspaces.archive.ArchivePollingContainerConfigurer;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.events.DynamicEventTemplate;
import org.openspaces.events.DynamicEventTemplateProvider;
import org.openspaces.itest.events.pojos.MockPojo;
import org.openspaces.itest.utils.TestUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.j_spaces.core.IJSpace;

/**
 * GS-10785 Test Archive Container
 * 
 * In order to run this test in eclise, edit the JUnit Test Configuration: Click the ClassPath tab,
 * click User Entries. Add Folders: Gigaspaces/ and Gigaspaces/src/java/resources and openspaces/src/main/java/
 * 
 * @author Itai Frenkel
 * @since 9.1.1
 */
public class TestArchiveContainer {

    private final Log logger = LogFactory.getLog(this.getClass());
    
    private final String TEST_RAW_XML = "/org/openspaces/itest/archive/statictemplate/test-archive-raw.xml";
    private final String TEST_NAMESPACE_XML = "/org/openspaces/itest/archive/statictemplate/test-archive-namespace.xml";
    private final String TEST_ANNOTATION_XML = "/org/openspaces/itest/archive/statictemplate/test-archive-annotation.xml";
    
    private final String TEST_DYNAMIC_RAW_XML = "/org/openspaces/itest/archive/dynamictemplate/test-dynamic-archive-raw.xml";
    private final String TEST_DYNAMIC_NAMESPACE_XML = "/org/openspaces/itest/archive/dynamictemplate/test-dynamic-archive-namespace.xml";
    private final String TEST_DYNAMIC_ANNOTATION_XML = "/org/openspaces/itest/archive/dynamictemplate/test-dynamic-archive-annotation.xml";

    /**
     * Tests archiver with raw spring bean xml
     */
    @Test
    public void testXmlRaw() throws InterruptedException {
        xmlTest(TEST_RAW_XML);
    }

    /**
     * Tests archiver with namespace spring bean xml
     */
    @Test 
    public void testXmlNamespace() throws InterruptedException {
        xmlTest(TEST_NAMESPACE_XML); 
    }
    
    /**
     * Tests archiver with mostly annotations such as @Archive (minimal xml)
     */
    @Test 
    public void testXmlAnnotation() throws InterruptedException {
        xmlTest(TEST_ANNOTATION_XML); 
    }
  
    /**
     * Tests archiver with mostly annotations such as @Archive (minimal xml)
     */
    @Test 
    public void testXmlDynamicTemplateAnnotation() throws InterruptedException {
        xmlTest(TEST_DYNAMIC_ANNOTATION_XML); 
    }

    
    /**
     * Tests archiver with mostly annotations such as @Archive (minimal xml)
     */
    @Test 
    public void testXmlDynamicTemplateNamespace() throws InterruptedException {
        xmlTest(TEST_DYNAMIC_NAMESPACE_XML); 
    }
    
    /**
     * Tests archiver with mostly annotations such as @Archive (minimal xml)
     */
    @Test 
    public void testXmlDynamicTemplateRaw() throws InterruptedException {
        xmlTest(TEST_DYNAMIC_RAW_XML); 
    }
    
    /**
     * Tests archiver with configurer (no Spring beans)
     */
    @Test
    public void testConfigurer() throws Exception {
        boolean atomic = false;
        configurerTest(atomic, TemplateToTest.TEMPLATE);
    }

    /**
     * Tests archiver with atomic archive handler
     */
    @Test
    public void testConfigurerAtomic() throws Exception {
        boolean atomic = true;
        configurerTest(atomic, TemplateToTest.TEMPLATE);
    }

    /**
     * Tests archiver with dynamic template (interface)
     */
    @Test
    public void testConfigurerDynamicTemplateInterface() throws Exception {
        boolean atomic = false;
        configurerTest(atomic, TemplateToTest.DYNAMIC_TEMPLATE_INTERFACE);
    }
    
    /**
     * Tests archiver with dynamic template (annotation)
     */
    @Test
    public void testConfigurerDynamicTemplateAnnotation() throws Exception {
        boolean atomic = false;
        configurerTest(atomic, TemplateToTest.DYNAMIC_TEMPLATE_ANNOTATION);
    }
    
    /**
     * Tests archiver with dynamic template (method)
     */
    @Test
    public void testConfigurerDynamicTemplateMethod() throws Exception {
        boolean atomic = false;
        configurerTest(atomic, TemplateToTest.DYNAMIC_TEMPLATE_METHOD);
    }
    
    enum TemplateToTest {
        TEMPLATE,
        DYNAMIC_TEMPLATE_INTERFACE,
        DYNAMIC_TEMPLATE_ANNOTATION,
        DYNAMIC_TEMPLATE_METHOD
    }
    
    private void configurerTest(boolean atomic, TemplateToTest templateToTest) throws Exception {
        final UrlSpaceConfigurer urlSpaceConfigurer = new UrlSpaceConfigurer("/./space");
        try {
            final IJSpace space = urlSpaceConfigurer.create();
            final GigaSpace gigaSpace = new GigaSpaceConfigurer(space).create();

            final MockArchiveOperationsHandler archiveHandler = new MockArchiveOperationsHandler();
            archiveHandler.setAtomicArchiveOfMultipleObjects(atomic);

            ArchivePollingContainerConfigurer containerConfigurer = 
                    new ArchivePollingContainerConfigurer(gigaSpace)           
                    .archiveHandler(archiveHandler);
            configureTemplate(containerConfigurer, templateToTest);
            
            // autostart is enabled by default
            containerConfigurer.create();

            
            test(archiveHandler, gigaSpace, atomic);
        } finally {
            urlSpaceConfigurer.destroy();
        }
    }

    private void configureTemplate(ArchivePollingContainerConfigurer containerConfigurer, TemplateToTest templateToTest) {
        switch (templateToTest) {
        case TEMPLATE:
            // template is called once
            final MockPojo template = new MockPojo();
            template.setProcessed(false);
            containerConfigurer.template(template);
            break;
            
        case DYNAMIC_TEMPLATE_INTERFACE:
            // dynamic template returns a different template each call
            final DynamicEventTemplateProvider dynamicTemplate = new DynamicEventTemplateProvider() {
                final AtomicInteger routing = new AtomicInteger();
                @Override
                public Object getDynamicTemplate() {
                    final MockPojo template = new MockPojo();
                    template.setProcessed(false);
                    template.setRouting(routing.incrementAndGet());
                    logger.info("getDynamicTemplate() returns " + template);
                    return template;
                }
            };
            containerConfigurer.dynamicTemplate(dynamicTemplate);
            break;
            
        case DYNAMIC_TEMPLATE_ANNOTATION:
            // dynamic template returns a different template each call
            final Object dynamicTemplateAnnotation = new Object() {
                final AtomicInteger routing = new AtomicInteger();
                
                @DynamicEventTemplate
                public Object getDynamicTemplate() {
                    final MockPojo template = new MockPojo();
                    template.setProcessed(false);
                    template.setRouting(routing.incrementAndGet());
                    logger.info("getDynamicTemplate() returns " + template);
                    return template;
                }
            };
            containerConfigurer.dynamicTemplateAnnotation(dynamicTemplateAnnotation);
            break;
            
        case DYNAMIC_TEMPLATE_METHOD:
            // dynamic template returns a different template each call
            final Object dynamicTemplateMethod = new Object() {
                final AtomicInteger routing = new AtomicInteger();
                
                public Object getDynamicTemplateMethod() {
                    final MockPojo template = new MockPojo();
                    template.setProcessed(false);
                    template.setRouting(routing.incrementAndGet());
                    logger.info("getDynamicTemplate() returns " + template);
                    return template;
                }
            };
            containerConfigurer.dynamicTemplateMethod(dynamicTemplateMethod, "getDynamicTemplateMethod");
            break;
            
        default:
            Assert.fail("Unknown template test " + templateToTest);
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
