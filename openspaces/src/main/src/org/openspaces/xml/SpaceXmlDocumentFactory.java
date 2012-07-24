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
package org.openspaces.xml;

import org.openspaces.core.GigaSpace;

import com.gigaspaces.document.SpaceXmlDocument;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;
import com.gigaspaces.metadata.index.SpaceIndexType;

/**
 * A factory for creating {@link SpaceXmlDocument} instances.
 * 
 * @author idan
 * @since 9.1
 *
 */
public class SpaceXmlDocumentFactory {

    private final GigaSpace gigaSpace;

    /**
     * @param gigaSpace
     */
    public SpaceXmlDocumentFactory(GigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
    }

    /**
     * @param xml
     * @return
     */
    public SpaceXmlDocument newXmlDocument(String xml) {
        return newXmlDocument(xml, null);
    }
    
    /**
     * 
     * @param xml
     * @param label
     * @return
     */
    public SpaceXmlDocument newXmlDocument(String xml, String label) {
        final SpaceXmlDocument document = new SpaceXmlDocument(xml, label);
        if (gigaSpace.getTypeManager().getTypeDescriptor(document.getTypeName()) == null)
            gigaSpace.getTypeManager().registerTypeDescriptor(createTypeDescriptor(document));
        return document;
    }
    
    private SpaceTypeDescriptor createTypeDescriptor(SpaceXmlDocument document)
    {
        return new SpaceTypeDescriptorBuilder(document.getTypeName())
            .idProperty(SpaceXmlDocument.ID_PROPERTY_NAME, true)
            .addFixedProperty(SpaceXmlDocument.XML_PROPERTY_NAME, String.class)
            .addFixedProperty(SpaceXmlDocument.LABEL_PROPERTY_NAME, String.class)
            .addPropertyIndex(SpaceXmlDocument.LABEL_PROPERTY_NAME, SpaceIndexType.BASIC)
            .create();
    }


}
