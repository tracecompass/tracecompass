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
package org.eclipse.linuxtools.lttng.ui.views.control.remote;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.linuxtools.lttng.ui.views.control.service.ICommandShell;
import org.eclipse.rse.core.model.IRSECallback;
import org.eclipse.rse.core.subsystems.ICommunicationsListener;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.services.terminals.ITerminalService;

/**
 * <b><u>IRemoteSystemProxy</u></b>
 * <p>
 * Remote System Proxy interface.
 * </p>
 */
public interface IRemoteSystemProxy {

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Find the first shell service associated with the host.
     * 
     * @return shell service object, or <code>null</code> if not found.
     */
    public IShellService getShellService();

    /**
     * Find the first terminal service associated with the host.
     * 
     * @return shell service object, or <code>null</code> if not found.
     */
    public ITerminalService getTerminalService();

    /**
     * Find the first IShellServiceSubSystem service associated with the host.
     * 
     * @return shell service subsystem, or <code>null</code> if not found.
     */
    public ISubSystem getShellServiceSubSystem();

    /**
     * Find the first ITerminalServiceSubSystem service associated with the host.
     * 
     * @param host the connection 
     * @return shell service subsystem, or <code>null</code> if not found.
     */
    public ISubSystem getTerminalServiceSubSystem();

    /**
     * Connects the shell service sub system. 
     * 
     * @param callback - call-back method being called when connection was finished
     * @throws Exception
     */
    public void connect(IRSECallback callback) throws ExecutionException;

    /**
     * Disconnects from the shell service sub system. 
     * 
     * @throws Exception
     */
    public void disconnect() throws ExecutionException;

    /**
     * Creates a command shell. 
     * 
     * @return the command shell implementation
     * @throws Exception
     */
    public ICommandShell createCommandShell() throws ExecutionException;

    /**
     * Method to add a communication listener to the connector service defined for 
     * the given connection. 
     * 
     * @param listener - listener to add
     * @throws Exception
     */
    public void addCommunicationListener(ICommunicationsListener listener);

    /**
     * Method to remove a communication listener from the connector service defined for 
     * the given connection. 
     * 
     * @param listener - listener to remove
     * @throws Exception
     */
    public void removeCommunicationListener(ICommunicationsListener listener);

}