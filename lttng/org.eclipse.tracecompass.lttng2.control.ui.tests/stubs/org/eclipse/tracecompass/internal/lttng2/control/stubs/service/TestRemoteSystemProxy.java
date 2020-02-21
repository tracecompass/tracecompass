/**********************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.stubs.service;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.tracecompass.internal.lttng2.control.stubs.shells.LTTngToolsFileShell;
import org.eclipse.tracecompass.tmf.remote.core.proxy.RemoteSystemProxy;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandShell;

@SuppressWarnings("javadoc")
public class TestRemoteSystemProxy extends RemoteSystemProxy {

    public TestRemoteSystemProxy(IRemoteConnection host) {
        super(checkNotNull(host));
    }

    private LTTngToolsFileShell fShell = null;
    private String fTestFile = null;
    private String fScenario = null;
    private String fSessionName = null;

    @Override
    public void connect(IProgressMonitor monitor) throws ExecutionException {
    }

    @Override
    public void disconnect() {
        fShell = null;
    }

    @Override
    public void dispose() {
    }

    @Override
    public ICommandShell createCommandShell() {
        LTTngToolsFileShell shell = fShell;
        if (shell == null) {
            shell = new LTTngToolsFileShell();
            if ((fTestFile != null) && (fScenario != null)) {
                shell.setSessionName(fSessionName);
                shell.loadScenarioFile(fTestFile);
                shell.setScenario(fScenario);
                fShell = shell;
            }
            fShell = shell;
        }
        return shell;
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

    public void setProfileName(String profileName) {
        if (fShell != null) {
            fShell.setProfileName(profileName);
        }
    }

    public void setSessionName(String sessionName) {
        fSessionName = sessionName;
    }

    public void deleteProfileFile() {
        if (fShell != null) {
            fShell.deleteProfileFile();
        }
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}
