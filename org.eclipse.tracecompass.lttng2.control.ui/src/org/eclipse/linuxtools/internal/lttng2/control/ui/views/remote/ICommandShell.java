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
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * <p>
 * Interface for a command shell implementation
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface ICommandShell {

    /**
     * Method to connect the command shell.
     *
     * @throws ExecutionException
     *             If the command fails
     */
    void connect() throws ExecutionException;

    /**
     * Method to disconnect the command shell.
     */
    void disconnect();

    /**
     * Method to execute a command on the command shell.
     *
     * @param command
     *            - the command to executed
     * @param monitor
     *            - a progress monitor
     * @return the command result
     * @throws ExecutionException
     *             If the command fails
     */
    ICommandResult executeCommand(String command,
            IProgressMonitor monitor) throws ExecutionException;

    /**
     * Method to execute a command on the command shell.
     *
     * @param command
     *            - the command to executed
     * @param monitor
     *            - a progress monitor
     * @param checkReturnValue
     *            - flag to indicate that the command result should be checked.
     *            If false the command result will be always 0.
     * @return the command result
     * @throws ExecutionException
     *             If the command fails
     */
    ICommandResult executeCommand(final String command,
            final IProgressMonitor monitor, final boolean checkReturnValue)
            throws ExecutionException;

}
