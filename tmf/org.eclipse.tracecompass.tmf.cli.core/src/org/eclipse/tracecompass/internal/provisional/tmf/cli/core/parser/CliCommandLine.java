/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.cli.core.parser;

import java.util.Objects;

import org.apache.commons.cli.CommandLine;

/**
 * Class that contains the results of parsing the command line arguments.
 *
 * @author Geneviève Bastien
 */
public class CliCommandLine {

    private final CommandLine fCommandLine;

    /**
     * Constructor
     *
     * @param cmdLine
     *            The CLI command line wrapped by this class
     */
    CliCommandLine(CommandLine cmdLine) {
        fCommandLine = cmdLine;
    }

    /**
     * Return whether this command line has an option
     *
     * @param option
     *            The option name
     * @return <code>true</code> if this command line has the given option
     */
    public boolean hasOption(String option) {
        return fCommandLine.hasOption(option);
    }

    /**
     * Return the values for the option in parameter. The option must be
     * present, you can use the {@link #hasOption(String)} method to verify the
     * option presence. If the option does not exist, it will throw a
     * {@link NullPointerException}.
     *
     * @param option
     *            The name of the option to get, it should match the short
     *            option tag specified in the creation of the {@link CliOption}
     *            class.
     * @return The list of option values
     */
    public String[] getOptionValues(String option) {
        return Objects.requireNonNull(fCommandLine.getOptionValues(option), "Command line option was not specified: " + option); //$NON-NLS-1$
    }

    @Override
    public String toString() {
        return String.valueOf(fCommandLine);
    }

}
