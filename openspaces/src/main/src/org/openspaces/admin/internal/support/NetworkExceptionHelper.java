package org.openspaces.admin.internal.support;

import java.nio.channels.ClosedChannelException;
import java.rmi.ConnectException;

/**
 * @author kimchy
 */
public abstract class NetworkExceptionHelper {

    public static boolean isConnectOrCloseException(Exception e) {
        if (e instanceof ConnectException) {
            if (e.getCause() instanceof ClosedChannelException || e.getCause() instanceof java.net.ConnectException) {
                return true;
            }
        }
        return false;
    }
}
