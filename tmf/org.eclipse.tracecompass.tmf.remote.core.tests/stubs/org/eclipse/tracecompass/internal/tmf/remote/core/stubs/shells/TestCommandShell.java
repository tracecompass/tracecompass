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
package org.eclipse.tracecompass.internal.tmf.remote.core.stubs.shells;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.remote.core.shell.CommandInput;
import org.eclipse.tracecompass.internal.tmf.remote.core.shell.CommandResult;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandInput;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandOutputListener;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandResult;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandShell;

/**
 * Command shell stub
 */
@NonNullByDefault
public class TestCommandShell implements ICommandShell {

    /** If the shell is connected */
    protected boolean fIsConnected = false;

    @Override
    public void dispose() {
        fIsConnected = false;
    }

    @Override
    public ICommandResult executeCommand(ICommandInput command, @Nullable IProgressMonitor monitor) throws ExecutionException {
        return executeCommand(command, monitor, null);
    }

    @Override
    public ICommandResult executeCommand(ICommandInput command, @Nullable IProgressMonitor monitor,
            @Nullable ICommandOutputListener listener) throws ExecutionException {
        if (fIsConnected) {
            return createCommandResult(0, new @NonNull String[0], new @NonNull String[0]);
        }
        return createCommandResult(1, new @NonNull String[0], new @NonNull String[0]);
    }

    @Override
    public ICommandInput createCommand() {
        return new CommandInput();
    }

    /**
     * Creates a command result
     *
     * @param result
     *            The result of the command
     * @param output
     *            The output, as an array of strings
     * @param errorOutput
     *            THe error output as an array of strings
     * @return {@link ICommandResult} instance
     */
    protected ICommandResult createCommandResult(int result, @NonNull String[] output, @NonNull String[] errorOutput) {
        return new CommandResult(result, output, errorOutput);
    }
}
