package org.openspaces.enhancer.entry.message;

import org.openspaces.enhancer.entry.Entry;
import org.openspaces.enhancer.entry.RoutingIndex;

/**
 * @author kimchy
 */
@Entry
public class Message {

    private byte[] content;

    @RoutingIndex
    private int value;

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
