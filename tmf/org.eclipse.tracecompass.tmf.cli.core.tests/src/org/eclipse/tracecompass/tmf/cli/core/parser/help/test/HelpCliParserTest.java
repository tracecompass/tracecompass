/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.cli.core.parser.help.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.cli.core.parser.CliCommandLine;
import org.eclipse.tracecompass.internal.provisional.tmf.cli.core.parser.CliParserException;
import org.eclipse.tracecompass.internal.provisional.tmf.cli.core.parser.CliParserManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test the {@link CliParserManager} class with the options provided by this
 * plugin. This class tests the parsing of the arguments, and executes the early
 * options, but it does not test the behavior of the individual arguments.
 *
 * @author Geneviève Bastien
 */
@RunWith(Parameterized.class)
public class HelpCliParserTest {

    /**
     * String to use in text files to be replaced by the complete help text
     */
    public static final String HELP_PLACEHOLDER = "%HELP%";
    private static final String TESTFILES = "testfiles/";
    private static final String HELP_FILE = "helpText.txt";

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
                // Errors
                { "--cli -faulty", true, "wrongOption.txt" },
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
    public HelpCliParserTest(String cmdLine, boolean exception, @Nullable String fileText) {
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

        CliParserException exception = null;
        try {
            // Parse the command line arguments
            String[] args = fCmdLine.split(" ");
            CliCommandLine parsedCli = CliParserManager.getInstance().parse(args);
            assertNotNull(parsedCli);
            CliParserManager.applicationStartup(parsedCli);
        } catch (CliParserException e) {
            exception = e;
        }

        // Verify if there was an exception
        assertEquals("Exception " + exception, fException, exception != null);

        assertEquals(outputString, outContent.toString());

    }

    private String replaceHelp(String outputString) throws IOException {
        if (outputString.contains(HELP_PLACEHOLDER)) {
            String helpString = getHelpText();
            return outputString.replace(HELP_PLACEHOLDER, helpString);
        }
        return outputString;
    }

    /**
     * Get the help text for this test case. The help text will replace the
     * {@link #HELP_PLACEHOLDER} text in test files.
     *
     * @return The complete help text string
     * @throws IOException
     *             Exceptions thrown by reading files
     */
    protected String getHelpText() throws IOException {
        byte[] helpBytes = Files.readAllBytes(Paths.get(TESTFILES + HELP_FILE));
        return new String(helpBytes);
    }

}
