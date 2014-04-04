/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.remote;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IRSECallback;
import org.eclipse.rse.core.subsystems.ICommunicationsListener;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.IService;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.services.terminals.ITerminalService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;

/**
 * <p>
 * RemoteSystemProxy implementation.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class RemoteSystemProxy implements IRemoteSystemProxy {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final IHost fHost;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param host
     *            The host of this proxy
     */
    public RemoteSystemProxy(IHost host) {
        fHost = host;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public IShellService getShellService() {
        ISubSystem ss = getShellServiceSubSystem();
        if (ss != null) {
            return (IShellService)ss.getSubSystemConfiguration().getService(fHost).getAdapter(IShellService.class);
        }
        return null;
    }

    @Override
    public ITerminalService getTerminalService() {
        ISubSystem ss = getTerminalServiceSubSystem();
        if (ss != null) {
            return (ITerminalService)ss.getSubSystemConfiguration().getService(fHost).getAdapter(ITerminalService.class);
        }
        return null;
    }

    @Override
    public ISubSystem getShellServiceSubSystem() {
        if (fHost == null) {
            return null;
        }
        ISubSystem[] subSystems = fHost.getSubSystems();
        IShellService ssvc = null;
        for (int i = 0; subSystems != null && i < subSystems.length; i++) {
            IService svc = subSystems[i].getSubSystemConfiguration().getService(fHost);
            if (svc!=null) {
                ssvc = (IShellService)svc.getAdapter(IShellService.class);
                if (ssvc != null) {
                    return subSystems[i];
                }
            }
        }
        return null;
    }

    @Override
    public ISubSystem getTerminalServiceSubSystem() {
        if (fHost == null) {
            return null;
        }
        ISubSystem[] subSystems = fHost.getSubSystems();
        ITerminalService ssvc = null;
        for (int i = 0; subSystems != null && i < subSystems.length; i++) {
            IService svc = subSystems[i].getSubSystemConfiguration().getService(fHost);
            if (svc!=null) {
                ssvc = (ITerminalService)svc.getAdapter(ITerminalService.class);
                if (ssvc != null) {
                    return subSystems[i];
                }
            }
        }
        return null;
    }

    @Override
    public IFileServiceSubSystem getFileServiceSubSystem() {
        if (fHost == null) {
            return null;
        }
        ISubSystem[] subSystems = fHost.getSubSystems();
        for (int i = 0; subSystems != null && i < subSystems.length; i++) {
            if (subSystems[i] instanceof IFileServiceSubSystem) {
                return (IFileServiceSubSystem)subSystems[i];
            }
        }
        return null;
    }

    @Override
    public int getPort() {
        if (getShellServiceSubSystem() != null) {
            return getShellServiceSubSystem().getConnectorService().getPort();
        }
        return IRemoteSystemProxy.INVALID_PORT_NUMBER;
    }

    @Override
    public void setPort(int port) {
        if ((getShellServiceSubSystem() != null) && (port > 0)) {
            getShellServiceSubSystem().getConnectorService().setPort(port);
        }
    }

    @Override
    public void connect(IRSECallback callback) throws ExecutionException {
        ISubSystem shellSubSystem = getShellServiceSubSystem();
        if (shellSubSystem != null) {
            if (!shellSubSystem.isConnected()) {
                try {
                    shellSubSystem.connect(false, callback);
                } catch (OperationCanceledException e) {
                    callback.done(Status.CANCEL_STATUS, null);
                }
                catch (Exception e) {
                    throw new ExecutionException(e.toString(), e);
                }
            } else {
                callback.done(Status.OK_STATUS, null);
            }
        }
    }

    @Override
    public void disconnect() throws ExecutionException {
            ISubSystem shellSubSystem = getShellServiceSubSystem();
            if (shellSubSystem != null) {
                try {
                    shellSubSystem.disconnect();
                } catch (Exception e) {
                    throw new ExecutionException(e.toString(), e);
                }
            }
    }

    @Override
    public ICommandShell createCommandShell() throws ExecutionException {
        ICommandShell shell = new CommandShell(this);
        shell.connect();
        return shell;
    }

    @Override
    public void addCommunicationListener(ICommunicationsListener listener) {
        IConnectorService[] css = fHost.getConnectorServices();
        for (IConnectorService cs : css) {
            cs.addCommunicationsListener(listener);
        }
    }

    @Override
    public void removeCommunicationListener(ICommunicationsListener listener) {
        IConnectorService[] css = fHost.getConnectorServices();
        for (IConnectorService cs : css) {
            cs.removeCommunicationsListener(listener);
        }
    }

    @Override
    public boolean isLocal() {
        return fHost.getSystemType().isLocal();
    }
}
