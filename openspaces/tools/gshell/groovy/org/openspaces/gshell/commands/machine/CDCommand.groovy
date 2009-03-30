package org.openspaces.gshell.commands.machine

import org.openspaces.gshell.command.support.CommandSupport
import org.openspaces.gshell.Groovysh
import org.codehaus.groovy.tools.shell.util.SimpleCompletor
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
        def String command = args.first();
        
    }
}

class CDCommandCompletor extends SimpleCompletor
{

    final Machine machine;

    CDCommandCompletor(Machine machine) {
        this.machine = machine;
    }

    public SortedSet getCandidates() {
        def result = ["pu-instances", "gs-containers", "gs-managers", "gs-agents", "lookup-services", "space-instances", "operating-system", "transports", "virtual-machines"]
        setCandidateStrings result.toArray(new String[result.size()])
        super.getCandidates()
    }
}
