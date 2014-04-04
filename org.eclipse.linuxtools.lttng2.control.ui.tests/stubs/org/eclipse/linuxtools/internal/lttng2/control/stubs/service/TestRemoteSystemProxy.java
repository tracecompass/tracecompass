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
package org.eclipse.linuxtools.internal.lttng2.control.stubs.service;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.lttng2.control.stubs.shells.LTTngToolsFileShell;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.remote.ICommandShell;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.remote.IRemoteSystemProxy;
import org.eclipse.rse.core.model.IRSECallback;
import org.eclipse.rse.core.subsystems.ICommunicationsListener;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.services.terminals.ITerminalService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;

@SuppressWarnings("javadoc")
public class TestRemoteSystemProxy implements IRemoteSystemProxy {

    private LTTngToolsFileShell fShell = null;
    private String fTestFile = null;
    private String fScenario = null;

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
    public IFileServiceSubSystem getFileServiceSubSystem() {
        return null;
    }

    @Override
    public void connect(IRSECallback callback) throws ExecutionException {
//        System.out.println("in done: proxy connect ");
        if (callback != null) {
            callback.done(Status.OK_STATUS, null);
        }
    }

    @Override
    public void disconnect() throws ExecutionException {
        fShell = null;
    }

    @Override
    public ICommandShell createCommandShell() throws ExecutionException {
        if (fShell == null) {
            fShell = CommandShellFactory.getInstance().getFileShell();
            if ((fTestFile != null) && (fScenario != null)) {
                try {
                    fShell.loadScenarioFile(fTestFile);
                } catch (Exception e) {
                    throw new ExecutionException(e.toString());
                }
                fShell.setScenario(fScenario);
            }
        }
        return fShell;
    }

    @Override
    public void addCommunicationListener(ICommunicationsListener listener) {
    }

    @Override
    public void removeCommunicationListener(ICommunicationsListener listener) {
    }

    public void setTestFile(String testFile) {
        fTestFile = testFile;
    }

    public void setScenario(String scenario) {
        fScenario = scenario;
        if (fShell != null) {
            fShell.setScenario(fScenario);
        }
    }

    @Override
    public int getPort() {
        return IRemoteSystemProxy.INVALID_PORT_NUMBER;
    }

    @Override
    public void setPort(int port) {
    }

    @Override
    public boolean isLocal() {
        return false;
    }
}
