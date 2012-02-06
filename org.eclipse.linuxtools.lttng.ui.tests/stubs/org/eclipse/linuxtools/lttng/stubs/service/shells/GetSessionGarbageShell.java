package org.eclipse.linuxtools.lttng.stubs.service.shells;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.lttng.ui.views.control.service.CommandResult;
import org.eclipse.linuxtools.lttng.ui.views.control.service.ICommandResult;

public class GetSessionGarbageShell extends TestCommandShell {
    @SuppressWarnings("nls")
    @Override
    public ICommandResult executeCommand(String command, IProgressMonitor monitor, boolean checkReturnValue) throws ExecutionException {
        String[] output = new String[2];
        //Session test not found
        //Error: Session name not found
        output[0] = String.valueOf("asdfaereafsdcv 12333456434&*89**(())(^%$*");
        output[1] = String.valueOf("@#$%^&*()@#$%^&*()0834523094578kj;djkfs");
        return new CommandResult(0, output);
    }
}

