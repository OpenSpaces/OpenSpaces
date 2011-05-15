package org.openspaces.admin.internal.application;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.openspaces.admin.Admin;
import org.openspaces.admin.application.Application;
import org.openspaces.admin.application.events.ApplicationAddedEventListener;
import org.openspaces.admin.application.events.ApplicationAddedEventManager;
import org.openspaces.admin.application.events.ApplicationLifecycleEventListener;
import org.openspaces.admin.application.events.ApplicationRemovedEventManager;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.application.events.DefaultApplicationAddedEventManager;
import org.openspaces.admin.internal.application.events.DefaultApplicationRemovedEventManager;
import org.openspaces.admin.internal.application.events.InternalApplicationAddedEventManager;
import org.openspaces.admin.internal.application.events.InternalApplicationRemovedEventManager;
import org.openspaces.admin.internal.pu.InternalProcessingUnits;
import org.openspaces.admin.pu.ProcessingUnit;

import com.j_spaces.kernel.SizeConcurrentHashMap;

public class DefaultApplications implements InternalApplications {

    private final InternalAdmin admin;

    private final Map<String, Application> applicationsByName = new SizeConcurrentHashMap<String, Application>();

    private final InternalApplicationAddedEventManager applicationAddedEventManager;

    private final InternalApplicationRemovedEventManager applicationRemovedEventManager;

    public DefaultApplications(InternalAdmin admin) {
        this.admin = admin;
        this.applicationAddedEventManager = new DefaultApplicationAddedEventManager(this);
        this.applicationRemovedEventManager = new DefaultApplicationRemovedEventManager(this);
    }

    public Application[] getApplications() {
        Collection<Application> applications = applicationsByName.values();
        return applications.toArray(new Application[applications.size()]);
    }

    public int getSize() {
        return applicationsByName.size();
    }

    public boolean isEmpty() {
        return applicationsByName.isEmpty();
    }

    public Application getApplication(String name) {
        return applicationsByName.get(name);
    }

    public Map<String, Application> getNames() {
        return Collections.unmodifiableMap(applicationsByName);
    }

    public Application waitFor(String applicationName) {
        return waitFor(applicationName, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());    
    }

    public Application waitFor(final String applicationName, long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Application> ref = new AtomicReference<Application>();
        ApplicationAddedEventListener added = new ApplicationAddedEventListener() {
            
            public void applicationAdded(Application application) {
                if (application.getName().equals(applicationName)) {
                    ref.set(application);
                    latch.countDown();
                }
                
            }
        };
        getApplicationAdded().add(added);
        try {
            latch.await(timeout, timeUnit);
            return ref.get();
        } catch (InterruptedException e) {
            return null;
        } finally {
            getApplicationAdded().remove(added);
        }
    }

    public ApplicationAddedEventManager getApplicationAdded() {
        return this.applicationAddedEventManager;
    }

    public ApplicationRemovedEventManager getApplicationRemoved() {
        return this.applicationRemovedEventManager;
    }

    public void addLifecycleListener(ApplicationLifecycleEventListener eventListener) {
        getApplicationAdded().add(eventListener);
        getApplicationRemoved().add(eventListener);
    }

    public void removeLifecycleListener(ApplicationLifecycleEventListener eventListener) {
        getApplicationAdded().remove(eventListener);
        getApplicationRemoved().remove(eventListener);
    }

    public Iterator<Application> iterator() {
        return Collections.unmodifiableCollection(applicationsByName.values()).iterator();
    }

    public Admin getAdmin() {
        return this.admin;
    }

    public void addApplication(Application application, ProcessingUnit processingUnit) {
        assertStateChangesPermitted();
        if (!applicationsByName.containsKey(application.getName())) {
            applicationsByName.put(application.getName(),application);
        }
        application = getApplication(application.getName());
        ((InternalApplicationAware)processingUnit).setApplication(application);
        InternalProcessingUnits processingUnits = (InternalProcessingUnits)application.getProcessingUnits();
        processingUnits.addProcessingUnit(processingUnit);
        if (processingUnits.getSize() == 1) {
            applicationAddedEventManager.applicationAdded(application);
        }
    }

    public void removeProcessingUnit(ProcessingUnit processingUnit) {
            
        Application application = processingUnit.getApplication();
        if (application != null) {
            assertStateChangesPermitted();
            InternalProcessingUnits processingUnits = (InternalProcessingUnits)application.getProcessingUnits();
            processingUnits.removeProcessingUnit(processingUnit.getName());
            if (processingUnits.isEmpty()) {
                // note, currently we do not remove the providers from the applicationProviders
                // this allows us to track the applications we had (if we want to expose that)
                Application existingApplication = applicationsByName.remove(application.getName());
                if (existingApplication != null) {
                    applicationRemovedEventManager.applicationRemoved(existingApplication);
                }
            }
        }
        
    }

    private void assertStateChangesPermitted() {
        admin.assertStateChangesPermitted();
    }
}
