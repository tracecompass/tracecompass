package org.eclipse.linuxtools.lttng.stubs.service;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.lttng.stubs.service.shells.TestCommandShell;
import org.eclipse.linuxtools.lttng.ui.views.control.remote.IRemoteSystemProxy;
import org.eclipse.linuxtools.lttng.ui.views.control.service.ICommandShell;
import org.eclipse.rse.core.model.IRSECallback;
import org.eclipse.rse.core.subsystems.ICommunicationsListener;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.services.terminals.ITerminalService;

public class TestRemoteSystemProxy implements IRemoteSystemProxy {

    @Override
    public IShellService getShellService() {
        return null;
    }

    @Override
    public ITerminalService getTerminalService() {
        return null;
    }

    @Override
    public ISubSystem getShellServiceSubSystem() {
        return null;
    }

    @Override
    public ISubSystem getTerminalServiceSubSystem() {
        return null;
    }

    @Override
    public void connect(IRSECallback callback) throws ExecutionException {
        if (callback != null) {
            callback.done(Status.OK_STATUS, null);
        }
    }

    @Override
    public void disconnect() throws ExecutionException {
    }

    @Override
    public ICommandShell createCommandShell() throws ExecutionException {
        ICommandShell shell = CommandShellFactory.getInstance().getShellForSessionNames();
        return shell;
    }

    @Override
    public void addCommunicationListener(ICommunicationsListener listener) {
    }

    @Override
    public void removeCommunicationListener(ICommunicationsListener listener) {
    }

}
