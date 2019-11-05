/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.rcp.ui.tests.cliparser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.cli.core.parser.CliParserManager;
import org.eclipse.tracecompass.tmf.cli.core.parser.help.test.HelpCliParserTest;
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
public class CliParserTest extends HelpCliParserTest {

    private static final String TESTFILES = "testfiles/";
    private static final String HELP_FILE = "helpText.txt";

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
        super(cmdLine, exception, fileText);
    }

    @Override
    protected String getHelpText() throws IOException {
        byte[] helpBytes = Files.readAllBytes(Paths.get(TESTFILES + HELP_FILE));
        return new String(helpBytes);
    }

}
