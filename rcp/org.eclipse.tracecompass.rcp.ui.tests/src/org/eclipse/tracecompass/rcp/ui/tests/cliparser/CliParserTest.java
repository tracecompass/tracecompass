/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.rcp.ui.tests.cliparser;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.cli.ParseException;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tracing.rcp.ui.cli.CliParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test the {@link CliParser} class. This class tests the parsing of the
 * arguments, and executes the early options, but it does not test the behavior
 * of the individual arguments.
 *
 * @author Geneviève Bastien
 */
@RunWith(Parameterized.class)
public class CliParserTest {

    private static final String TESTFILES = "testfiles/";
    private static final String HELP_FILE = "helpText.txt";
    private static final String HELP_PLACEHOLDER = "%HELP%";

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    /**
     * Stream the standard output to our buffer
     */
    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    /**
     * Restore the standard output to its original value
     */
    @After
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                // Help arguments
                { "--cli --help", false, HELP_FILE },
                { "--cli -h", false, HELP_FILE },
                // List capabilities arguments
                { "--cli --list", false, "listCapabilities.txt" },
                { "--cli -l", false, "listCapabilities.txt" },
                // Open trace
                { "--cli --open mytrace", false, null },
                { "--cli -o mytrace", false, null },
                { "--cli --open mytrace mytrace2 mytrace3", false, null },
                { "--cli -o mytrace mytrace2 mytrace3", false, null },
                { "--cli --open", true, "missingOpenArgument.txt" },
                { "--cli -o", true, "missingOpenArgument.txt" },
                // Errors
                { "--cli -faulty", true, "wrongOption.txt" },
                // Legacy open arguments
                { "--open mytrace", false, "warningCli.txt" },
                { "--open", true, "warningCliError.txt" },
                { "--open mytrace --list", false, "warningCli.txt" },
        });
    }

    private final String fCmdLine;
    private final boolean fException;
    private final @Nullable String fTextFile;

    /**
     * Constructor
     *
     * @param cmdLine
     *            The command line arguments to parse
     * @param exception
     *            Whether these arguments should throw an exception
     * @param fileText
     *            The file containing the expected output. Set to
     *            <code>null</code> if there is no output.
     */
    public CliParserTest(String cmdLine, boolean exception, @Nullable String fileText) {
        fCmdLine = cmdLine;
        fException = exception;
        fTextFile = fileText;
    }

    /**
     * Test the command line arguments
     *
     * @throws IOException
     *             Exception thrown during the test
     */
    @Test
    public void testCmdLineArguments() throws IOException {
        byte[] outputBytes = (fTextFile != null) ? Files.readAllBytes(Paths.get(TESTFILES + fTextFile)) : new byte[0];
        String outputString = replaceHelp(new String(outputBytes));

        ParseException exception = null;
        try {
            // Parse the command line arguments
            String[] args = fCmdLine.split(" ");
            CliParser.getInstance().parse(args);
            CliParser.getInstance().handleEarlyOption();
        } catch (ParseException e) {
            exception = e;
        }

        // Verify if there was an exception
        assertEquals(fException, exception != null);

        assertEquals(outputString, outContent.toString());

    }

    private static String replaceHelp(String outputString) throws IOException {
        if (outputString.contains(HELP_PLACEHOLDER)) {
            byte[] helpBytes = Files.readAllBytes(Paths.get(TESTFILES + HELP_FILE));
            String helpString = new String(helpBytes);
            return outputString.replace(HELP_PLACEHOLDER, helpString);
        }
        return outputString;
    }

}
