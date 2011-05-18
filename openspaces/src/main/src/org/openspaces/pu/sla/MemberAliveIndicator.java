/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.pu.sla;

import java.io.Serializable;

/**
 * The member alive indicator allows to configure the SLA on how often a member will be checed to
 * see if a member is alive, and in case of failure, how many times to retry and how often.
 *
 * @author kimchy
 * @see org.openspaces.core.cluster.MemberAliveIndicator
 */
public class MemberAliveIndicator implements Serializable {

    private static final long serialVersionUID = -7738144705717881390L;

    private long invocationDelay = 5000;

    private long retryTimeout = 500;

    private int retryCount = 3;

    /**
     * How often an instance will be checked and verfied to be alive. In <b>milliseconds</b>
     * and defaults to 5000, which are 5 seconds.
     */
    public long getInvocationDelay() {
        return invocationDelay;
    }

    /**
     * How often an instance will be checked and verfied to be alive. In <b>milliseconds</b>
     * and defaults to 5000, which are 5 seconds.
     */
    public void setInvocationDelay(long invocationDelay) {
        this.invocationDelay = invocationDelay;
    }

    /**
     * Once a member has been indicated as not alive, what is the retry timeout interval. In
     * <b>milliseconds</b> and defaults to 500 milliseconds.
     */
    public long getRetryTimeout() {
        return retryTimeout;
    }

    /**
     * Once a member has been indicated as not alive, what is the retry timeout interval. In
     * <b>milliseconds</b> and defaults to 500 milliseconds.
     */
    public void setRetryTimeout(long retryTimeout) {
        this.retryTimeout = retryTimeout;
    }

    /**
     * Once a member has been indicated as not alive, how many times to check it before giving
     * up on it. Defaults to <code>3</code>.
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * Once a member has been indicated as not alive, how many times to check it before giving
     * up on it. Defaults to <code>3</code>.
     */
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}
