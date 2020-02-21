/*******************************************************************************
 * Copyright (c) 2015 Ericsson
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
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.remote.core.tests.shell;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.remote.core.shell.CommandResult;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandResult;
import org.junit.Test;

/**
 * Test suite for the {@link CommandResult} class
 */
public class CommandResultTest {

    private static final @NonNull String @NonNull [] CMD_OUTPUT = { "This", "is", "the", "output" };
    private static final @NonNull String @NonNull [] CMD_NO_ERROR_OUTPUT = {};
    private static final @NonNull String @NonNull [] CMD_ERROR_OUTPUT = { "This", "is", "the", "error", "output" };

    /**
     * Test suite for the {@link CommandResult} class
     */
    @Test
    public void testConstructorNoError() {
        ICommandResult result = new CommandResult(0, CMD_OUTPUT, CMD_NO_ERROR_OUTPUT);
        assertEquals(0, result.getResult());
        assertEquals(Arrays.asList(CMD_OUTPUT), result.getOutput());
        assertEquals(Arrays.asList(CMD_NO_ERROR_OUTPUT), result.getErrorOutput());
        String expected = expectedResultString(0, CMD_OUTPUT, CMD_NO_ERROR_OUTPUT);
        assertEquals(expected, result.toString());
    }

    /**
     * Test suite for the {@link CommandResult} method
     */
    @Test
    public void testConstructorError() {
        ICommandResult result = new CommandResult(1, CMD_OUTPUT, CMD_ERROR_OUTPUT);
        assertEquals(1, result.getResult());
        assertEquals(Arrays.asList(CMD_OUTPUT), result.getOutput());
        assertEquals(Arrays.asList(CMD_ERROR_OUTPUT), result.getErrorOutput());
        String expected = expectedResultString(1, CMD_OUTPUT, CMD_ERROR_OUTPUT);
        assertEquals(expected, result.toString());
    }

    private static String expectedResultString(int result, String output[], String[] errorOutput) {
        StringBuffer expected = new StringBuffer();
        expected.append("Error Output:\n");
        for (String string : errorOutput) {
            expected.append(string).append("\n");
        }
        expected.append("Return Value: ").append(result).append("\n");
        for (String string : output) {
            expected.append(string).append("\n");
        }
        return expected.toString();
    }

}