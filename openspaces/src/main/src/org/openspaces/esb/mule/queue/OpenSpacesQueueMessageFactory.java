package org.openspaces.esb.mule.queue;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.transport.MessageTypeNotSupportedException;
import org.mule.transport.AbstractMuleMessageFactory;

public class OpenSpacesQueueMessageFactory extends AbstractMuleMessageFactory {

    public OpenSpacesQueueMessageFactory(MuleContext ctx) {
        super(ctx);
    }

    @Override
    protected Object extractPayload(Object transportMessage, String encoding) throws Exception {
        if (transportMessage == null) {
            throw new MessageTypeNotSupportedException(transportMessage, getClass());
        }

        if (transportMessage instanceof OpenSpacesQueueObject) {
            return ((OpenSpacesQueueObject) transportMessage).getPayload();
        }
        // handle previous versions when the payload itself was passed to this method
        else {
            return transportMessage;
        }
      
    }

    @Override
    protected Class<?>[] getSupportedTransportMessageTypes() {
        return new Class[] { Object.class };
    }

    @Override
    protected void addProperties(DefaultMuleMessage message, Object transportMessage) throws Exception {
        super.addProperties(message, transportMessage);

        if (transportMessage instanceof OpenSpacesQueueObject) {
            OpenSpacesQueueObject queueObject = (OpenSpacesQueueObject) transportMessage;
            
            if(queueObject.getPayloadMetaData() != null)
                message.addProperties(queueObject.getPayloadMetaData());

            String correlationId = queueObject.getCorrelationID();

            if (correlationId != null)
                message.setCorrelationId(correlationId);
            
        }
    }

}
