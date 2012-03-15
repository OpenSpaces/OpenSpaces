/*******************************************************************************
 * 
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
 *  
 ******************************************************************************/
package org.openspaces.admin.internal.support;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.rmi.ConnectException;

/**
 * @author kimchy
 */
public abstract class NetworkExceptionHelper {

    public static boolean isConnectOrCloseException(Throwable e) {
        if (e instanceof ConnectException && 
            e.getCause() != null && 
            e.getCause() instanceof IOException) {
            
            if (e.getCause() instanceof ClosedChannelException || e.getCause() instanceof java.net.ConnectException) {
                return true;
            }

            if (e.getCause().getMessage() != null && e.getCause().getMessage().contains("aborted")) {
                return true;
            }

            if (e.getCause().getMessage() != null && e.getCause().getMessage().contains("Connection reset by peer")) {
                return true;
            }
        }
        if (e instanceof IOException) {
            if (e.getMessage() != null && e.getMessage().startsWith("Connection reset by peer")) {
                return true;
            }
        }
        return false;
    }
}
