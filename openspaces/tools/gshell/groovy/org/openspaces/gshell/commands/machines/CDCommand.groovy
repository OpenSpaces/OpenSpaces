package org.openspaces.gshell.commands.machines

import org.openspaces.gshell.Groovysh
import org.openspaces.gshell.command.support.CommandSupport
import org.codehaus.groovy.tools.shell.util.SimpleCompletor
import org.openspaces.admin.machine.Machines
import org.openspaces.admin.machine.Machine

/**
 * @author kimchy
 */
public class CDCommand extends CommandSupport {

    CDCommand(final Groovysh shell) {
        super(shell, 'cd', '\\c')
    }

    protected List createCompletors() {
        return [
            new CDCommandCompletor(shell.currentContext.value),
            null
        ]
    }
    
    public Object execute(List args) {
        assert args != null
        if (args.isEmpty()) {
            fail("Command 'cd' requires one") // TODO: i18n
        }
        def command = args.first();
        switch (command) {
            case "..":
                shell.removeContext()
                break;
            default:
                fail("Paramaeter ${command} unknown") // TODO: i18n
                break;
        }
    }
}

class CDCommandCompletor extends SimpleCompletor
{

    final Machines machines;

    CDCommandCompletor(Machines machines) {
        this.machines = machines;
    }

    public SortedSet getCandidates() {
        def result = [".."]
        machines.each { Machine m -> result.add(m.operatingSystem.details.hostName) }
        machines.each { Machine m -> result.add(m.operatingSystem.details.hostAddress) }
        setCandidateStrings result.toArray(new String[result.size()])
        super.getCandidates()
    }
}
