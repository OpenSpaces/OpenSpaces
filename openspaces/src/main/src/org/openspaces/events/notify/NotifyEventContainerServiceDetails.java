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

package org.openspaces.events.notify;

import org.openspaces.events.EventContainerServiceDetails;
import org.openspaces.pu.service.AggregatedServiceDetails;
import org.openspaces.pu.service.ServiceDetails;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Notify container service details.
 *
 * @author kimchy
 */
public class NotifyEventContainerServiceDetails extends EventContainerServiceDetails {

    private static final long serialVersionUID = -3767022008839525953L;
    
    public static final String SERVICE_SUB_TYPE = "notify";

    public static class Attributes extends EventContainerServiceDetails.Attributes {
        public static final String COMM_TYPE = "comm-type";
        public static final String FIFO = "fifo";
        public static final String BATCH_SIZE = "batch-size";
        public static final String BATCH_TIME = "batch-time";
        public static final String BATCH_PENDING_THRESHOLD = "batch-pending-threshold";
        public static final String AUTO_RENEW = "auto-renew";
        public static final String NOTIFY_WRITE = "notify-write";
        public static final String NOTIFY_TAKE = "notify-take";
        public static final String NOTIFY_UPDATE = "notify-update";
        public static final String NOTIFY_LEASE_EXPIRE = "notify-lease-expire";
        public static final String NOTIFY_UNMATCHED = "notify-unmatched";
        public static final String TRIGGER_NOTIFY_TEMPLATE = "trigger-notify-template";
        public static final String REPLICATE_NOTIFY_TEMPLATE = "replicate-notify-template";
        public static final String PERFORM_TAKE_ON_NOTIFY = "perform-take-on-notify";
        public static final String PASS_ARRAY_AS_IS = "pass-array-as-is";
        public static final String GUARANTEED = "guaranteed";
        public static final String DURABLE = "durable";
    }

    public NotifyEventContainerServiceDetails() {
        super();
    }

    public NotifyEventContainerServiceDetails(String id, String gigaSpace, Object template, boolean performSnapshot, String transactionManager,
                                              int commType,
                                              boolean fifo, Integer batchSize, Integer batchTime, Integer batchPendingThreshold, boolean autoRenew,
                                              Boolean notifyAll, Boolean notifyWrite, Boolean notifyUpdate, Boolean notifyTake, Boolean notifyLeaseExpire, Boolean notifyUnmatched,
                                              Boolean triggerNotifyTemplate, Boolean replicateNotifyTemplate,
                                              boolean performTakeOnNotify, boolean passArrayAsIs, boolean guaranteed, boolean durable) {
        super(id, SERVICE_SUB_TYPE, gigaSpace, "Notify event container", "Notify event container, template [" + template + "]", template, performSnapshot, transactionManager);
        switch (commType) {
            case 0:
                getAttributes().put(Attributes.COMM_TYPE, "unicast");
                break;
            case 1:
                getAttributes().put(Attributes.COMM_TYPE, "multiplex");
                break;
            case 2:
                getAttributes().put(Attributes.COMM_TYPE, "multicast");
                break;
        }
        getAttributes().put(Attributes.FIFO, fifo);
        getAttributes().put(Attributes.BATCH_SIZE, batchSize);
        getAttributes().put(Attributes.BATCH_TIME, batchTime);
        getAttributes().put(Attributes.BATCH_PENDING_THRESHOLD, batchPendingThreshold);
        getAttributes().put(Attributes.AUTO_RENEW, autoRenew);
        if (notifyAll != null && notifyAll) {
            getAttributes().put(Attributes.NOTIFY_WRITE, true);
            getAttributes().put(Attributes.NOTIFY_UPDATE, true);
            getAttributes().put(Attributes.NOTIFY_TAKE, true);
            getAttributes().put(Attributes.NOTIFY_LEASE_EXPIRE, true);
            getAttributes().put(Attributes.NOTIFY_UNMATCHED, true);
        } else {
            getAttributes().put(Attributes.NOTIFY_WRITE, notifyWrite == null ? false : notifyWrite);
            getAttributes().put(Attributes.NOTIFY_UPDATE, notifyUpdate == null ? false : notifyUpdate);
            getAttributes().put(Attributes.NOTIFY_TAKE, notifyTake == null ? false : notifyTake);
            getAttributes().put(Attributes.NOTIFY_LEASE_EXPIRE, notifyLeaseExpire == null ? false : notifyLeaseExpire);
            getAttributes().put(Attributes.NOTIFY_UNMATCHED, notifyUnmatched == null ? false : notifyUnmatched);
        }
        getAttributes().put(Attributes.TRIGGER_NOTIFY_TEMPLATE, triggerNotifyTemplate);
        getAttributes().put(Attributes.REPLICATE_NOTIFY_TEMPLATE, replicateNotifyTemplate);
        getAttributes().put(Attributes.PERFORM_TAKE_ON_NOTIFY, performTakeOnNotify);
        getAttributes().put(Attributes.PASS_ARRAY_AS_IS, passArrayAsIs);
        getAttributes().put(Attributes.GUARANTEED, guaranteed);
        getAttributes().put(Attributes.DURABLE, durable);
    }

