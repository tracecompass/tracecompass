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
package org.eclipse.tracecompass.tmf.remote.core.shell;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface for a command shell implementation
 *
 * @author Bernd Hufmann
 */
public interface ICommandShell {

    /**
     * Method to disconnect the command shell.
     */
    void dispose();

    /**
     * Method to execute a command on the command shell.
     *
     * @param command
     *            The command to executed
     * @param monitor
     *            A progress monitor
     * @return a {@link ICommandResult} instance
     * @throws ExecutionException
     *             If the command fails
     */
    ICommandResult executeCommand(ICommandInput command, @Nullable IProgressMonitor monitor) throws ExecutionException;

    /**
     * Method to execute a command on the command shell.
     *
     * @param command
     *            The command to executed
     * @param monitor
     *            A progress monitor
     * @param listener
     *            A command output listener (can be null if not used)
     * @return a {@link ICommandResult} instance
     * @throws ExecutionException
     *             If the command fails
     * @since 2.0
     */
    ICommandResult executeCommand(ICommandInput command, @Nullable IProgressMonitor monitor, @Nullable ICommandOutputListener listener) throws ExecutionException;

    /**
     * Creates a command input instance
     *
     * @return {@link ICommandInput} instance
     *
     */
    ICommandInput createCommand();
}
