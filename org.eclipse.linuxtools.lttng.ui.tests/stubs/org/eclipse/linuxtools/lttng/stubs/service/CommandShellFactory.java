package org.eclipse.linuxtools.lttng.stubs.service;

import org.eclipse.linuxtools.lttng.stubs.service.shells.GetSessionGarbageShell;
import org.eclipse.linuxtools.lttng.stubs.service.shells.LttngNotExistsShell;
import org.eclipse.linuxtools.lttng.stubs.service.shells.NoSessionNamesShell;
import org.eclipse.linuxtools.lttng.stubs.service.shells.NoUstProviderShell;
import org.eclipse.linuxtools.lttng.stubs.service.shells.SessionCreationErrorsShell;
import org.eclipse.linuxtools.lttng.stubs.service.shells.SessionNotExistsShell;
import org.eclipse.linuxtools.lttng.stubs.service.shells.SessionNamesShell;
import org.eclipse.linuxtools.lttng.ui.views.control.service.ICommandShell;

public class CommandShellFactory {

    public static final int GET_SESSION_NAMES_COMMAND_SHELL = 0;
    
    private static CommandShellFactory fInstance = null;
    
    public static CommandShellFactory getInstance() {
        if (fInstance == null) {
            fInstance = new CommandShellFactory();
        }
        return fInstance;
    }

    public ICommandShell getShellForNoSessionNames() {
        return new NoSessionNamesShell();
    }

    public ICommandShell getShellForSessionNames() {
        return new SessionNamesShell();
    }

    public ICommandShell getShellForLttngNotExistsShell() {
        return new LttngNotExistsShell();
    }
    
    public ICommandShell getShellForSessionNotExists() {
        return new SessionNotExistsShell();
    }
    
    public ICommandShell getShellForSessionGarbage() {
        return new GetSessionGarbageShell();
    }

    public ICommandShell getShellForNoUstProvider() {
        return new NoUstProviderShell();
    }
    
    public ICommandShell getShellForSessionErrors() {
        return new SessionCreationErrorsShell();
    }
}
