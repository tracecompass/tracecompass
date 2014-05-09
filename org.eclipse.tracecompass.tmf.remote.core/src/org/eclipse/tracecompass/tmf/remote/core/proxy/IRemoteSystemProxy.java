/**********************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 *   Bernd Hufmann - Update to org.eclipse.remote API 2.0
 **********************************************************************/
package org.eclipse.tracecompass.tmf.remote.core.proxy;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.IRemoteFileService;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandShell;

/**
 * <p>
 * Remote System Proxy interface.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface IRemoteSystemProxy {

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Finds the File Service Subsystem.
     *
     * @return file service subsystem, or <code>null</code> if not found.
     */
    IRemoteFileService getRemoteFileService();

    /**
     * Returns a remote process builder for remote launching a process.
     * @param command the command to be executed.
     * @return the builder, or <code>null</code> if not possible.
     */
    IRemoteProcessBuilder getProcessBuilder(String...command);

    /**
     * Connects the shell service sub system.
     * @param monitor a monitor to report progress.
     *
     * @param callback
     *            - call-back method being called when connection was finished
     * @throws ExecutionException
     *             If the connection fails
     */
    void connect(IProgressMonitor monitor) throws ExecutionException;

    /**
     * Disconnects from the shell service sub system.
     *
     * @throws ExecutionException
     *             If the disconnect command fails
     */
    void disconnect() throws ExecutionException;

    /**
     * Disposes the proxy, may close the connection.
     */
    void dispose();

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
    void addConnectionChangeListener(IRemoteConnectionChangeListener listener);

    /**
     * Method to remove a communication listener from the connector service
     * defined for the given connection.
     *
     * @param listener
     *            - listener to remove
     */
    void removeConnectionChangeListener(IRemoteConnectionChangeListener listener);

    /**
     * @return whether the remote host is currently connected.
     */
    boolean isConnected();
}
