package org.openspaces.gshell.commands.main

import org.openspaces.gshell.Groovysh
import org.openspaces.gshell.command.support.CommandSupport
import org.openspaces.admin.Admin

/**
 * @author kimchy
 */
public class LSCommand extends CommandSupport {

    LSCommand(final Groovysh shell) {
        super(shell, 'ls', '\\s')
    }

    public Object execute(List list) {
        Admin admin = shell.admin
        io.out.println("@|bold machines|: [${admin.machines.size}] Machines")
    }
}