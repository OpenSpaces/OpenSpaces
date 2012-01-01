package org.openspaces.admin.internal.support;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.rmi.ConnectException;

/**
 * @author kimchy
 */
public abstract class NetworkExceptionHelper {

    public static boolean isConnectOrCloseException(Throwable e) {
        if (e instanceof ConnectException) {
            if (e.getCause() instanceof ClosedChannelException || e.getCause() instanceof java.net.ConnectException) {
                return true;
            }

            if (e.getCause() instanceof IOException && e.getCause().getMessage().contains("aborted")) {
                return true;
            }

            if (e.getCause() instanceof IOException && e.getCause().getMessage().contains("Connection reset by peer")) {
                return true;
            }
        }
        if (e instanceof IOException) {
            if (e.getMessage().startsWith("Connection reset by peer")) {
                return true;
            }
        }
        return false;
    }
}
