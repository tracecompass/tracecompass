/**********************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.tmf.remote.core.shell;

/**
 * Interface for a providing a command output listener
 *
 * @author Bernd Hufmann
 * @since 2.0
 */
public interface ICommandOutputListener {

    /**
     * Call back with new output String. It will provide the delta String
     * in comparison to the previous call of this method.
     *
     * The implementer of this method must not block the current thread.
     *
     * @param updatedString
     *            The new outputString
     */
    void outputUpdated(String updatedString);

    /**
     * Call back with new error output String. It will provide the delta String
     * in comparison to the previous call of this method.
     *
     * The implementer of this method must not block the current thread.
     *
     * @param updatedString
     *            The new outputString
     */
    void errorOutputUpdated(String updatedString);
}
