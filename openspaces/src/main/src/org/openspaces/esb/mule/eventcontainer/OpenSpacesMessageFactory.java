package org.openspaces.esb.mule.eventcontainer;

import java.util.Iterator;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.transport.MessageTypeNotSupportedException;
import org.mule.transport.AbstractMuleMessageFactory;
import org.mule.util.UUID;
import org.openspaces.esb.mule.message.MessageHeader;
import org.openspaces.esb.mule.message.UniqueIdMessageHeader;

public class OpenSpacesMessageFactory extends AbstractMuleMessageFactory{
    
    public OpenSpacesMessageFactory(MuleContext ctx) {
        super(ctx);
    }
    
    @Override
    protected Object extractPayload(Object transportMessage, String encoding) throws Exception {
        if (transportMessage == null) {
            throw new MessageTypeNotSupportedException(transportMessage, getClass());
        }
        return transportMessage;
    }

    @Override
    protected Class<?>[] getSupportedTransportMessageTypes() {
        return new Class[] { Object.class };
    }
    
    @Override
    protected void addProperties(DefaultMuleMessage message, Object transportMessage) throws Exception {
        super.addProperties(message, transportMessage);
        
        if (transportMessage instanceof MessageHeader) {
            Iterator keys = ((MessageHeader) transportMessage).getProperties().keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                Object value = ((MessageHeader) transportMessage).getProperty(key);
                if (value != null) {
                    message.setProperty(key, value);
                }
            }
        }
        if (message.getProperty(UniqueIdMessageHeader.UNIQUE_ID) == null) {
            message.setProperty(UniqueIdMessageHeader.UNIQUE_ID, UUID.getUUID());
        }
    }

}
