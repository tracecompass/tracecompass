/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
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
    // Constants
    // ------------------------------------------------------------------------

    private static final String BEGIN_TAG = "org.eclipse.tracecompass-BEGIN-TAG:"; //$NON-NLS-1$
    private static final String END_TAG = "org.eclipse.tracecompass-END-TAG:"; //$NON-NLS-1$
    private static final String RSE_ADAPTER_ID = "org.eclipse.ptp.remote.RSERemoteServices"; //$NON-NLS-1$
    private static final String SHELL_ECHO_CMD = "echo "; //$NON-NLS-1$
    private static final char CMD_SEPARATOR = ';';
    private static final String CMD_RESULT_VAR = " $?"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private IRemoteConnection fConnection = null;
    private final ExecutorService fExecutor = Executors.newFixedThreadPool(1);
    private int fBackedByShell;

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
                        final boolean wrapCommand =
                                RSE_ADAPTER_ID.equals(fConnection.getRemoteServices().getId())
                                && isBackedByShell();
                        IRemoteProcess process = startRemoteProcess(wrapCommand, command);
                        InputReader stdout = new InputReader(process.getInputStream());
                        InputReader stderr = new InputReader(process.getErrorStream());

                        try {
                            stdout.waitFor(monitor);
                            stderr.waitFor(monitor);
                            if (monitor == null || !monitor.isCanceled()) {
                                return createResult(wrapCommand, process.waitFor(), stdout.toString(), stderr.toString());
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

    private IRemoteProcess startRemoteProcess(boolean wrapCommand, List<String> command) throws IOException {
        if (wrapCommand) {
            StringBuilder formattedCommand = new StringBuilder();
            formattedCommand.append(SHELL_ECHO_CMD).append(BEGIN_TAG);
            formattedCommand.append(CMD_SEPARATOR);
            for(String cmd : command) {
                formattedCommand.append(cmd).append(' ');
            }
            formattedCommand.append(CMD_SEPARATOR);
            formattedCommand.append(SHELL_ECHO_CMD).append(END_TAG).append(CMD_RESULT_VAR);
            String[] args = formattedCommand.toString().trim().split("\\s+"); //$NON-NLS-1$
            return fConnection.getProcessBuilder(args).start();
        }

        return fConnection.getProcessBuilder(command).start();
    }

    private boolean isBackedByShell() throws InterruptedException {
        if (fBackedByShell == 0) {
            String cmd= SHELL_ECHO_CMD + BEGIN_TAG + CMD_SEPARATOR + SHELL_ECHO_CMD + END_TAG;
            IRemoteProcessBuilder pb = fConnection.getProcessBuilder(cmd.trim().split("\\s+")); //$NON-NLS-1$
            pb.redirectErrorStream(true);
            IRemoteProcess process = null;
            InputReader reader = null;
            try {
                process = pb.start();
                reader = new InputReader(process.getInputStream());
                reader.waitFor(new NullProgressMonitor());
                process.waitFor();

                fBackedByShell = -1;
                String result= reader.toString();
                int pos = result.indexOf(BEGIN_TAG, skipEchoBeginTag(result));
                if (pos >= 0 && result.substring(pos + BEGIN_TAG.length()).trim().startsWith(END_TAG)) {
                    fBackedByShell = 1;
                }
            } catch (IOException e) {
                // On Windows, cannot start built-in echo command
                fBackedByShell = -1;
            } finally {
                if (process != null) {
                    process.destroy();
                }
                if (reader != null) {
                    reader.stop();
                }
            }
        }
        return fBackedByShell == 1;
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    private static CommandResult createResult(boolean isWrapped, int origResult, String origStdout, String origStderr) {
        final int result;
        final String stdout, stderr;
        if (isWrapped) {
            String[] holder = {origStdout};
            result = unwrapOutput(holder);
            stdout = holder[0];
            // Workaround if error stream is not available and stderr output is written
            // in standard output above. This is true for the SshTerminalShell implementation.
            stderr = origStderr.isEmpty() ? stdout : origStderr;
        } else {
            result = origResult;
            stdout = origStdout;
            stderr = origStderr;
        }

        String[] output = splitLines(stdout);
        String[] error = result == 0 ? null : splitLines(stderr);
        return new CommandResult(result, output, error);
    }

    private static String[] splitLines(String output) {
        if (output == null) {
            return null;
        }
        return output.split("\\r?\\n"); //$NON-NLS-1$
    }

    private static int unwrapOutput(String[] outputHolder) {
        String output = outputHolder[0];
        int begin = skipEchoBeginTag(output);
        begin = output.indexOf(BEGIN_TAG, begin);

        if (begin < 0) {
            outputHolder[0] = ""; //$NON-NLS-1$
            return 1;
        }

        begin += BEGIN_TAG.length();
        int end = output.indexOf(END_TAG, begin);
        if (end < 0) {
            outputHolder[0] = output.substring(begin).trim();
            return 1;
        }

        outputHolder[0] = output.substring(begin, end).trim();
        String tail = output.substring(end + END_TAG.length()).trim();
        int numEnd;
        for (numEnd = 0; numEnd < tail.length(); numEnd++) {
            if (!Character.isDigit(tail.charAt(numEnd))) {
                break;
            }
        }
        try {
            return Integer.parseInt(tail.substring(0, numEnd));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private static int skipEchoBeginTag(String output) {
        final String searchFor = SHELL_ECHO_CMD + BEGIN_TAG;
        int begin = 0;
        for(;;) {
            int i= output.indexOf(searchFor, begin);
            if (i >= begin) {
                begin = i + searchFor.length();
            } else {
                return begin;
            }
        }
    }
}
