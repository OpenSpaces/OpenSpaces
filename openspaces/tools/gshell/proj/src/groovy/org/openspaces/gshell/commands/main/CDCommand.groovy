package org.openspaces.gshell.commands.main

import org.openspaces.gshell.Groovysh
import org.openspaces.admin.machine.Machines
import org.openspaces.gshell.command.support.CommandSupport

/**
 * @author kimchy
 */
public class CDCommand extends CommandSupport {

    CDCommand(final Groovysh shell) {
        super(shell, 'cd', '\\c')
    }

    public Object execute(List args) {
        assert args != null
        if (args.isEmpty()) {
            fail("Command 'cd' requires one") // TODO: i18n
        }
        def command = args.first();
        switch (command) {
            case "machines":
                Machines machines = shell.admin.machines
                shell.cdToContext(new ContextItem("machines", "machines", machines))
                break;
            default:
                fail("Paramaeter ${command} unknown") // TODO: i18n
                break;
        }
    }
}