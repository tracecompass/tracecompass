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
import org.eclipse.rse.core.model.IRSECallback;
import org.eclipse.rse.core.subsystems.ICommunicationsListener;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.services.terminals.ITerminalService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;

/**
 * <p>
 * Remote System Proxy interface.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface IRemoteSystemProxy {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Invalid port number for IP based connections.
     */
    static final int INVALID_PORT_NUMBER = -1;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Find the first shell service.
     *
     * @return shell service object, or <code>null</code> if not found.
     */
    IShellService getShellService();

    /**
     * Find the first terminal service.
     *
     * @return shell service object, or <code>null</code> if not found.
     */
    ITerminalService getTerminalService();

    /**
     * Find the first IShellServiceSubSystem service.
     *
     * @return shell service subsystem, or <code>null</code> if not found.
     */
    ISubSystem getShellServiceSubSystem();

    /**
     * Find the first ITerminalServiceSubSystem service.
     *
     * @return shell service subsystem, or <code>null</code> if not found.
     */
    ISubSystem getTerminalServiceSubSystem();

    /**
     * Finds the File Service Subsystem.
     *
     * @return file service subsystem, or <code>null</code> if not found.
     */
    IFileServiceSubSystem getFileServiceSubSystem();

    /**
     * @return port of IP connection to be used
     */
    int getPort();

    /**
     * Sets the port of the IP connection.
     * @param port - the IP port to set
     */
    void setPort(int port);

    /**
     * Connects the shell service sub system.
     *
     * @param callback
     *            - call-back method being called when connection was finished
     * @throws ExecutionException
     *             If the connection fails
     */
    void connect(IRSECallback callback) throws ExecutionException;

    /**
     * Disconnects from the shell service sub system.
     *
     * @throws ExecutionException
     *             If the disconnect command fails
     */
    void disconnect() throws ExecutionException;

    /**
     * Creates a command shell.
     *
     * @return the command shell implementation
     * @throws ExecutionException
     *             If the command fails
     */
    ICommandShell createCommandShell() throws ExecutionException;

    /**
     * Method to add a communication listener to the connector service defined
     * for the given connection.
     *
     * @param listener
     *            - listener to add
     */
    void addCommunicationListener(ICommunicationsListener listener);

    /**
     * Method to remove a communication listener from the connector service
     * defined for the given connection.
     *
     * @param listener
     *            - listener to remove
     */
    void removeCommunicationListener(ICommunicationsListener listener);

    /**
     * Checks whether the local host connector service is used or not.
     *
     * @return <code>true</code> if local connection else <false>
     *
     */
    public boolean isLocal();
}