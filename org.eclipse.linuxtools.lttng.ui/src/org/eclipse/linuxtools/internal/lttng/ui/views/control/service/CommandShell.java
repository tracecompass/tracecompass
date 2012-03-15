/**********************************************************************
 * Copyright (c) 2012 Ericsson
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
package org.eclipse.linuxtools.internal.lttng.ui.views.control.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
import org.eclipse.linuxtools.internal.lttng.ui.views.control.Messages;
import org.eclipse.linuxtools.internal.lttng.ui.views.control.remote.IRemoteSystemProxy;
import org.eclipse.rse.services.shells.HostShellProcessAdapter;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;

/**
 * <b><u>CommandShell</u></b>
 * <p>
 * Implementation of remote command execution using RSE's shell service. 
 * </p>
 */
public class CommandShell implements ICommandShell {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    // string to be echo'ed when running command in shell, used to indicate that the command has finished running
    public final static String DONE_MARKUP_STRING = "--RSE:donedonedone:--"; //$NON-NLS-1$
    
    //command delimiter for shell
    public final static String CMD_DELIMITER = "\n"; //$NON-NLS-1$

    public final static String SHELL_ECHO_CMD = " echo "; //$NON-NLS-1$

    private final static int DEFAULT_TIMEOUT_VALUE = 15000; // in milliseconds

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private IRemoteSystemProxy fProxy = null;
    private IHostShell fHostShell = null;
    private BufferedReader fBufferReader = null;
    private ExecutorService fExecutor = Executors.newFixedThreadPool(1);
    private boolean fIsConnected = false;
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    public CommandShell(IRemoteSystemProxy proxy) {
        fProxy = proxy;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng.ui.views.control.service.ICommandShell#connect()
     */
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
        fBufferReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        fIsConnected = true;

        // Flush Login messages
        executeCommand(" ", new NullProgressMonitor(), false); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng.ui.views.control.service.ICommandShell#disconnect()
     */
    @Override
    public void disconnect() {
        fIsConnected = false;
        try {
            fBufferReader.close();
        } catch (IOException e) {
            // ignore
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng.ui.views.control.service.ICommandShell#executeCommand(java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public ICommandResult executeCommand(String command, IProgressMonitor monitor) throws ExecutionException {
        return executeCommand(command, monitor, true);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng.ui.views.control.service.ICommandShell#executeCommand(java.lang.String, org.eclipse.core.runtime.IProgressMonitor, boolean)
     */
    @Override
    public ICommandResult executeCommand(final String command, final IProgressMonitor monitor, final boolean checkReturnValue) throws ExecutionException {
        if (fIsConnected) {
            FutureTask<CommandResult> future = new FutureTask<CommandResult>(new Callable<CommandResult>() {
                @Override
                public CommandResult call() throws IOException, CancellationException {
                    final ArrayList<String> result = new ArrayList<String>();
                    int returnValue = 0;

                    synchronized (fHostShell) {
                        fHostShell.writeToShell(formatShellCommand(command));
                        String nextLine;
                        while ((nextLine = fBufferReader.readLine()) != null) {

                            if (monitor.isCanceled()) {
                                flushInput();
                                throw new CancellationException(); 
                            }

                            if (nextLine.contains(DONE_MARKUP_STRING) && nextLine.contains(SHELL_ECHO_CMD)) {
                                break;
                            }
                        }

                        while ((nextLine = fBufferReader.readLine()) != null) {
                            // check if job was cancelled
                            if (monitor.isCanceled()) {
                                flushInput();
                                throw new CancellationException(); 
                            }

                            if (!nextLine.contains(DONE_MARKUP_STRING)) {
                                result.add(nextLine);
                            } else {
                                if (checkReturnValue) {
                                    returnValue = Integer.valueOf(nextLine.substring(DONE_MARKUP_STRING.length()+1));
                                }
                                break;
                            }
                        }

                        flushInput();
                    }
                    return new CommandResult(returnValue, result.toArray(new String[result.size()]));
                }
            });

            fExecutor.execute(future);

            try {
                return future.get(DEFAULT_TIMEOUT_VALUE, TimeUnit.MILLISECONDS);
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
        while (fBufferReader.ready()) {
            if (fBufferReader.read(cbuf, 0, 1) == -1) {
                break;
            }
        }
    }
    
    /**
     * format the command to be sent into the shell command with the done markup string.
     * The done markup string is needed so we can tell that end of output has been reached.
     * 
     * @param cmd
     * @return formatted command string
     */
    private String formatShellCommand(String cmd) {
        if (cmd == null || cmd.equals("")) //$NON-NLS-1$
            return cmd;
        StringBuffer formattedCommand = new StringBuffer();
        // Make a multi line command by using \ and \r. This is needed for matching
        // the DONE_MARKUP_STRING in echoed command when having a long command 
        // (bigger than max SSH line)
        formattedCommand.append(cmd).append("\\\r;"); //$NON-NLS-1$ 
        formattedCommand.append(SHELL_ECHO_CMD).append(DONE_MARKUP_STRING);
        formattedCommand.append(" $?"); //$NON-NLS-1$
        formattedCommand.append(CMD_DELIMITER);
        return formattedCommand.toString();
    }

}
