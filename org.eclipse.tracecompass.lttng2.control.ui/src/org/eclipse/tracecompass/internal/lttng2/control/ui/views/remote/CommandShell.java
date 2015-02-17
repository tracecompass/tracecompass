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
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.remote;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessService;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.preferences.ControlPreferences;

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
    private IRemoteConnection fConnection = null;
    private final ExecutorService fExecutor = Executors.newFixedThreadPool(1);

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
    public void connect() throws ExecutionException {
    }

    @Override
    public void disconnect() {
        fExecutor.shutdown();
    }

    @Override
    public ICommandResult executeCommand(final List<String> command, final IProgressMonitor monitor) throws ExecutionException {
        if (fConnection.isOpen()) {
            FutureTask<CommandResult> future = new FutureTask<>(new Callable<CommandResult>() {
                @Override
                public CommandResult call() throws IOException, InterruptedException {
                    if (monitor == null || !monitor.isCanceled()) {
                        IRemoteProcess process = fConnection.getService(IRemoteProcessService.class).getProcessBuilder(command).start();
                        InputReader stdout = new InputReader(process.getInputStream());
                        InputReader stderr = new InputReader(process.getErrorStream());

                        try {
                            stdout.waitFor(monitor);
                            stderr.waitFor(monitor);
                            if (monitor == null || !monitor.isCanceled()) {
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
                return future.get(ControlPreferences.getInstance().getCommandTimeout(), TimeUnit.SECONDS);
            } catch (java.util.concurrent.ExecutionException ex) {
                throw new ExecutionException(Messages.TraceControl_ExecutionFailure, ex);
            } catch (InterruptedException ex) {
                throw new ExecutionException(Messages.TraceControl_ExecutionCancelled, ex);
            } catch (TimeoutException ex) {
                throw new ExecutionException(Messages.TraceControl_ExecutionTimeout, ex);
            } finally {
                future.cancel(true);
            }
        }
        throw new ExecutionException(Messages.TraceControl_ShellNotConnected, null);
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

    private static String[] splitLines(String output) {
        if (output == null) {
            return null;
        }
        return output.split("\\r?\\n"); //$NON-NLS-1$
    }

}
