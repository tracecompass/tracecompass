/**********************************************************************
 * Copyright (c) 2012 Ericsson
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

import org.eclipse.linuxtools.internal.lttng2.control.stubs.shells.LTTngToolsFileShell;

@SuppressWarnings("javadoc")
public class CommandShellFactory {

    public static final int GET_SESSION_NAMES_COMMAND_SHELL = 0;

    private static CommandShellFactory fInstance = null;

    public static CommandShellFactory getInstance() {
        if (fInstance == null) {
            fInstance = new CommandShellFactory();
        }
        return fInstance;
    }

//    public ICommandShell getRealShell() {
//        ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
//        IHost host = registry.getLocalHost();
//        RemoteSystemProxy proxy = new RemoteSystemProxy(host);
//        ICommandShell shell = new LTTngToolsSimulatorShell(proxy);
//        return shell;
//    }

    public LTTngToolsFileShell getFileShell() {
        return new LTTngToolsFileShell();
    }
}
