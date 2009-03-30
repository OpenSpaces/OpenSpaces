package org.openspaces.gshell
/**
 * @author kimchy
 */

class ContextItem {

    final String name

    final String type

    final Object value

    final Closure context

    ContextItem(String name, String type, Object value, Closure context) {
        this.name = name
        this.type = type
        this.value = value
        this.context = context
    }
}