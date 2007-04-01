package org.openspaces.enhancer.entry;

import java.io.IOException;

/**
 * @author kimchy
 */
public interface BinaryEntry {

    void pack() throws IOException;

    void unpack() throws IOException, ClassNotFoundException;
}
