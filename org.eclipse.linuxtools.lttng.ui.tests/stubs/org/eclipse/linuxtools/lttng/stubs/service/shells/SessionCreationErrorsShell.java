package org.eclipse.linuxtools.lttng.stubs.service.shells;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.lttng.ui.views.control.service.CommandResult;
import org.eclipse.linuxtools.lttng.ui.views.control.service.ICommandResult;

public class SessionCreationErrorsShell extends TestCommandShell {
    @SuppressWarnings("nls")
    @Override
    public ICommandResult executeCommand(String command, IProgressMonitor monitor, boolean checkReturnValue) throws ExecutionException {
        if ("lttng create alreadyExist".equals(command)) {
            String[] output = new String[1];
            //Error: Session name already exist
            output[0] = String.valueOf("Error: Session name already exist");
            return new CommandResult(1, output);
        } else if("lttng create \"session with spaces\"".equals(command)) {
            List<String> list = new ArrayList<String>();
            list.add("Session session with spaces created.");
            list.add("Traces will be written in /home/user/lttng-traces/session with spaces-20120209-095418");
            return new CommandResult(0, list.toArray(new String[list.size()]));
        } else if ("lttng create wrongName".equals(command)) {
            List<String> list = new ArrayList<String>();
            list.add("Session auto created.");
            list.add("Traces will be written in /home/user/lttng-traces/auto-20120209-095418");
            return new CommandResult(0, list.toArray(new String[list.size()]));
        } else if ("lttng create wrongPath -o /home/user/hallo".equals(command)) {
            List<String> list = new ArrayList<String>();
            list.add("Session wrongPath created.");
            list.add("Traces will be written in /home/user/lttng-traces/wrongPath-20120209-095418");
            return new CommandResult(0, list.toArray(new String[list.size()]));
        } else if ("lttng create pathWithSpaces -o \"/home/user/hallo user/here\"".equals(command)) {
            List<String> list = new ArrayList<String>();
            list.add("Session pathWithSpaces created.");
            list.add("Traces will be written in /home/user/hallo user/here/pathWithSpaces-20120209-095418");
            return new CommandResult(0, list.toArray(new String[list.size()]));
        }
        
        String[] output = new String[1];
        output[0] = String.valueOf("Command not found");
        return new CommandResult(1, output);
    }
}

