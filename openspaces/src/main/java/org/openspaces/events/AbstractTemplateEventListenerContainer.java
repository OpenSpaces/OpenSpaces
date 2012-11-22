/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.events;

import org.openspaces.events.support.AnnotationProcessorUtils;
import org.springframework.dao.DataAccessException;

/**
 * A simple base class that provides support methods for Template based event listeners.
 *
 * <p>There are several ways a template can be provided. The first is by explicitly setting it using
 * {@link #setTemplate(Object)}. The second option is for the event listener to implement an interface
 * called {@link org.openspaces.events.EventTemplateProvider}. The last option is to annotate a method
 * within the event listener that will return the actual template using {@link org.openspaces.events.EventTemplate}
 * annotation.
 *
 * @author kimchy
 */
public abstract class AbstractTemplateEventListenerContainer extends AbstractEventListenerContainer {

    private Object template;

    private boolean performSnapshot = true; // enabled by default

    private Object receiveTemplate;

    private DynamicEventTemplateProvider dynamicTemplate;
    private Object dynamicTemplateRef;

    @Override
    public void initialize() throws DataAccessException {
        
        Object possibleTemplateProvider = null;
        
        if (template != null) {
            // check if template object is actually a template provider
            possibleTemplateProvider = template;
        }
        else {
            Class<?> eventListenerType = getEventListenerClass();
            if (eventListenerType != null) {
                //check if listener object is also a template provider
                possibleTemplateProvider = getActualEventListener();
            }
        }
        
        if (possibleTemplateProvider != null) {
            Object templateFromProvider = AnnotationProcessorUtils.findTemplateFromProvider(possibleTemplateProvider);
            if (templateFromProvider != null) {
                setTemplate(templateFromProvider);
            }
        }
        
        if (dynamicTemplate == null && dynamicTemplateRef != null) {
            Object dynamicTemplateProviderBean = dynamicTemplateRef;
            dynamicTemplate = AnnotationProcessorUtils.findDynamicEventTemplateProvider(dynamicTemplateProviderBean);
            if (dynamicTemplate == null) {
                throw new IllegalArgumentException("Cannot find dynamic template provider in " + dynamicTemplateRef.getClass());
            }
        }
        
        if (template != null && dynamicTemplate != null) {
            throw new IllegalArgumentException("dynamicTemplate and template are mutually exclusive.");
        }

        if (performSnapshot && template != null) {
            if (logger.isTraceEnabled()) {
                logger.trace(message("Performing snapshot on template [" + template + "]"));
            }
            receiveTemplate = getGigaSpace().snapshot(template);
        } else {
            receiveTemplate = template;
        }

        super.initialize();
    }

    /**
     * Sets the specified template to be used with the polling space operation.
     *
     * @see org.openspaces.core.GigaSpace#take(Object,long)
     */
    public void setTemplate(Object template) {
        this.template = template;
    }
    
    /**
     * Returns the template that will be used. Note, in order to perform receive operations, the
     * {@link #getReceiveTemplate()} should be used.
     */
    protected Object getTemplate() {
        return this.template;
    }
    
    /**
     * Returns whether dynamic template is configured
     */
    protected boolean isDynamicTemplate(){
        return dynamicTemplate != null;
    }

    /**
     * Returns the template to be used for receive operations. If
     * {@link #setPerformSnapshot(boolean)} is set to <code>true</code> (the default)
     * will return the snapshot of the provided template.
     */
    protected Object getReceiveTemplate() {
        
        if (dynamicTemplate != null) {
            return dynamicTemplate.getDynamicTemplate();
        }
        
        return receiveTemplate;
    }

    /**
     * If set to <code>true</code> will perform snapshot operation on the provided template
     * before invoking registering as an event listener.
     *
     * @see org.openspaces.core.GigaSpace#snapshot(Object)
     */
    public void setPerformSnapshot(boolean performSnapshot) {
        this.performSnapshot = performSnapshot;
    }

    protected boolean isPerformSnapshot() {
        return this.performSnapshot;
    }
    
    /**
     * Called before each take and read polling operation to change the template
     * Overrides any template defined with {@link #setTemplate(Object)} 
     * @param templateProvider - 
     *      An object that implements {@link DynamicEventTemplateProvider} 
     *      or has a method annotated with {@link DynamicEventTemplateProvider}   
     */
    public void setDynamicTemplate(Object dynamicTemplate) {
        this.dynamicTemplateRef = dynamicTemplate;
    }
}
