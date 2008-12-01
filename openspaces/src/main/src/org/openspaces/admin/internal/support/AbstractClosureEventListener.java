package org.openspaces.admin.internal.support;

import groovy.lang.Closure;

/**
 * @author kimchy
 */
public class AbstractClosureEventListener {

    private final Closure closure;

    public AbstractClosureEventListener(Object closure) {
        this.closure = (Closure) closure;
    }

    protected Closure getClosure() {
        return this.closure;
    }

    @Override
    public int hashCode() {
        return closure.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return closure.equals(((AbstractClosureEventListener) obj).closure);
    }
}
