package org.openspaces.gshell.commands.machine

import org.openspaces.gshell.command.support.CommandSupport
import org.openspaces.gshell.Groovysh
import org.openspaces.admin.machine.Machine

/**
 * @author kimchy
 */

public class LSCommand extends CommandSupport {

    LSCommand(final Groovysh shell) {
        super(shell, 'ls', '\\s')
    }

    public Object execute(List list) {
        Machine m = shell.currentContext.value
        shell.io.print "operating-system(${m.operatingSystem.details.name})"
        shell.io.print "\t"
        shell.io.print "virtual-machines(${m.virtualMachines.size})"
        shell.io.print "\t"
        shell.io.print "transports(${m.transports.size})"
        shell.io.println ""
        shell.io.print "gs-agents(${m.gridServiceAgents.size})"
        shell.io.print "\t"
        shell.io.print "gs-containers(${m.gridServiceContainers.size})"
        shell.io.print "\t"
        shell.io.print "gs-managers(${m.gridServiceManagers.size})"
        shell.io.print "\t"
        shell.io.print "lookup-services(${m.lookupServices.size})"
        shell.io.println ""
        shell.io.print "pu-instances(${m.processingUnitInstances.length})"
        shell.io.print "\t"
        shell.io.print "space-instances(${m.spaceInstances.length})"
        shell.io.println ""
        return null
    }
}