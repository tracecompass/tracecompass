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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.remote.core.shell.CommandInput;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandInput;
import org.junit.Test;

/**
 * Test suite for the {@link CommandInput} class
 */
public class CommandInputTest {

    private static final @NonNull String COMMAND = "my-command";
    private static final @NonNull String @NonNull [] CMD_INPUT = { "This", "are", "the", "params" };

    /**
     * Test suite for the {@link CommandInput#add(String)} and {@link CommandInput#addAll(List)}
     */
    @Test
    public void testConstructorAndAdd() {
        ICommandInput iunput = new CommandInput();
        iunput.add(COMMAND);
        @NonNull List<@NonNull String> params = Arrays.asList(CMD_INPUT);
        iunput.addAll(params);

        List<String> expectedList = new ArrayList<>();
        expectedList.add(COMMAND);
        expectedList.addAll(params);
        assertEquals(expectedList, iunput.getInput());
        String expected = expectedInputString();
        assertEquals(expected, iunput.toString());
    }

    /**
     * Test suite to test null segment for {@link CommandInput#add(String)}
     */
    @Test
    public void testNullSegment() {
        ICommandInput input = new CommandInput();
        input.add(null);
        assertEquals(0, input.getInput().size());
    }

    private static String expectedInputString() {
        StringBuilder builder = new StringBuilder();
        builder.append(COMMAND).append((' '));
        for (String segment : CMD_INPUT) {
            builder.append(segment).append(' ');
        }
        return builder.toString().trim();
    }

}