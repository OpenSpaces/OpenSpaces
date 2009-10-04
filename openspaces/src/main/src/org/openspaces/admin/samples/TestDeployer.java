package org.openspaces.admin.samples;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceContainerOptions;
import org.openspaces.admin.gsa.GridServiceOptions;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.gsm.events.GridServiceManagerAddedEventListener;
import org.openspaces.admin.gsm.events.GridServiceManagerRemovedEventListener;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.events.GridServiceContainerAddedEventListener;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitPartition;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.space.SpaceDeployment;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEventListener;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEvent;

import java.util.concurrent.TimeUnit;

import com.gigaspaces.log.LogEntry;
import com.gigaspaces.log.AllLogEntryMatcher;
import com.gigaspaces.log.LastXTimeLogEntryMatcher;

/**
 * @author kimchy
 */
public class TestDeployer {

    public static void main(String[] args) throws Exception {
        Admin admin = new AdminFactory().addGroup("kimchy").createAdmin();

        admin.getGridServiceContainers().waitFor(1);
        GridServiceContainer container = admin.getGridServiceContainers().getContainers()[0];

        System.out.println("Getting logs...");
        System.out.println("*****");
        LogEntry[] logs = container.log(new LastXTimeLogEntryMatcher(2, TimeUnit.SECONDS));
        for (LogEntry log : logs) {
            System.out.print(log.getText());
        }
        System.out.println("*****");
        logs = container.log(new LastXTimeLogEntryMatcher(logs[0].getTimestamp(), 2, TimeUnit.SECONDS));
        for (LogEntry log : logs) {
            System.out.print(log.getText());
        }


        System.out.println("Closing Admin");
        admin.close();
    }
}
