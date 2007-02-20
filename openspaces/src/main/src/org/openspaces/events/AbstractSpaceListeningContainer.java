package org.openspaces.events;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceException;
import org.openspaces.core.space.mode.AfterSpaceModeChangeEvent;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.Lifecycle;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>Common base class for all containers which need to implement listening
 * based on Space events.
 *
 * <p>Provide lifecycle support for containers using {@link #doStart()} and {@link #doStop()}.
 * By default, the container lifecycle will be bound to the space mode, starting when the space
 * mode is <code>PRIMARY</code> and stopping otherwise. This behviour is controlled by
 * {@link #setActiveWhenPrimary(boolean)}. Note that most operations can not be performed directly
 * against a backup space cluster member (when working with the {@link org.openspaces.core.GigaSpaceFactoryBean#setClustered(boolean)}
 * flag set to <code>false</code>) so setting this flag to <code>false</code> (which means this container
 * will operate when the space is in <code>BACKUP</code> mode as well) needs to be done with care.
 *
 * <p>This base class does not assume any specific listener programming model
 * or listener invoker mechanism. It just provides the general runtime
 * lifecycle management needed for any kind of Space event listening mechanism.
 *
 * <p>For a concrete listener programming model, check out the
 * {@link org.openspaces.events.AbstractEventListenerContainer} subclass. For a concrete listener
 * invoker mechanism, check out the {@link org.openspaces.events.polling.SimplePollingEventListenerContainer} class.
 *
 * @author kimchy
 */
public abstract class AbstractSpaceListeningContainer implements Lifecycle, BeanNameAware,
        InitializingBean, DisposableBean, ApplicationListener {

    protected final Log logger = LogFactory.getLog(getClass());

    private GigaSpace gigaSpace;

    private boolean activeWhenPrimary = true;

    private String beanName;

    private boolean active = false;

    private boolean running = false;

    private final List pausedTasks = new LinkedList();

    private final Object lifecycleMonitor = new Object();


    /**
     * Sets the GigaSpace instance to be used for space event listening operations.
     */
    public void setGigaSpace(GigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
    }

    /**
     * Returns the GigaSpace instance to be used for Space operations.
     */
    protected final GigaSpace getGigaSpace() {
        return this.gigaSpace;
    }

    /**
     * <p>Set whether this container will start only when it is primary (space mode).
     * <p>Default is <code>true</code>. Set to <code>false</code> in order for this
     * container to always start regardless of the space mode.
     */
    public void setActiveWhenPrimary(boolean activeWhenPrimary) {
        this.activeWhenPrimary = activeWhenPrimary;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    /**
     * Return the bean name that this listener container has been assigned
     * in its containing bean factory, if any.
     */
    protected final String getBeanName() {
        return this.beanName;
    }


    /**
     * Delegates to {@link #validateConfiguration()} and {@link #initialize()}.
     */
    public void afterPropertiesSet() {
        validateConfiguration();
        initialize();
    }

    /**
     * Validate the configuration of this container.
     */
    protected void validateConfiguration() {
        Assert.notNull(gigaSpace, "gigaSpace property is requried");
    }


    /**
     * Initialize this container. If this container is not configured with "activeWhenPrimary" flag
     * set to <code>true</code> will call {@link #doStart()} (if it is set to <code>true</code>, lifecycle
     * of the container will be controlled by the current space mode). {@link #doInitialize()} will be called
     * for additional initialization after the possible {@link #doStart()} call.
     *
     * @see #onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    public void initialize() throws GigaSpaceException {
        synchronized (this.lifecycleMonitor) {
            this.active = true;
            this.lifecycleMonitor.notifyAll();
        }

        if (!activeWhenPrimary) {
            doStart();
        }

        doInitialize();
    }

    /**
     * Calls {@link #shutdown()} when the BeanFactory destroys the container instance.
     *
     * @see #shutdown()
     */
    public void destroy() {
        shutdown();
    }

    /**
     * Stop container, call {@link #doShutdown()}, and close this container.
     */
    public void shutdown() throws GigaSpaceException {
        logger.debug("Shutting down Space Event listener container [" + getBeanName() + "]");
        synchronized (this.lifecycleMonitor) {
            this.running = false;
            this.active = false;
            this.lifecycleMonitor.notifyAll();
        }

        // Shut down the invokers.
        doShutdown();
    }

    /**
     * Return whether this container is currently active,
     * that is, whether it has been set up but not shut down yet.
     */
    public final boolean isActive() {
        synchronized (this.lifecycleMonitor) {
            return this.active;
        }
    }

    /**
     * <p>If the {@link #setActiveWhenPrimary(boolean)} is set to <code>true</code> (the default), the
     * container lifecycle will be controlled by the space mode. The container will start when the
     * space is in <code>PRIMARY</code> mode, and will stop when the space is in <code>BACKUP</code>
     * mode.
     *
     * <p>Note, this might cause {@link #doStart()} or {@link #doStop()} to be called several times
     * in a row, and sub classes should take this into account.
     */
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        // TODO maybe match on the space itself, and verify that we are not being notified of the wrong space
        if (activeWhenPrimary) {
            if (applicationEvent instanceof AfterSpaceModeChangeEvent) {
                AfterSpaceModeChangeEvent spEvent = (AfterSpaceModeChangeEvent) applicationEvent;
                if (spEvent.isPrimary()) {
                    doStart();
                } else {
                    doStop();
                }
            }
        }
    }

    //-------------------------------------------------------------------------
    // Lifecycle methods for dynamically starting and stopping the container
    //-------------------------------------------------------------------------

    /**
     * Start this container.
     *
     * @see #doStart
     */
    public void start() throws GigaSpaceException {
        doStart();
    }

    /**
     * Notify all invoker tasks.
     */
    protected void doStart() throws GigaSpaceException {
        synchronized (this.lifecycleMonitor) {
            this.running = true;
            this.lifecycleMonitor.notifyAll();
            for (Iterator it = this.pausedTasks.iterator(); it.hasNext();) {
                doRescheduleTask(it.next());
                it.remove();
            }
        }
    }

    /**
     * Stop this container.
     *
     * @see #doStop
     */
    public void stop() throws GigaSpaceException {
        doStop();
    }

    /**
     * Notify all invoker tasks to stop
     */
    protected void doStop() throws GigaSpaceException {
        synchronized (this.lifecycleMonitor) {
            this.running = false;
            this.lifecycleMonitor.notifyAll();
        }
    }

    /**
     * Return whether this container is currently running,
     * that is, whether it has been started and not stopped yet.
     */
    public final boolean isRunning() {
        synchronized (this.lifecycleMonitor) {
            return this.running;
        }
    }

    /**
     * Wait while this container is not running.
     *
     * <p>To be called by asynchronous tasks that want to block
     * while the container is in stopped state.
     */
    protected final void waitWhileNotRunning() {
        synchronized (this.lifecycleMonitor) {
            while (this.active && !this.running) {
                try {
                    this.lifecycleMonitor.wait();
                }
                catch (InterruptedException ex) {
                    // Re-interrupt current thread, to allow other threads to react.
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * <p>Take the given task object and reschedule it, either immediately if
     * this container is currently running, or later once this container
     * has been restarted.
     *
     * <p>If this container has already been shut down, the task will not
     * get rescheduled at all.
     *
     * @param task the task object to reschedule
     * @return whether the task has been rescheduled
     *         (either immediately or for a restart of this container)
     * @see #doRescheduleTask
     */
    protected final boolean rescheduleTaskIfNecessary(Object task) {
        Assert.notNull(task, "Task object must not be null");
        synchronized (this.lifecycleMonitor) {
            if (this.running) {
                doRescheduleTask(task);
                return true;
            } else if (this.active) {
                this.pausedTasks.add(task);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Reschedule the given task object immediately.
     *
     * <p>To be implemented by subclasses if they ever call  <code>rescheduleTaskIfNecessary</code>.
     * This implementation throws an UnsupportedOperationException.
     *
     * @param task the task object to reschedule
     * @see #rescheduleTaskIfNecessary
     */
    protected void doRescheduleTask(Object task) {
        throw new UnsupportedOperationException(
                ClassUtils.getShortName(getClass()) + " does not support rescheduling of tasks");
    }

    //-------------------------------------------------------------------------
    // Template methods to be implemented by subclasses
    //-------------------------------------------------------------------------

    /**
     * Register any invokers within this container.
     *
     * <p>Subclasses need to implement this method for their specific
     * invoker management process.
     */
    protected abstract void doInitialize() throws GigaSpaceException;

    /**
     * Close the registered invokers.
     *
     * <p>Subclasses need to implement this method for their specific
     * invoker management process.
     *
     * @see #shutdown()
     */
    protected abstract void doShutdown() throws GigaSpaceException;


}
