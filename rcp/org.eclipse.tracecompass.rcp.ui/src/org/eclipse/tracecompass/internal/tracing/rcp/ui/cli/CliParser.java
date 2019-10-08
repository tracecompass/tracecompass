/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam- Initial API and implementation
 **********************************************************************/

package org.eclipse.tracecompass.internal.tracing.rcp.ui.cli;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tracing.rcp.ui.TracingRcpPlugin;
import org.eclipse.tracecompass.internal.tracing.rcp.ui.messages.Messages;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;

/**
 * Command line parser
 *
 * @author Matthew Khouzam
 * @author Bernd Hufmann
 */
public class CliParser {

    /**
     * The CLI Parser instance
     */
    private static @Nullable CliParser INSTANCE = null;

    private static final String CLI_ARGS_PREFIX = "--cli"; //$NON-NLS-1$

    // Help
    private static final String OPTION_HELP_SHORT = "h"; //$NON-NLS-1$
    private static final String OPTION_HELP_LONG = "help"; //$NON-NLS-1$
    private static final String OPTION_HELP_DESCRIPTION = Messages.CliParser_HelpDescription;

    private static final String OPTION_COMMAND_LINE_OPEN_SHORT = "o"; //$NON-NLS-1$
    private static final String OPTION_COMMAND_LINE_OPEN_LONG = "open"; //$NON-NLS-1$
    private static final String OPTION_COMMAND_LINE_OPEN_DESCRIPTION = Messages.CliParser_OpenTraceDescription;

    private static final String OPTION_COMMAND_LINE_LIST_SHORT = "l"; //$NON-NLS-1$
    private static final String OPTION_COMMAND_LINE_LIST_LONG = "list"; //$NON-NLS-1$
    private static final String OPTION_COMMAND_LINE_LIST_DESCRIPTION = Messages.CliParser_ListCapabilitiesDescription;

    private final Options fOptions;

    private CommandLine fLineParser;

    /**
     * Constructor
     *
     * @param args
     *            the command line arguments
     * @throws TracingRCPCliException
     *             an error occurred parsing the cli
     */
    private CliParser() {
        fOptions = new Options();
        fOptions.addOption(OPTION_HELP_SHORT, OPTION_HELP_LONG, false, OPTION_HELP_DESCRIPTION);
        fOptions.addOption(OPTION_COMMAND_LINE_LIST_SHORT, OPTION_COMMAND_LINE_LIST_LONG, false, OPTION_COMMAND_LINE_LIST_DESCRIPTION);

        OptionBuilder.withArgName("path"); //$NON-NLS-1$
        OptionBuilder.hasArg();
        OptionBuilder.withLongOpt(OPTION_COMMAND_LINE_OPEN_LONG);
        OptionBuilder.withDescription(OPTION_COMMAND_LINE_OPEN_DESCRIPTION);
        Option openOpt = OptionBuilder.create(OPTION_COMMAND_LINE_OPEN_SHORT);

        fOptions.addOption(openOpt);
    }

    /**
     * Get the instance of this class
     *
     * @return The instance
     */
    public static synchronized CliParser getInstance() {
        CliParser instance = INSTANCE;
        if (instance == null) {
            instance = new CliParser();
            INSTANCE = instance;
        }
        return instance;
    }

    /**
     * Parse the command line arguments
     *
     * @param args
     *            The arguments of the application
     * @throws ParseException
     *             If there were exceptions parsing the arguments
     */
    public void parse(String[] args) throws ParseException {
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
        CommandLineParser cmdLineParser = new PosixParser();
        try {
            fLineParser = cmdLineParser.parse(fOptions, tcArgs.toArray(new String[0]), false);
        } catch(ParseException e) {
            // Print to screen the error and help text and re-throw the exception
            System.out.println(Messages.CliParser_ErrorParsingArguments);
            System.out.println(e);
            System.out.println();
            printHelpText();
            throw e;
        }
    }

    /**
     * Handle early command line option. If any option is handled at this point,
     * the method will return true and the application will exit.
     *
     * @return <code>true</code> if an option was handled at this stage, or
     *         <code>false</code> otherwise, indicating to continue loading the
     *         application.
     */
    public boolean handleEarlyOption() {
        if (fLineParser.hasOption(OPTION_HELP_SHORT)) {
            printHelpText();
            return true;
        }
        if (fLineParser.hasOption(OPTION_COMMAND_LINE_LIST_SHORT)) {
            System.out.println(Messages.CliParser_ListSupportedTraceTypes);
            System.out.println();
            for (TraceTypeHelper helper : TmfTraceType.getTraceTypeHelpers()) {
                System.out.println(helper.getName() + ": " + helper.getTraceTypeId()); //$NON-NLS-1$
                System.out.println();
            }
            return true;
        }
        return false;
    }

    /**
     * Print the help text for the command line
     */
    public void printHelpText() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(Messages.CliParser_HelpTextIntro, fOptions);
    }

    /**
     * Handle the command line options. The Trace Compass application will load
     * as usual and those options will act upon it.
     */
    public void handleLateOptions() {
        if (fLineParser.hasOption(OPTION_COMMAND_LINE_OPEN_SHORT)) {
            IProject defaultProject = createDefaultProject();
            openTraceIfNecessary(defaultProject, fLineParser.getOptionValue(OPTION_COMMAND_LINE_OPEN_SHORT));
        }

    }

    private static void openTraceIfNecessary(IProject project, String trace) {
        String traceToOpen = trace;
        String userHome = System.getProperty("user.home"); //$NON-NLS-1$
        // In case the application was not started on the shell, expand ~ to home directory
        if ((traceToOpen != null) && traceToOpen.startsWith("~/") && (userHome != null)) { //$NON-NLS-1$
            traceToOpen = traceToOpen.replaceFirst("^~", userHome); //$NON-NLS-1$
        }

        if (traceToOpen != null) {
            try {
                TmfTraceFolder destinationFolder = TmfProjectRegistry.getProject(project, true).getTracesFolder();
                TmfOpenTraceHelper.openTraceFromPath(destinationFolder, traceToOpen, TracingRcpPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell());
            } catch (CoreException e) {
                TracingRcpPlugin.getDefault().logError(e.getMessage());
            }
        }
    }

    private static IProject createDefaultProject() {
        return TmfProjectRegistry.createProject(TmfCommonConstants.DEFAULT_TRACE_PROJECT_NAME, null, new NullProgressMonitor());
    }

}