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

package org.openspaces.admin.space;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.j_spaces.core.client.SpaceURL;
import org.openspaces.admin.GridComponent;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.space.events.ReplicationStatusChangedEventManager;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEventManager;
import org.openspaces.admin.space.events.SpaceModeChangedEventManager;
import org.openspaces.core.GigaSpace;

import java.util.concurrent.TimeUnit;

/**
 * A Space Instance is a single instance of a space running as a part of a {@link Space}. For example, when deploying
 * a 2 partitions each with one backup topology, there will be 4 space instances running.
 *
 * @author kimchy
 */
public interface SpaceInstance extends GridComponent, StatisticsMonitor {

    /**
     * Returns the instance id of the space (starting from 1).
     */
    int getInstanceId();

    /**
     * Returns the backup id (if it is a topology with backups) of the space instance.
     */
    int getBackupId();

    /**
     * Returns the space mode, indicating if the space is primary or backup.
     */
    SpaceMode getMode();

    SpaceURL getSpaceUrl();

    /**
     * Waits for the space instance to move to the provided space mode. Returns
     * <code>true</code> if the mode was changed to the required one withing the
     * timeout, or <code>false</code> otherwise.
     */
    boolean waitForMode(SpaceMode requiredMode, long timeout, TimeUnit timeUnit);

    /**
     * Returns the <b>direct</b> proxy to the actual space instance.
     */
    GigaSpace getGigaSpace();

    /**
     * Returns the space instance statistics.
     */
    SpaceInstanceStatistics getStatistics();

    /**
     * Returns the space this instance is part of.
     */
    Space getSpace();

    /**
     * Returns the partition this instance is part of.
     */
    SpacePartition getPartition();

    /**
     * Returns the replication targets this space instance is replicating to.
     */
    ReplicationTarget[] getReplicationTargets();

    /**
     * Allows to register for {@link org.openspaces.admin.space.events.SpaceModeChangedEvent}s.
     */
    SpaceModeChangedEventManager getSpaceModeChanged();

    /**
     * Allows to register for {@link org.openspaces.admin.space.events.ReplicationStatusChangedEvent}s.
     */
    ReplicationStatusChangedEventManager getReplicationStatusChanged();

    /**
     * Allows to register for {@link org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEvent}s.
     *
     * <p>Note, monitoring must be started using {@link #startStatisticsMonitor()} for events to occur.
     */
    SpaceInstanceStatisticsChangedEventManager getStatisticsChanged();
}
