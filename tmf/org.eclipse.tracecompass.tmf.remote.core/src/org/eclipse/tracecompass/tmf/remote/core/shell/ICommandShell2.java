/**********************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.tmf.remote.core.shell;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface for a command shell implementation
 *
 * @author Bernd Hufmann
 * @since 1.1
 */
public interface ICommandShell2 extends ICommandShell {

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
     */
    ICommandResult executeCommand(ICommandInput command, @Nullable IProgressMonitor monitor, @Nullable ICommandOutputListener listener) throws ExecutionException;
}
