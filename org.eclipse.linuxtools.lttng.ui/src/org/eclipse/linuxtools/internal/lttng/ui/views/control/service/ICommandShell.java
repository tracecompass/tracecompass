/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.views.control.service;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * <b><u>ICommandShell</u></b>
 * <p>
 * Interface for a command shell implementation
 * </p>
 */
public interface ICommandShell {
    
    /**
     * Method to connect the command shell.
     * @throws ExecutionException
     */
    public void connect() throws ExecutionException;
    
    /**
     * Method to disconnect the command shell.
     */
    public void disconnect();
    
    /**
     * Method to execute a command on the command shell.
     * 
     * @param command - the command to executed
     * @param monitor - a progress monitor
     * @return the command result
     * @throws ExecutionException
     */
    public ICommandResult executeCommand(String command, IProgressMonitor monitor) throws ExecutionException;

    /**
     * Method to execute a command on the command shell.
     * 
     * @param command - the command to executed
     * @param monitor - a progress monitor
     * @param checkReturnValue - flag to indicate that the command result should be checked. If false the command result will be always 0.
     * @return the command result
     * @throws ExecutionException
     */
    public ICommandResult executeCommand(final String command, final IProgressMonitor monitor, final boolean checkReturnValue) throws ExecutionException;

}
