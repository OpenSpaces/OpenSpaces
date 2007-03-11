package org.openspaces.events.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author kimchy
 */
public abstract class AbstarctTxEventContainerBeanDefinitionParser extends AbstarctEventContainerBeanDefinitionParser {

    private static final String TX_MANAGER = "tx-manager";

    private static final String TX_NAME = "tx-name";

    private static final String TX_TIMEOUT = "tx-timeout";

    private static final String TX_ISOLATION = "tx-isolation";

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        String txManager = element.getAttribute(TX_MANAGER);
        if (StringUtils.hasLength(txManager)) {
            builder.addPropertyReference("transactionManager", txManager);
        }

        String txName = element.getAttribute(TX_NAME);
        if (StringUtils.hasLength(txName)) {
            builder.addPropertyValue("transactionName", txName);
        }

        String txTimeout = element.getAttribute(TX_TIMEOUT);
        if (StringUtils.hasLength(txTimeout)) {
            builder.addPropertyValue("transactionTimeout", txTimeout);
        }

        String txIsolation = element.getAttribute(TX_ISOLATION);
        if (StringUtils.hasLength(txIsolation)) {
            builder.addPropertyValue("transactionIsolationLevelName", DefaultTransactionDefinition.PREFIX_ISOLATION + txIsolation);
        }
    }
}