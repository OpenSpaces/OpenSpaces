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

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.template.ColumnFamilyResult;
import me.prettyprint.cassandra.service.template.ColumnFamilyRowMapper;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;

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

    private static final String SPACE_DOCUMENT_NAME_KEY = "Name";

	private static final String SPACEDOCUMENT_NAME = "Anvil";

	private static final String SPACEDOCUMENT_TYPENAME = "Product";

	private static final String SPACEDOCUMENT_ID = "hw-1234";

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
			registerTypeDescriptor(gigaSpace);
		}
		
		archiveHandler.archive(createSpaceDocument());
		
		verifyDocumentInCassandra();
    }

	private SpaceDocument createSpaceDocument() {
		final Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("CatalogNumber", SPACEDOCUMENT_ID);
		properties.put("Category", "Hardware");
		properties.put(SPACE_DOCUMENT_NAME_KEY, SPACEDOCUMENT_NAME);
		properties.put("Price", 9.99f);
		final SpaceDocument document = new SpaceDocument(SPACEDOCUMENT_TYPENAME, properties);
		return document;
	}

	private void registerTypeDescriptor(GigaSpace gigaSpace) {
		final SpaceTypeDescriptor typeDescriptor = 
				new SpaceTypeDescriptorBuilder(SPACEDOCUMENT_TYPENAME)
				.idProperty("CatalogNumber")
				.routingProperty("Category")
				.addPropertyIndex(SPACE_DOCUMENT_NAME_KEY, SpaceIndexType.BASIC)
				.addPropertyIndex("Price", SpaceIndexType.EXTENDED)
				.create();
		gigaSpace.getTypeManager().registerTypeDescriptor(typeDescriptor);
	}

	private void verifyDocumentInCassandra() {
		
		Cluster cluster = HFactory.getOrCreateCluster("test-localhost_"+server.getPort(), server.getHost()+ ":" + server.getPort());
		Keyspace keyspace = HFactory.createKeyspace(server.getKeySpaceName(), cluster);
		
		String columnFamilyName = SPACEDOCUMENT_TYPENAME; // as long as shorter than 40 bytes
		ThriftColumnFamilyTemplate<String, String> template = new ThriftColumnFamilyTemplate<String, String>(
				keyspace,
				columnFamilyName,
                StringSerializer.get(),
                StringSerializer.get());
		
		Assert.assertTrue(SPACEDOCUMENT_TYPENAME + " does not exist",template.isColumnsExist(SPACEDOCUMENT_ID));

		ColumnFamilyRowMapper<String, String, Object> mapper = new ColumnFamilyRowMapper<String, String, Object>() {
			@Override
		    public String mapRow(ColumnFamilyResult<String, String> rs) {
				
				for (String columnName : rs.getColumnNames()) {
		            ByteBuffer bytes = rs.getColumn(columnName).getValueBytes();
		            if (columnName.equals(SPACE_DOCUMENT_NAME_KEY)) {
		            	return StringSerializer.get().fromByteBuffer(bytes);
		            }
				}
				
				return "Could not find column " + SPACE_DOCUMENT_NAME_KEY;
		    }
		};
		Object name = template.queryColumns(SPACEDOCUMENT_ID, mapper);
		Assert.assertEquals(SPACEDOCUMENT_NAME,name); 
	}
}

