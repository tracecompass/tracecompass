/*******************************************************************************
 * Copyright (c) 2019 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.cli.core.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.cli.core.Activator;
import org.eclipse.tracecompass.internal.tmf.cli.core.parser.CliParserConfigElement;
import org.eclipse.tracecompass.internal.tmf.cli.core.parser.Messages;

/**
 * Utility class to manage cli actions
 *
 * @author Matthew Khouzam
 * @author Bernd Hufmann
 * @author Geneviève Bastien
 */
public final class CliParserManager {

    private static final String CLI_ARGS_PREFIX = "--cli"; //$NON-NLS-1$

    /**
     * The CLI Parser instance
     */
    private static @Nullable CliParserManager INSTANCE = null;

    private final Options fOptions;

    /**
     * Constructor, not to be used
     */
    private CliParserManager() {
        Collection<@NonNull ICliParser> cliParsers = getCliParsers();
        Options options = new Options();
        for (ICliParser parser : cliParsers) {
            for (CliOption option : parser.getCmdLineOptions()) {
                options.addOption(option.toCliOption());
            }
        }
        fOptions = options;
    }

    /**
     * Get the instance of this class
     *
     * @return The instance
     */
    public static synchronized CliParserManager getInstance() {
        CliParserManager instance = INSTANCE;
        if (instance == null) {
            instance = new CliParserManager();
            INSTANCE = instance;
        }
        return instance;
    }

    /**
     * Get the CLI actions available
     *
     * @return A collection of CLI actions
     */
    public static Collection<ICliParser> getCliParsers() {
        return CliParserConfigElement.getInstance().getParsers();
    }

    /**
     * Parse the command line arguments and return the result of the parsing
     *
     * @param args
     *            The arguments of the application
     * @return The parsed command line, or <code>null</code> if there were no
     *         options given
     * @throws CliParserException
     *             If there were exceptions parsing the arguments
     */
    public @Nullable CliCommandLine parse(String[] args) throws CliParserException {
        List<String> tcArgs = new ArrayList<>();
        boolean found = false;
        boolean addNext = false;
        for (String arg : args) {
            if (arg.equals(CLI_ARGS_PREFIX)) {
                found = true;
                continue;
            }
            if (found) {
                tcArgs.add(arg);
            } else if (arg.equals("--open")) { //$NON-NLS-1$
                // Legacy parsing of the --open option, without preceding --cli
                System.out.println(Messages.CliParser_WarningCliPrefix);
                tcArgs.add(arg);
                addNext = true;
            } else if (addNext) {
                tcArgs.add(arg);
                addNext = false;
            }
        }
        if (tcArgs.isEmpty()) {
            return null;
        }
        CommandLineParser cmdLineParser = new PosixParser();
        try {
            // There may not be one and only one command line, do not keep the
            // command line as internal field of this class
            CommandLine cmdLine = cmdLineParser.parse(fOptions, tcArgs.toArray(new String[tcArgs.size()]), false);
            if (cmdLine != null) {
                CliCommandLine cliCommandLine = new CliCommandLine(cmdLine);
                return cliCommandLine;
            }
        } catch (ParseException e) {
            // Print to screen the error and help text and re-throw the
            // exception
            System.out.println(Messages.CliParser_ErrorParsingArguments);
            System.out.println(e);
            System.out.println();
            printHelpText();
            throw new CliParserException(String.valueOf(e.getMessage()));
        }
        return null;
    }

    /**
     * Print the help text for the command line
     */
    public void printHelpText() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(Messages.CliParser_HelpTextIntro, fOptions);
    }

    /**
     * Handle early command line option, at a stage where the application is not
     * fully loaded yet, but loading. This is the ideal place to handle help
     * options or other options that should give instant feedback to the user,
     * without having to fully open the interface.
     *
     * The return value of this method indicate whether to shutdown the
     * application after executing the option or continue loading.
     *
     * @param commandLine
     *            The command line that was previously parsed using the
     *            {@link #parse(String[])} method
     *
     * @return <code>true</code> if an option was handled, or <code>false</code>
     *         otherwise. At this stage, a return value of <code>true</code>
     *         will cause the application not to continue loading and do a quick
     *         return.
     */
    public static boolean applicationStartup(CliCommandLine commandLine) {
        boolean optionsHandled = false;
        for (ICliParser parser : getCliParsers()) {
            optionsHandled |= parser.preStartup(commandLine);
        }
        return optionsHandled;
    }

    private static class ExecuteCliParsersJob extends Job {

        private final CliCommandLine fCommandLine;

        /**
         * Constructor
         *
         * @param commandLine
         *            the trace element
         */
        public ExecuteCliParsersJob(CliCommandLine commandLine) {
            super("Executing CLI parser"); //$NON-NLS-1$
            fCommandLine = commandLine;
        }

        @Override
        protected IStatus run(@Nullable IProgressMonitor monitor) {
            SubMonitor subMonitor = SubMonitor.convert(monitor);
            Collection<ICliParser> cliParsers = getCliParsers();
            subMonitor.beginTask("Executing CLI Parsers", cliParsers.size()); //$NON-NLS-1$
            for (ICliParser parser : cliParsers) {
                if (subMonitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                subMonitor.subTask("Executing parser :" + parser.getCmdLineOptions().toString()); //$NON-NLS-1$
                IStatus status = parser.workspaceLoading(fCommandLine, subMonitor);
                if (!status.isOK()) {
                    Activator.getInstance().getLog().log(status);
                }
            }
            return Status.OK_STATUS;
        }
    }

    /**
     * Get a job that will handle the command line options. It is the caller's
     * responsibility to schedule the job and do any appropriate actions for
     * proper termination and cancellation from the user.
     *
     * This method will create a Job in which each CLI parser, ordered by
     * priority, will execute its action sequentially. When the job is done, all
     * CLI parsers will have been executed
     *
     * @param commandLine
     *            The command line that was previously parsed using the
     *            {@link #parse(String[])} method
     * @return The workspace loading job, so it can be cancelled by caller
     */
    public static Job getWorkspaceLoadingJob(CliCommandLine commandLine) {
        return new ExecuteCliParsersJob(commandLine);
    }

}
