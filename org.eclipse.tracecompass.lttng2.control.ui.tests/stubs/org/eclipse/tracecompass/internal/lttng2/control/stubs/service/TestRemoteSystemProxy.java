/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.stubs.service;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.IRemoteFileManager;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.tracecompass.internal.lttng2.control.stubs.shells.LTTngToolsFileShell;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.remote.ICommandShell;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.remote.IRemoteSystemProxy;

@SuppressWarnings("javadoc")
public class TestRemoteSystemProxy implements IRemoteSystemProxy {

    private LTTngToolsFileShell fShell = null;
    private String fTestFile = null;
    private String fScenario = null;

    @Override
    public IRemoteProcessBuilder getProcessBuilder(String... command) {
        return null;
    }

    @Override
    public IRemoteFileManager getFileServiceSubSystem() {
        return null;
    }

    @Override
    public void connect(IProgressMonitor monitor) throws ExecutionException {
    }

    @Override
    public void disconnect() throws ExecutionException {
        fShell = null;
    }

    @Override
    public void dispose() {
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
    public void addConnectionChangeListener(IRemoteConnectionChangeListener listener) {
    }

    @Override
    public void removeConnectionChangeListener(IRemoteConnectionChangeListener listener) {
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
    public boolean isConnected() {
        return true;
    }
}
