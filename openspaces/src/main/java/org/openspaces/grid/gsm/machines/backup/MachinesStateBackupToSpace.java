/*******************************************************************************
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
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

package org.openspaces.grid.gsm.machines.backup;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import net.jini.core.lease.Lease;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.core.GigaSpace;
import org.openspaces.grid.esm.EsmSystemProperties;
import org.openspaces.grid.gsm.SingleThreadedPollingLog;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementState;

import com.gigaspaces.async.AsyncFuture;
import com.gigaspaces.internal.utils.concurrent.GSThreadFactory;
import com.gigaspaces.query.IdQuery;

public class MachinesStateBackupToSpace implements MachinesStateBackup {

    private final Log logger = new SingleThreadedPollingLog(LogFactory.getLog(this.getClass()));
    
    public static final Integer SINGLETON_ID = 0;
    
    private final MachinesSlaEnforcementState machinesSlaEnforcementState;
    private final GigaSpace space;
    private final AtomicLong writeCompletedVersion;
    private final AtomicReference<Throwable> lastError;
    
    private final ThreadFactory threadFactory = new GSThreadFactory(this.getClass().getName(), /*daemonThreads=*/ true);
    private final ExecutorService service = Executors.newSingleThreadExecutor(threadFactory);

    private final InternalAdmin admin;
    
    private static final long BACKUP_INTERVAL_MILLISECONDS = Long.getLong(EsmSystemProperties.ESM_BACKUP_INTERVAL_MILLISECONDS, EsmSystemProperties.ESM_BACKUP_INTERVAL_MILLISECONDS_DEFAULT);

    private ScheduledFuture<?> scheduledFuture;

    private boolean started = false;
    private boolean recovered = false;

    private AsyncFuture<MachinesState> asyncRead;
    
    public MachinesStateBackupToSpace(Admin admin, GigaSpace space, MachinesSlaEnforcementState machinesSlaEnforcementState) {
        this.admin = (InternalAdmin) admin;
        this.space = space;
        this.machinesSlaEnforcementState = machinesSlaEnforcementState;
        writeCompletedVersion = new AtomicLong(machinesSlaEnforcementState.getVersion() - 1);
        lastError = new AtomicReference<Throwable>();
    }

    private void startBackup() {
        if (started) {
            return;
        }
        scheduledFuture = this.admin.scheduleWithFixedDelayNonBlockingStateChange(new Runnable() {

            private Future<?> future = null;

            @Override
            public void run() {
                final long currentVersion = machinesSlaEnforcementState.getVersion();
                if (writeCompletedVersion.get() == currentVersion) {
                    logger.trace("Already wrote machines state to space. version=" + currentVersion);
                    return;
                }
                else if (future != null && !future.isDone()) {
                    logger.trace("Machine state write is in progress");
                }
                else {
                    write(currentVersion);
                }
            }

            private void write(final long currentVersion) {
                try {
                    final MachinesState machinesState = machinesSlaEnforcementState.toMachinesState();
                    machinesState.setId(SINGLETON_ID);
                    if (machinesState.getVersion() != currentVersion) {
                        throw new IllegalStateException("Expected version " + currentVersion + ", instead got " + machinesState.getVersion());
                    }
                    future = service.submit(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                logger.trace("Before writing machines state to space. version=" + currentVersion);
                                space.write(machinesState, Lease.FOREVER);
                                writeCompletedVersion.set(currentVersion);
                                lastError.set(null);                                
                                logger.trace("Successfully written machines state to space. version=" + currentVersion);
                            } catch (final Throwable t) {
                                logger.debug("Failed writing machines state to space. version=" + currentVersion, t);
                                lastError.set(t);
                            }
                        }
                    });
                }
                catch (final Throwable t) {
                    logger.debug("Failed writing machines state to space. version=" + currentVersion, t);
                    lastError.set(t);
                }
            }
        },0L, 
        BACKUP_INTERVAL_MILLISECONDS, 
        TimeUnit.MILLISECONDS);
        started = true;
    }
    
    @Override
    public void close() {
        scheduledFuture.cancel(false);
        service.shutdownNow();
    }

    @Override
    public void validateBackupCompleted(ProcessingUnit pu) throws MachinesStateBackupFailureException, MachinesStateBackupInProgressException {
        final Throwable lastError = (Throwable) this.lastError.get();
        if (lastError != null) { 
            throw new MachinesStateBackupFailureException(pu, lastError);
        }
        else if (writeCompletedVersion.get() != machinesSlaEnforcementState.getVersion()) {
            throw new MachinesStateBackupInProgressException(pu);    
        }
    }

    @Override
    public void recoverAndStartBackup(ProcessingUnit pu) throws MachinesStateRecoveryFailureException, MachinesStateRecoveryInProgressException {
        recover(pu);
        startBackup();
    }

    private void recover(ProcessingUnit pu)
            throws MachinesStateRecoveryFailureException, MachinesStateRecoveryInProgressException {
        if (!recovered) {
            final MachinesState machinesState = readMachineStateFromSpace(pu);
            if (machinesState != null) {
               logger.info("Recovering machines state from " + space.getName());
               machinesSlaEnforcementState.fromMachinesState(machinesState);
            }
            else {
                logger.debug("Not recovering machines state from " + space.getName() + " since it's empty.");
            }
            recovered = true;
        }
    }

    private MachinesState readMachineStateFromSpace(ProcessingUnit pu) throws MachinesStateRecoveryFailureException, MachinesStateRecoveryInProgressException {
        if (this.asyncRead == null) {
            this.asyncRead = space.asyncRead(new IdQuery<MachinesState>(MachinesState.class, SINGLETON_ID));
            
        }
        if (this.asyncRead != null && this.asyncRead.isDone()){
            try {
                return asyncRead.get();
            }
            catch (ExecutionException e) {
                throw new MachinesStateRecoveryFailureException(pu, e);
            }
            catch (InterruptedException e) {
                throw new MachinesStateRecoveryFailureException(pu, e);
            }
        }
        throw new MachinesStateRecoveryInProgressException(pu);
    }
}