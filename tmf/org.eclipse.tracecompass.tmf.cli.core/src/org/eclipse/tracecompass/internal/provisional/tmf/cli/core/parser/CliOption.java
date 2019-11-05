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

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Describe a command line argument for a CLI parser.
 *
 * TODO Add more types of options to create
 *
 * @author Geneviève Bastien
 */
public class CliOption {

    private static final String DEFAULT_ARG_NAME = "arg"; //$NON-NLS-1$

    private final String fShortOption;
    private final @Nullable String fLongOption;
    private final @Nullable String fDescription;
    private final int fMinArguments;
    private final int fMaxArguments;
    private final String fArgumentName;

    /**
     * Create a new simple options, without arguments.
     *
     * @param optionShort
     *            The short key for this options, can be null
     * @param optionLong
     *            The long key for this option
     * @param optionDescription
     *            The options description
     * @return A new option
     */
    public static CliOption createSimpleOption(String optionShort, @Nullable String optionLong, @Nullable String optionDescription) {
        return new CliOption(optionShort, optionLong, optionDescription, 0, 0, DEFAULT_ARG_NAME);
    }

    private CliOption(String optionShort, @Nullable String optionLong, @Nullable String optionDescription, int min, int max, String argName) {
        fShortOption = optionShort;
        fLongOption = optionLong;
        fDescription = optionDescription;
        fMinArguments = Math.min(min, max);
        fMaxArguments = Math.max(min, max);
        fArgumentName = argName;
    }

    Option toCliOption() {
        OptionBuilder.withArgName(fArgumentName);
        // Number of arguments
        if (fMaxArguments == 0) {
            // No arguments
            OptionBuilder.hasArg(false);
        } else if (fMaxArguments == 1) {
            // 1 argument, optional or mandatory
            if (fMinArguments == 0) {
                OptionBuilder.hasOptionalArg();
            } else {
                OptionBuilder.hasArg();
            }
        } else if (fMaxArguments < Integer.MAX_VALUE) {
            // Many arguments, optional or mandatory
            if (fMinArguments == 0) {
                OptionBuilder.hasOptionalArgs(fMaxArguments);
            } else {
                OptionBuilder.hasArgs(fMaxArguments);
            }
        } else {
            // Unbounded number of optional or mandatory arguments
            if (fMinArguments == 0) {
                OptionBuilder.hasOptionalArgs();
            } else {
                OptionBuilder.hasArgs();
            }
        }
        if (fLongOption != null) {
            OptionBuilder.withLongOpt(fLongOption);
        }
        if (fDescription != null) {
            OptionBuilder.withDescription(fDescription);
        }
        return Objects.requireNonNull(OptionBuilder.create(fShortOption));
    }

    @Override
    public String toString() {
        return '-' + fShortOption;
    }

}
