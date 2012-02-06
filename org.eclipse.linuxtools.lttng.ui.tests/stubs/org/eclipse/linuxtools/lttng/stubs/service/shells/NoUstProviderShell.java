package org.eclipse.linuxtools.lttng.stubs.service.shells;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.lttng.ui.views.control.service.CommandResult;
import org.eclipse.linuxtools.lttng.ui.views.control.service.ICommandResult;

public class NoUstProviderShell extends TestCommandShell {
    @SuppressWarnings("nls")
    @Override
    public ICommandResult executeCommand(String command, IProgressMonitor monitor, boolean checkReturnValue) throws ExecutionException {
        String[] output = new String[4];
        //Session test not found
        //Error: Session name not found
        output[0] = String.valueOf("UST events:");
        output[1] = String.valueOf("-------------");
        output[2] = String.valueOf("None");
        output[3] = String.valueOf("");
        CommandResult result = new CommandResult(0, output);
        result.setResult(0);
        return result;
    }
}

