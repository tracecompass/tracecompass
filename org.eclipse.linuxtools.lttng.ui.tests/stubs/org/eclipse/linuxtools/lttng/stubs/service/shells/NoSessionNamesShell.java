package org.eclipse.linuxtools.lttng.stubs.service.shells;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.lttng.ui.views.control.service.CommandResult;
import org.eclipse.linuxtools.lttng.ui.views.control.service.ICommandResult;

public class NoSessionNamesShell extends TestCommandShell {

    @SuppressWarnings("nls")
    @Override
    public ICommandResult executeCommand(String command, IProgressMonitor monitor, boolean checkReturnValue) throws ExecutionException {
        String[] output = new String[1];
        output[0] = String.valueOf("Error: No Session found");
        return new CommandResult(1, output);
    }
}
