/**********************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *********************************************************************/
package org.eclipse.tracecompass.tmf.remote.core.shell;

/**
 * <p>
 * Interface for providing command execution result.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface ICommandResult {
    /**
     * The result of the command.
     *
     * @return 0 if successful else >0
     */
    int getResult();

    /**
     * @return returns the command output.
     */
    String[] getOutput();

    /**
     * The error output of the command.
     *
     * @return returns the command error output.
     */
    String[] getErrorOutput();
}