/**********************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Bernd Hufmann - Updated using Executor Framework
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 *   Bernd Hufmann - Update to org.eclipse.remote API 2.0
 **********************************************************************/
package org.eclipse.tracecompass.internal.tmf.remote.core.shell;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

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
        if (fConnection.isOpen()) {
            FutureTask<CommandResult> future = new FutureTask<>(new Callable<CommandResult>() {
                @Override
                public CommandResult call() throws IOException, InterruptedException {
                    IProgressMonitor monitor = aMonitor;
                    if (monitor == null) {
                        monitor = new NullProgressMonitor();
                    }
                    if (!monitor.isCanceled()) {
                        IRemoteProcess process = fConnection.getService(IRemoteProcessService.class).getProcessBuilder(command.getInput()).start();
                        InputReader stdout = new InputReader(checkNotNull(process.getInputStream()));
                        InputReader stderr = new InputReader(checkNotNull(process.getErrorStream()));

                        try {
                            stdout.waitFor(monitor);
                            stderr.waitFor(monitor);
                            if (!monitor.isCanceled()) {
                                return createResult(process.waitFor(), stdout.toString(), stderr.toString());
                            }
                        } catch (OperationCanceledException e) {
                        } catch (InterruptedException e) {
                            return new CommandResult(1, new String[0], new String[] {e.getMessage()});
                        } finally {
                            stdout.stop();
                            stderr.stop();
                            process.destroy();
                        }
                    }
                    return new CommandResult(1, new String[0], new String[] {"cancelled"}); //$NON-NLS-1$
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
        String[] output = splitLines(stdout);
        String[] error = splitLines(stderr);
        return new CommandResult(result, output, error);
    }

    private static @NonNull String[] splitLines(String output) {
        return checkNotNull(output.split("\\r?\\n")); //$NON-NLS-1$
    }
}
