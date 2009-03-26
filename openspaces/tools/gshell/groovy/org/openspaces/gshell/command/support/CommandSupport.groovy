package org.openspaces.gshell.command.support

import org.openspaces.gshell.Groovysh

/**
 * @author kimchy
 */

abstract class CommandSupport extends org.codehaus.groovy.tools.shell.CommandSupport {

    protected final Groovysh shell

    protected CommandSupport(final Groovysh shell, final String name, final String shortcut) {
        super(shell, name, shortcut)
        this.shell = shell; 
    }    
}