    public String getCommType() {
        return (String) getAttributes().get(Attributes.COMM_TYPE);
    }

    public boolean isFifo() {
        return (Boolean) getAttributes().get(Attributes.FIFO);
    }

    public Integer getBatchSize() {
        return (Integer) getAttributes().get(Attributes.BATCH_SIZE);
    }

    public Integer getBatchTime() {
        return (Integer) getAttributes().get(Attributes.BATCH_TIME);
    }

    public boolean isAutoRenew() {
        return (Boolean) getAttributes().get(Attributes.AUTO_RENEW);
    }

    public boolean isNotifyWrite() {
        return (Boolean) getAttributes().get(Attributes.NOTIFY_WRITE);
    }

    public boolean isNotifyUpdate() {
        return (Boolean) getAttributes().get(Attributes.NOTIFY_UPDATE);
    }

    public boolean isNotifyTake() {
        return (Boolean) getAttributes().get(Attributes.NOTIFY_TAKE);
    }

    public boolean isNotifyLeaseExpire() {
        return (Boolean) getAttributes().get(Attributes.NOTIFY_LEASE_EXPIRE);
    }

    public boolean isNotifyUnmatched() {
        return (Boolean) getAttributes().get(Attributes.NOTIFY_UNMATCHED);
    }

    public Boolean isTriggerNotifyTemplate() {
        return (Boolean) getAttributes().get(Attributes.TRIGGER_NOTIFY_TEMPLATE);
    }

    public Boolean isReplicateNotifyTemplate() {
        return (Boolean) getAttributes().get(Attributes.REPLICATE_NOTIFY_TEMPLATE);
    }

    public boolean isPerformTakeOnNotify() {
        return (Boolean) getAttributes().get(Attributes.PERFORM_TAKE_ON_NOTIFY);
    }

    public boolean isPassArrayAsIs() {
        return (Boolean) getAttributes().get(Attributes.PASS_ARRAY_AS_IS);
    }

    public boolean isGuaranteed() {
        return (Boolean) getAttributes().get(Attributes.GUARANTEED);
    }

    public boolean isDurable() {
        Boolean durable = (Boolean) getAttributes().get(Attributes.DURABLE);
        return durable == null ? false : durable.booleanValue();
    }
    
    @Override
    public AggregatedServiceDetails aggregateById(ServiceDetails[] servicesDetails) {
        return new AggregatedNotifyEventContainerServiceDetails(serviceType, servicesDetails);
    }

    @Override
    public AggregatedServiceDetails aggregateByServiceSubType(ServiceDetails[] servicesDetails) {
        return new AggregatedNotifyEventContainerServiceDetails(serviceType, servicesDetails);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
    }
}