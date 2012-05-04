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
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.remote;

import java.util.Arrays;

/**
 * <b><u>CommandResult</u></b>
 * <p>
 * Class containing command result of remote command execution.
 * </p>
 */
public class CommandResult implements ICommandResult {
    
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The result of the command. 0 if successful else > 0
     */
    private int fResult;

    /**
     * The output as String array.
     */
    private String[] fOutput = new String[0];

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    public CommandResult(int result, String[] output) {
        fResult = result;
        if (output != null) {
            fOutput = Arrays.copyOf(output, output.length);
        }
    }

    // ------------------------------------------------------------------------
    // Accessor
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ICommandResult#getResult()
     */
    @Override
    public int getResult() {
        return fResult;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ICommandResult#setResult(int)
     */
    @Override
    public void setResult(int result) {
        fResult = result;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ICommandResult#getOutput()
     */
    @Override
    public String[] getOutput() {
        return Arrays.copyOf(fOutput, fOutput.length);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.ICommandResult#setOutput(java.lang.String[])
     */
    @Override
    public void setOutput(String[] output) {
        fOutput = new String[0];
        if (output != null) {
            fOutput = Arrays.copyOf(output, output.length);
        }
    }
}