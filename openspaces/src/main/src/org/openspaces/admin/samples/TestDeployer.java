package org.openspaces.admin.samples;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsc.GridServiceContainer;

import java.util.concurrent.TimeUnit;

import com.gigaspaces.log.*;

/**
 * @author kimchy
 */
public class TestDeployer {

    public static void main(String[] args) throws Exception {
        Admin admin = new AdminFactory().addGroup("kimchy").createAdmin();

        admin.getGridServiceContainers().waitFor(1);
        final GridServiceContainer container = admin.getGridServiceContainers().getContainers()[0];

        System.out.println("Getting logs...");

        new Thread(new Runnable() {

            private LogEntryMatcher matcher = new ContinuousLogEntryMatcher(new LastNLogEntryMatcher(100));

            public void run() {
                while (true) {
                    final LogEntries logs = container.log(matcher);
                    for (LogEntry log : logs) {
                        System.out.print(log.getText());
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            }
        }).start();

        Thread.sleep(10000000);


        System.out.println("Closing Admin");
        admin.close();
    }
}
