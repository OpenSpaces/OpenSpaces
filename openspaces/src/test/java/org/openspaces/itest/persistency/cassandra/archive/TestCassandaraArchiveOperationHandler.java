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
package org.openspaces.itest.persistency.cassandra.archive;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.itest.persistency.cassandra.CassandraTestServer;
import org.openspaces.persistency.cassandra.archive.CassandraArchiveOperationHandler;
import org.openspaces.persistency.cassandra.archive.CassandraArchiveOperationHandlerConfigurer;
import org.openspaces.persistency.cassandra.error.SpaceCassandraException;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;
import com.gigaspaces.metadata.index.SpaceIndexType;
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
public class TestCassandaraArchiveOperationHandler {

    private final Log logger = LogFactory.getLog(this.getClass());
    
    private final String TEST_NAMESPACE_XML = "/org/openspaces/itest/persistency/cassandra/archive/test-cassandra-archive-handler-namespace.xml";
    
    private final CassandraTestServer server = new CassandraTestServer();

	private boolean skipRegisterTypeDescriptor;
    
    @Before
    public void startServer() {
    	server.initialize(false);
    }
    
    @After
    public void stopServer() {
    	server.destroy();
    }
    
    /**
     * Tests archiver with namespace spring bean xml
     */
    @Test 
    public void testXmlNamespace() {
        xmlTest(TEST_NAMESPACE_XML); 
    }
    
    @Test
    public void testConfigurer() throws Exception {
    	configurerTest();
    }

    @Test(expected = SpaceCassandraException.class)
    public void testNoTypeDescriptorInSpace() throws Exception {
    	skipRegisterTypeDescriptor = true;
    	configurerTest();
    }

	private void configurerTest() throws Exception {
		final UrlSpaceConfigurer urlSpaceConfigurer = new UrlSpaceConfigurer("/./space");
    	
    	CassandraArchiveOperationHandler archiveHandler = null;
    	
        try {
            GigaSpace gigaSpace;
            
            final IJSpace space = urlSpaceConfigurer.create();
            gigaSpace = new GigaSpaceConfigurer(space).create();

            archiveHandler = 
            	new CassandraArchiveOperationHandlerConfigurer()
	            .keyspace(server.getKeySpaceName())
	        	.host(server.getHost())
	        	.port(server.getPort())
	        	.gigaSpace(gigaSpace)
	            .create();
            
            test(archiveHandler, gigaSpace);
        } finally {
            
            if (urlSpaceConfigurer != null) {
                urlSpaceConfigurer.destroy();
            }
            
            if (archiveHandler != null) {
            	archiveHandler.destroy();
            }
        }
	}
    
    private void xmlTest(String relativeXmlName) {

    	final boolean refreshNow = false;
    	final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{relativeXmlName}, refreshNow);
    	
    	PropertyPlaceholderConfigurer propertyConfigurer = new PropertyPlaceholderConfigurer();
    	Properties properties = new Properties();
    	properties.put("cassandra.keyspace", server.getKeySpaceName());
    	properties.put("cassandra.host", server.getHost());
    	properties.put("cassandra.port", ""+server.getPort());
    	propertyConfigurer.setProperties(properties);
    	context.addBeanFactoryPostProcessor(propertyConfigurer);
    	context.refresh();
    	
        try {
            final CassandraArchiveOperationHandler archiveHandler = context.getBean(CassandraArchiveOperationHandler.class);
            final GigaSpace gigaSpace = context.getBean(org.openspaces.core.GigaSpace.class);
            test(archiveHandler, gigaSpace);
        } finally {
            context.close();
        }
    }

    private void test(CassandraArchiveOperationHandler archiveHandler, GigaSpace gigaSpace) {
         
		if (!skipRegisterTypeDescriptor) {
			SpaceTypeDescriptor typeDescriptor = 
					new SpaceTypeDescriptorBuilder("Product")
			.idProperty("CatalogNumber")
			.routingProperty("Category")
			.addPropertyIndex("Name", SpaceIndexType.BASIC)
			.addPropertyIndex("Price", SpaceIndexType.EXTENDED)
			.create();
			gigaSpace.getTypeManager().registerTypeDescriptor(typeDescriptor);
		}
		
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("CatalogNumber", "hw-1234");
		properties.put("Category", "Hardware");
		properties.put("Name", "Anvil");
		properties.put("Price", 9.99f);
		SpaceDocument document = new SpaceDocument("Product", properties);
		archiveHandler.archive(document);
    }
}

