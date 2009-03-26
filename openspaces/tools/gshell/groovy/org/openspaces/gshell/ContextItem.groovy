package org.openspaces.gshell
/**
 * @author kimchy
 */

class ContextItem {

    final String name

    final String type

    final Object value

    ContextItem(String name, String type, Object value) {
        this.name = name
        this.type = type
        this.value = value
    }
}