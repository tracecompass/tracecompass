/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.cli.core.parser.help;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.provisional.tmf.cli.core.parser.CliCommandLine;
import org.eclipse.tracecompass.internal.provisional.tmf.cli.core.parser.CliOption;
import org.eclipse.tracecompass.internal.provisional.tmf.cli.core.parser.CliParserManager;
import org.eclipse.tracecompass.internal.provisional.tmf.cli.core.parser.ICliParser;
import org.eclipse.tracecompass.internal.tmf.cli.core.parser.Messages;

/**
 * Default CLI parser for printing the help
 *
 * @author Geneviève Bastien
 */
public class HelpCliParser implements ICliParser {

 // Help
    private static final String OPTION_HELP_SHORT = "h"; //$NON-NLS-1$
    private static final String OPTION_HELP_LONG = "help"; //$NON-NLS-1$
    private static final String OPTION_HELP_DESCRIPTION = Objects.requireNonNull(Messages.CliParser_HelpDescription);

    private final List<@NonNull CliOption> fOptions;

    /**
     * Constructor
     */
    public HelpCliParser() {
        fOptions = new ArrayList<>();
        fOptions.add(CliOption.createSimpleOption(OPTION_HELP_SHORT, OPTION_HELP_LONG, OPTION_HELP_DESCRIPTION));
    }

    @Override
    public boolean preStartup(CliCommandLine commandLine) {
        if (commandLine.hasOption(OPTION_HELP_SHORT)) {
            CliParserManager.getInstance().printHelpText();
            return true;
        }
        return false;
    }

    @Override
    public @NonNull List<@NonNull CliOption> getCmdLineOptions() {
        return fOptions;
    }
}
