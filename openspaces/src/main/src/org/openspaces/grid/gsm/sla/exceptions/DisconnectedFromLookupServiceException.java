/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.grid.gsm.sla.exceptions;

import java.util.Arrays;

import net.jini.core.discovery.LookupLocator;


/**
 * @author itaif
 * @since 9.0.1
 */
public class DisconnectedFromLookupServiceException extends SlaEnforcementInProgressException implements SlaEnforcementFailure {

    private static final long serialVersionUID = 1L;
    
    public DisconnectedFromLookupServiceException(String puName, LookupLocator[] locators, String[] groups) {
        super(new String[] {puName}, message(locators, groups));
    }
    
    private static String message(LookupLocator[] locators, String[] groups) {
        return "Disconnected from all Lookup Services. lookup-locators="+toString(locators)+" lookup-groups="+toString(groups);
    }

    private static String toString(String[] array) {
        return Arrays.toString(array);
    }

    private static String toString(LookupLocator[] locators) {
        final String[] locatorsToString = new String[locators.length];
        for (int i = 0 ; i < locators.length ; i ++) {
            locatorsToString[i]=locators[i].getHost() + ":" + locators[i].getPort();
        }
        return toString(locatorsToString);
    }
}
