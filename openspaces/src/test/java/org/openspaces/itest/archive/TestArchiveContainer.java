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
import org.openspaces.archive.ArchivePollingContainer;
import org.openspaces.archive.ArchivePollingContainerConfigurer;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.core.transaction.manager.AbstractJiniTransactionManager;
import org.openspaces.core.transaction.manager.DistributedJiniTxManagerConfigurer;
import org.openspaces.events.DynamicEventTemplate;
import org.openspaces.events.DynamicEventTemplateProvider;
import org.openspaces.events.support.AnnotationProcessorUtils;
import org.openspaces.events.support.EventContainersBus;
import org.openspaces.itest.events.pojos.MockPojo;
import org.openspaces.itest.utils.TestUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;

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
    private final String TEST_ANNOTATION_WRONG_XML = "/org/openspaces/itest/archive/wrong/test-wrong-archive-annotation.xml";
    
    private final String TEST_DYNAMIC_RAW_XML = "/org/openspaces/itest/archive/dynamictemplate/test-dynamic-archive-raw.xml";
    private final String TEST_DYNAMIC_NAMESPACE_XML = "/org/openspaces/itest/archive/dynamictemplate/test-dynamic-archive-namespace.xml";
    private final String TEST_DYNAMIC_ANNOTATION_XML = "/org/openspaces/itest/archive/dynamictemplate/test-dynamic-archive-annotation.xml";
    
    /**
     * Tests archiver with raw spring bean xml
     */
    @Test
    public void testXmlRaw() throws InterruptedException {
        final int expectedBatchSize = 1;
        xmlTest(TEST_RAW_XML, expectedBatchSize);
    }

    /**
     * Tests archiver with namespace spring bean xml
     */
    @Test 
    public void testXmlNamespace() throws InterruptedException {
        //see batch-size="2" in xml file
        final int expectedBatchSize = 2;
        xmlTest(TEST_NAMESPACE_XML , expectedBatchSize); 
    }
    
    /**
     * Tests archiver with mostly annotations such as @Archive (minimal xml)
     */
    @Test 
    public void testXmlAnnotation() throws InterruptedException {
        // see @Archive(batchSize=2) annotation and atomic=true in xml file
        int expectedBatchSize = 2; 
        xmlTest(TEST_ANNOTATION_XML , expectedBatchSize); 
    }
  
    /**
     * Tests archiver with mostly annotations such as @Archive (minimal xml)
     */
    @Test(expected=BeanCreationException.class)
    public void testXmlWrongAnnotationAttribute() throws InterruptedException {
        int expectedBatchSize = 2; 
        xmlTest(TEST_ANNOTATION_WRONG_XML , expectedBatchSize); 
    }
    
    /**
     * Tests archiver with mostly annotations such as @Archive (minimal xml)
     */
    @Test 
    public void testXmlDynamicTemplateAnnotation() throws InterruptedException {
        int expectedBatchSize = 1;
        xmlTest(TEST_DYNAMIC_ANNOTATION_XML, expectedBatchSize); 
    }

    
    /**
     * Tests archiver with mostly annotations such as @Archive (minimal xml)
     */
    @Test 
    public void testXmlDynamicTemplateNamespace() throws InterruptedException {
        final int expectedBatchSize = 1;
        xmlTest(TEST_DYNAMIC_NAMESPACE_XML, expectedBatchSize); 
    }
    
    /**
     * Tests archiver with mostly annotations such as @Archive (minimal xml)
     */
    @Test 
    public void testXmlDynamicTemplateRaw() throws InterruptedException {
        final int expectedBatchSize = 1;
        xmlTest(TEST_DYNAMIC_RAW_XML , expectedBatchSize); 
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
     * Tests archiver with atomic archive handler with default batchSize
     */
    @Test
    public void testConfigurerAtomic() throws Exception {
        boolean atomic = true;
        configurerTest(atomic, TemplateToTest.TEMPLATE);
    }

    /**
     * Tests archiver with atomic archive handler with batchSize
     */
    @Test
    public void testConfigurerAtomicBatchSize() throws Exception {
        boolean atomic = true;
        int batchSize = 2;
        configurerTest(atomic, TemplateToTest.TEMPLATE, batchSize);
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
        Integer batchSize = null;
        configurerTest(atomic, templateToTest, batchSize);
    }
    
    private void configurerTest(boolean atomic, TemplateToTest templateToTest, Integer batchSize) throws Exception {
        final UrlSpaceConfigurer urlSpaceConfigurer = new UrlSpaceConfigurer("/./space");
        try {
            PlatformTransactionManager transactionManager = null;
            if (atomic) {
                transactionManager = new DistributedJiniTxManagerConfigurer().transactionManager();
                if (transactionManager instanceof AbstractJiniTransactionManager) {
                    //used later to verify that container has a tx manager.
                    ((AbstractJiniTransactionManager)transactionManager).setBeanName("tx-manager");
                }
            }
            
            final IJSpace space = urlSpaceConfigurer.create();
            final GigaSpace gigaSpace = new GigaSpaceConfigurer(space).transactionManager(transactionManager).create();
                        
            final MockArchiveOperationsHandler archiveHandler = new MockArchiveOperationsHandler();
            archiveHandler.setAtomicArchiveOfMultipleObjects(atomic);

            ArchivePollingContainerConfigurer containerConfigurer = 
                    new ArchivePollingContainerConfigurer(gigaSpace)           
                    .archiveHandler(archiveHandler);
            configureTemplate(containerConfigurer, templateToTest);

            if (atomic) {
                containerConfigurer.transactionManager(transactionManager);
            }
            
            if (atomic && batchSize != null) {
                containerConfigurer.batchSize(batchSize);
            }
            
            // autostart is enabled by default
            ArchivePollingContainer container = containerConfigurer.create();
            
            int expectedBatchSize;
            if (atomic && batchSize != null) {
                expectedBatchSize = batchSize;
            }
            else if (atomic && batchSize == null) {
                expectedBatchSize = 50; //the default both in the annotation and in the archive container.
            }
            else {
                expectedBatchSize = 1;
            }
            test(archiveHandler, gigaSpace, container, expectedBatchSize);
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

    private void xmlTest(String relativeXmlName, int expectedBatchSize) throws InterruptedException {

        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(relativeXmlName);
        try {
            final MockArchiveOperationsHandler archiveHandler = context.getBean(MockArchiveOperationsHandler.class);
            final GigaSpace gigaSpace = context.getBean(org.openspaces.core.GigaSpace.class);
            ArchivePollingContainer container = getArchivePollingContainer(context);
            test(archiveHandler, gigaSpace, container, expectedBatchSize);
        } finally {
            context.close();
        }
    }

    private ArchivePollingContainer getArchivePollingContainer(final ClassPathXmlApplicationContext context) {
        ArchivePollingContainer container;
        try {
            container = context.getBean(ArchivePollingContainer.class);
        }
        catch (final Exception e) {
            final EventContainersBus eventContainersBus = AnnotationProcessorUtils.findBus(context);
            container = (ArchivePollingContainer) eventContainersBus.getEventContainers().iterator().next();
        }
        return container;
    }
    
    private void test(MockArchiveOperationsHandler archiveHandler, GigaSpace gigaSpace, ArchivePollingContainer container, int expectedBatchSize) throws InterruptedException {
          boolean atomic = archiveHandler.supportsBatchArchiving();
          int batchSize = container.getBatchSize();
          int actualBatchSize;
            if (atomic) {
                actualBatchSize = batchSize;
            }
            else {
                Assert.assertEquals("Test configuration error. Cannot expect batchSize!=1 if not atomic, since the implementation uses take and not takeMultiple", 1, expectedBatchSize);
                actualBatchSize = 1;
            }
            
            Assert.assertEquals(expectedBatchSize, actualBatchSize);
            Assert.assertTrue("Atomic test must be performed with a space that uses a transaction manager", !atomic || gigaSpace.getTxProvider() != null);
            Assert.assertTrue("Atomic test must be performed with a container that uses a transaction manager", !atomic || container.getTransactionManagerName() != null);
            
            test(archiveHandler, gigaSpace, actualBatchSize);
    }

    private void test(final MockArchiveOperationsHandler archiveHandler, GigaSpace gigaSpace, final int batchSize)
            throws InterruptedException {

        
        // TODO: Make it transactional by default?
        final MockPojo[] entries = new MockPojo[] { new MockPojo(false, 1), new MockPojo(false, 2), new MockPojo(false, 3), new MockPojo(false, 4), new MockPojo(false, 5) };
        final int numberOfEntries = entries.length;
        gigaSpace.writeMultiple(entries);
        TestUtils.repetitive(new Runnable() {

            @Override
            public void run() {
                Assert.assertEquals(numberOfEntries, countEntries(archiveHandler));
            }

        }, 10000);

        if (batchSize >= 5) {
            // MultiTake uses take() and then takeMultiple() or
            // takeMultiple()
            int count = countOperations(archiveHandler);
            Assert.assertTrue("expected at most 2 count operations (" + count +" > 2)", count <= 2);
        }
        else if (batchSize == 2) {
            // MultiTake uses take() and then takeMultiple (twice) or
            // MultiTake uses takeMultiple (three times) 
            Assert.assertEquals(3, countOperations(archiveHandler));
        }
        else if (batchSize == 1) {
            Assert.assertEquals(numberOfEntries, countOperations(archiveHandler));
        }
        else {
            Assert.fail("unexpected batchSize " + batchSize);
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
