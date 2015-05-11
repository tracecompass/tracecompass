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

import java.util.List;


/**
 * Interface for providing command execution result.
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
     * Return the command output.
     *
     * It should not be null, but could be empty.
     * It should return an immutable list.
     *
     * @return the command output.
     */
    List<String> getOutput();

    /**
     * Return the command error output.
     *
     * It should not be null, but could be empty.
     * It should return an immutable list.
     *
     * @return the command error output.
     */
    List<String> getErrorOutput();
}