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

package org.eclipse.tracecompass.internal.provisional.tmf.cli.core.parser;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Base class to describe cli parsers.
 *
 * @author Geneviève Bastien
 */
public interface ICliParser {

    /**
     * Get the command line options provided by this cli parser class
     *
     * @return The list of options provided by this cli parser
     */
    List<CliOption> getCmdLineOptions();

    /**
     * Handle command line option before setting up the workspace, at a stage
     * where the application is not fully loaded yet, but loading. This is the
     * ideal place to handle help options or other options that should give
     * instant feedback to the user, without having to fully open the interface.
     *
     * The return value of this method indicate whether to continue the
     * application after executing the option or continue loading.
     *
     * Implementations must make sure an option is present on the command line
     * before executing actions, by calling the
     * {@link CliCommandLine#hasOption(String)} method first, then getting
     * possible arguments.
     *
     * @param commandLine
     *            The parsed command line options
     *
     * @return <code>true</code> if an option was handled and the application
     *         should stop loading, or <code>false</code> otherwise, indicating
     *         to continue loading the application.
     */
    default boolean preStartup(CliCommandLine commandLine) {
        // Let implementations implement this
        return false;
    }

    /**
     * Handle the command line options. This method is called from a thread
     * separate from the UI that starts executing the CLI parsers after the
     * startup. Typically the workspace is loaded and ready to receive actions.
     *
     * Implementations must make sure an option is present on the command line
     * before executing actions, by calling the
     * {@link CliCommandLine#hasOption(String)} method first, then getting
     * possible arguments.
     *
     * Parsers should execute the appropriate actions according to the options
     * and should not return until the actions is completed, even if it means
     * waiting for other threads, as some CLI parsers of lower priority may
     * require their actions to be completed.
     *
     * For example, a parser that opens a trace should wait for the trace to be
     * fully opened before returning and not simply send a request to open a
     * trace and return right away.
     *
     * @param commandLine
     *            The parsed command line options
     * @param monitor
     *            The progress monitor, for the implementations to update their
     *            progress and stop processing if necessary
     * @return The status of running the actions for this parser. By default,
     *         one can return {@link Status#OK_STATUS}.
     */
    default IStatus workspaceLoading(CliCommandLine commandLine, IProgressMonitor monitor) {
        // Let implementations implement this
        return Status.OK_STATUS;
    }

}
