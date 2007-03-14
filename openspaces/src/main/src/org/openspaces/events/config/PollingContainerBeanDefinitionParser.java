package org.openspaces.events.config;

import org.openspaces.events.polling.SimplePollingEventListenerContainer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author kimchy
 */
public class PollingContainerBeanDefinitionParser extends AbstarctTxEventContainerBeanDefinitionParser {

    private static final String TEMPLATE = "template";

    private static final String SQL_QUERY = "sql-query";

    private static final String RECEIVE_OPERATION_HANDLER = "receive-operation-handler";

    private static final String TRIGGER_OPERATION_HANDLER = "trigger-operation-handler";

    private static final String RECEIVE_TIMEOUT = "receive-timeout";

    private static final String RECOVERY_INTERVAL = "recovery-interval";

    private static final String CONCURRENT_CONSUMERS = "concurrent-consumers";

    private static final String MAX_CONCURRENT_CONSUMERS = "max-concurrent-consumers";

    private static final String IDLE_TASK_EXECUTION_LIMIT = "idle-task-execution-limit";

    protected Class<SimplePollingEventListenerContainer> getBeanClass(Element element) {
        return SimplePollingEventListenerContainer.class;
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        Element templateEle = DomUtils.getChildElementByTagName(element, TEMPLATE);
        if (templateEle != null) {
            Object template = parserContext.getDelegate().parsePropertyValue(templateEle, builder.getRawBeanDefinition(), "template");
            builder.addPropertyValue("template", template);
        }
        Element sqlQueryEle = DomUtils.getChildElementByTagName(element, SQL_QUERY);
        if (sqlQueryEle != null) {
            builder.addPropertyValue("template", parserContext.getDelegate().parsePropertySubElement(sqlQueryEle, builder.getRawBeanDefinition(), null));
        }

        Element receiveOperationHandlerEle = DomUtils.getChildElementByTagName(element, RECEIVE_OPERATION_HANDLER);
        if (receiveOperationHandlerEle != null) {
            builder.addPropertyValue("receiveOperationHandler",
                    parserContext.getDelegate().parsePropertyValue(receiveOperationHandlerEle, builder.getRawBeanDefinition(), "receiveOperationHandler"));
        }

        Element triggerOperationHandlerEle = DomUtils.getChildElementByTagName(element, TRIGGER_OPERATION_HANDLER);
        if (receiveOperationHandlerEle != null) {
            builder.addPropertyValue("triggerOperationHandler",
                    parserContext.getDelegate().parsePropertyValue(triggerOperationHandlerEle, builder.getRawBeanDefinition(), "triggerOperationHandler"));
        }

        String receiveTimeout = element.getAttribute(RECEIVE_TIMEOUT);
        if (StringUtils.hasLength(receiveTimeout)) {
            builder.addPropertyValue("receiveTimeout", receiveTimeout);
        }

        String recoveryInterval = element.getAttribute(RECOVERY_INTERVAL);
        if (StringUtils.hasLength(recoveryInterval)) {
            builder.addPropertyValue("recoveryInterval", recoveryInterval);
        }

        String concurrentConsumers = element.getAttribute(CONCURRENT_CONSUMERS);
        if (StringUtils.hasLength(concurrentConsumers)) {
            builder.addPropertyValue("concurrentConsumers", concurrentConsumers);
        }

        String maxConcurrentConsumers = element.getAttribute(MAX_CONCURRENT_CONSUMERS);
        if (StringUtils.hasLength(maxConcurrentConsumers)) {
            builder.addPropertyValue("maxConcurrentConsumers", maxConcurrentConsumers);
        }

        String idleTaskExecutionLimit = element.getAttribute(IDLE_TASK_EXECUTION_LIMIT);
        if (StringUtils.hasLength(idleTaskExecutionLimit)) {
            builder.addPropertyValue("idleTaskExecutionLimit", idleTaskExecutionLimit);
        }
    }
}
