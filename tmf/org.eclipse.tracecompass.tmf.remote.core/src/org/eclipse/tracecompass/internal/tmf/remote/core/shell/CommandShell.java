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
 *   Patrick Tasse - Initial API and implementation
 *   Bernd Hufmann - Updated using Executor Framework
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 *   Bernd Hufmann - Update to org.eclipse.remote API 2.0
 **********************************************************************/
package org.eclipse.tracecompass.internal.tmf.remote.core.shell;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessService;
import org.eclipse.tracecompass.internal.tmf.remote.core.messages.Messages;
import org.eclipse.tracecompass.internal.tmf.remote.core.preferences.TmfRemotePreferences;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandInput;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandOutputListener;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandResult;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandShell;

/**
 * <p>
 * Implementation of remote command execution using IRemoteConnection.
 * </p>
 *
 * @author Patrick Tasse
 * @author Bernd Hufmann
 */
public class CommandShell implements ICommandShell {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private final IRemoteConnection fConnection;
    private final ExecutorService fExecutor = checkNotNull(Executors.newFixedThreadPool(1));

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Create a new command shell
     *
     * @param connection the remote connection for this shell
     */
    public CommandShell(IRemoteConnection connection) {
        fConnection = connection;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void dispose() {
        fExecutor.shutdown();
    }

    @Override
    public ICommandInput createCommand() {
        return new CommandInput();
    }

    @Override
    public ICommandResult executeCommand(final ICommandInput command, final IProgressMonitor aMonitor) throws ExecutionException {
        return executeCommand(command, aMonitor, null);
    }

    @Override
    public ICommandResult executeCommand(final ICommandInput command, final IProgressMonitor aMonitor, ICommandOutputListener listener) throws ExecutionException {
        if (fConnection.isOpen()) {
            FutureTask<CommandResult> future = new FutureTask<>(new Callable<CommandResult>() {
                @Override
                public CommandResult call() throws IOException, InterruptedException {
                    IProgressMonitor monitor = aMonitor;
                    if (monitor == null) {
                        monitor = new NullProgressMonitor();
                    }
                    if (!monitor.isCanceled()) {
                        IRemoteProcessService service = fConnection.getService(IRemoteProcessService.class);
                        if (service == null) {
                            return new CommandResult(1, new @NonNull String[0], new @NonNull String[] { nullToEmptyString(Messages.RemoteConnection_ServiceNotDefined) });
                        }
                        IRemoteProcess process = service.getProcessBuilder(command.getInput()).start();
                        InputReader stdout = new InputReader(checkNotNull(process.getInputStream()), listener, true);
                        InputReader stderr = new InputReader(checkNotNull(process.getErrorStream()), listener, false);

                        try {
                            stdout.waitFor(monitor);
                            stderr.waitFor(monitor);
                            if (!monitor.isCanceled()) {
                                return createResult(process.waitFor(), stdout.toString(), stderr.toString());
                            }
                        } catch (OperationCanceledException e) {
                        } catch (InterruptedException e) {
                            return new CommandResult(1, new @NonNull String[0],
                                    new @NonNull String[] { checkNotNull(e.getMessage()) });
                        } finally {
                            stdout.stop();
                            stderr.stop();
                            process.destroy();
                        }
                    }
                    return new CommandResult(1, new @NonNull String[0], new @NonNull String[] { "cancelled" }); //$NON-NLS-1$
                }
            });

            fExecutor.execute(future);

            try {
                return checkNotNull(future.get(TmfRemotePreferences.getCommandTimeout(), TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                throw new ExecutionException(Messages.RemoteConnection_ExecutionCancelled, ex);
            } catch (TimeoutException ex) {
                throw new ExecutionException(Messages.RemoteConnection_ExecutionTimeout, ex);
            } catch (Exception ex) {
                throw new ExecutionException(Messages.RemoteConnection_ExecutionFailure, ex);
            }
            finally {
                future.cancel(true);
            }
        }
        throw new ExecutionException(Messages.RemoteConnection_ShellNotConnected, null);
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    private static CommandResult createResult(int origResult, String origStdout, String origStderr) {
        final int result;
        final String stdout, stderr;
        result = origResult;
        stdout = origStdout;
        stderr = origStderr;
        @NonNull String[] output = splitLines(stdout);
        @NonNull String[] error = splitLines(stderr);
        return new CommandResult(result, output, error);
    }

    private static @NonNull String @NonNull [] splitLines(String output) {
        return output.split("\\r?\\n"); //$NON-NLS-1$
    }
}
