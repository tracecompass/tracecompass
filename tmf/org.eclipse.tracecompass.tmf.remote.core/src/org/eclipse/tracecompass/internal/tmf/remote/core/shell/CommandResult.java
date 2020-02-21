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
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.tmf.remote.core.shell;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandResult;

import com.google.common.collect.ImmutableList;

/**
 * Class containing command result of remote command execution.
 *
 * @author Bernd Hufmann
 */
@NonNullByDefault
public class CommandResult implements ICommandResult {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /** The result of the command. 0 if successful else > 0 */
    private final int fResult;

    /** The output as list of Strings. */
    private final List<String> fOutput;

    /** The error stream output as list of Strings. */
    private final List<String> fErrorOutput;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param result
     *            The result of the command
     * @param output
     *            The output, as an array of strings
     * @param errorOutput
     *            THe error output as an array of strings
     */
    public CommandResult(int result, @NonNull String[] output, @NonNull String[] errorOutput) {
        fResult = result;
        fOutput = ImmutableList.copyOf(output);
        fErrorOutput = ImmutableList.copyOf(errorOutput);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public int getResult() {
        return fResult;
    }

    @Override
    public List<String> getOutput() {
        return fOutput;
    }

    @Override
    public List<String> getErrorOutput() {
        return fErrorOutput;
    }

    @Override
    public String toString() {
        StringBuffer ret = new StringBuffer();
        ret.append("Error Output:\n"); //$NON-NLS-1$
        for (String string : fErrorOutput) {
            ret.append(string).append("\n"); //$NON-NLS-1$
        }
        ret.append("Return Value: "); //$NON-NLS-1$
        ret.append(fResult);
        ret.append("\n"); //$NON-NLS-1$
        for (String string : fOutput) {
            ret.append(string).append("\n"); //$NON-NLS-1$
        }
        return nullToEmptyString(ret.toString());
    }
}
