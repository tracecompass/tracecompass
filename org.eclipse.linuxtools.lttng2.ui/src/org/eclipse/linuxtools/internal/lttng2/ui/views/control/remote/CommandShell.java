/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Bernd Hufmann - Updated using Executor Framework
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.preferences.ControlPreferences;
import org.eclipse.rse.services.shells.HostShellProcessAdapter;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;

/**
 * <p>
 * Implementation of remote command execution using RSE's shell service.
 * </p>
 *
 * @author Patrick Tasse
 * @author Bernd Hufmann
 */
public class CommandShell implements ICommandShell {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /** Sub-string to be echo'ed when running command in shell, used to indicate that the command has finished running */
    public static final String DONE_MARKUP_STRING = "--RSE:donedonedone:--"; //$NON-NLS-1$

    /** Sub-string to be echoed when running a command in shell. */
    public static final String BEGIN_END_TAG = "BEGIN-END-TAG:"; //$NON-NLS-1$

    /** Command delimiter for shell */
    public static final String CMD_DELIMITER = "\n"; //$NON-NLS-1$

    /** Shell "echo" command */
    public static final String SHELL_ECHO_CMD = " echo "; //$NON-NLS-1$

    /** Default command separator */
    public static final char CMD_SEPARATOR = ';';

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private IRemoteSystemProxy fProxy = null;
    private IHostShell fHostShell = null;
    private BufferedReader fInputBufferReader = null;
    private BufferedReader fErrorBufferReader = null;
    private final ExecutorService fExecutor = Executors.newFixedThreadPool(1);
    private boolean fIsConnected = false;
    private final Random fRandom = new Random(System.currentTimeMillis());
    private int fReturnValue;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Create a new command shell
     *
     * @param proxy
     *            The RSE proxy for this shell
     */
    public CommandShell(IRemoteSystemProxy proxy) {
        fProxy = proxy;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void connect() throws ExecutionException {
        IShellService shellService = fProxy.getShellService();
        Process p = null;
        try {
            fHostShell = shellService.launchShell("", new String[0], new NullProgressMonitor()); //$NON-NLS-1$
            p = new HostShellProcessAdapter(fHostShell);
        } catch (Exception e) {
            throw new ExecutionException(Messages.TraceControl_CommandShellError, e);
        }
        fInputBufferReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        fErrorBufferReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        fIsConnected = true;
    }

    @Override
    public void disconnect() {
        fIsConnected = false;
        try {
            fInputBufferReader.close();
            fErrorBufferReader.close();
        } catch (IOException e) {
            // ignore
        }
    }

    @Override
    public ICommandResult executeCommand(String command, IProgressMonitor monitor) throws ExecutionException {
        return executeCommand(command, monitor, true);
    }

    @Override
    public ICommandResult executeCommand(final String command, final IProgressMonitor monitor, final boolean checkReturnValue) throws ExecutionException {
        if (fIsConnected) {
            FutureTask<CommandResult> future = new FutureTask<>(new Callable<CommandResult>() {
                @Override
                public CommandResult call() throws IOException, CancellationException {
                    final ArrayList<String> result = new ArrayList<>();

                    synchronized (fHostShell) {
                        // Initialize return value which will be updated in isAliasEchoResult()
                        fReturnValue = 0;

                        int startAlias = fRandom.nextInt();
                        int endAlias = fRandom.nextInt();
                        fHostShell.writeToShell(formatShellCommand(command, startAlias, endAlias));

                        String nextLine;
                        boolean isStartFound = false;
                        while ((nextLine = fInputBufferReader.readLine()) != null) {

                            if (monitor.isCanceled()) {
                                flushInput();
                                throw new CancellationException();
                            }

                            // check if line contains echoed start alias
                            if (isAliasEchoResult(nextLine, startAlias, true)) {
                                isStartFound = true;
                                continue;
                            }

                            // check if line contains is the end mark-up. This will retrieve also
                            // the return value of the actual command.
                            if (isAliasEchoResult(nextLine, endAlias, false)) {
                                break;
                            }

                            // Ignore line if
                            // 1) start hasn't been found or
                            // 2) line is an echo of the command or
                            // 3) line is  an echo of the end mark-up
                            if (!isStartFound ||
                                    isCommandEcho(nextLine, command) ||
                                    nextLine.contains(getEchoResult(endAlias)))
                            {
                                continue;
                            }

                            // Now it's time add to the result
                            result.add(nextLine);
                        }

                        // Read any left over output
                        flushInput();

                        // Read error stream output when command failed.
                        if (fReturnValue != 0) {
                            while(fErrorBufferReader.ready()) {
                                if ((nextLine = fErrorBufferReader.readLine()) != null)  {
                                    result.add(nextLine);
                                }
                            }
                        }
                    }
                    return new CommandResult(fReturnValue, result.toArray(new String[result.size()]));
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
            }
        }
        throw new ExecutionException(Messages.TraceControl_ShellNotConnected, null);
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------
    /**
     * Flushes the buffer reader
     * @throws IOException
     */
    private void flushInput() throws IOException {
        char[] cbuf = new char[1];
        while (fInputBufferReader.ready()) {
            if (fInputBufferReader.read(cbuf, 0, 1) == -1) {
                break;
            }
        }
    }

    /**
     * Format the command to be sent into the shell command with start and end marker strings.
     * The start marker is need to know when the actual command output starts. The end marker
     * string is needed so we can tell that end of output has been reached.
     *
     * @param cmd The actual command
     * @param startAlias The command alias for start marker
     * @param endAlias The command alias for end marker
     * @return formatted command string
     */
    private static String formatShellCommand(String cmd, int startAlias, int endAlias) {
        if (cmd == null || cmd.equals("")) { //$NON-NLS-1$
            return cmd;
        }
        StringBuffer formattedCommand = new StringBuffer();
        // Make multi-line command.
        // Wrap actual command with start marker and end marker to wrap actual command.
        formattedCommand.append(getEchoCmd(startAlias));
        formattedCommand.append(CMD_DELIMITER);
        formattedCommand.append(cmd);
        formattedCommand.append(CMD_DELIMITER);
        formattedCommand.append(getEchoCmd(endAlias));
        formattedCommand.append(CMD_DELIMITER);
        return formattedCommand.toString();
    }

    /**
     * Creates a echo command line in the format: echo <start tag> <alias> <end tag> $?
     *
     * @param alias The command alias integer to be included in the echoed message.
     * @return the echo command line
     */
    private static String getEchoCmd(int alias) {
        return SHELL_ECHO_CMD + getEchoResult(alias) + "$?"; //$NON-NLS-1$
    }

    /**
     * Creates the expected result for a given command alias:
     * <start tag> <alias> <end tag> $?
     *
     * @param alias The command alias integer to be included in the echoed message.
     * @return the expected echo result
     */
    private static String getEchoResult(int alias) {
        return BEGIN_END_TAG + String.valueOf(alias) + DONE_MARKUP_STRING;
    }

    /**
     * Verifies if given command line contains a command alias echo result.
     *
     * @param line The output line to test.
     * @param alias The command alias
     * @param checkReturnValue <code>true</code> to retrieve command result (previous command) <code>false</code>
     * @return <code>true</code> if output line is a command alias echo result else <code>false</code>
     */
    private boolean isAliasEchoResult(String line, int alias, boolean checkReturnValue) {
        String expected = getEchoResult(alias);
        if (line.startsWith(expected)) {
            if (!checkReturnValue) {
                try {
                    int k = Integer.valueOf(line.substring(expected.length()));
                    fReturnValue = k;
                } catch (NumberFormatException e) {
                    // do nothing
                }
            }
            return true;
        }
        int index = line.indexOf(expected);
        if ((index > 0) && (line.indexOf(SHELL_ECHO_CMD) == -1)) {
            return true;
        }

        return false;
    }

    /**
     * Verifies if output line is an echo of the given command line. If the
     * output line is longer then the maximum line lengths (e.g. for ssh), the
     * shell adds a line break character. This method takes this in
     * consideration by comparing the command line without any whitespaces.
     *
     * @param line
     *            The output line to verify
     * @param cmd
     *            The command executed
     * @return <code>true</code> if it's an echoed command line else
     *         <code>false</code>
     */
    @SuppressWarnings("nls")
    private static boolean isCommandEcho(String line, String cmd) {
        String s1 = line.replaceAll("\\s","");
        String s2 = cmd.replaceAll("\\s","");
        s2 = s2.replaceAll("(\\*)", "(\\\\*)");
        String patternStr = ".*(" + s2 +")$";
        return s1.matches(patternStr);
    }
}
