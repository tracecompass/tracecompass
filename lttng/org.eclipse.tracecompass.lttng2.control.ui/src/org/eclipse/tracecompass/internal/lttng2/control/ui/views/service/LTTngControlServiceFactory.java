/**********************************************************************
 * Copyright (c) 2012, 2016 Ericsson
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
 *   Jonathan Rajotte - machine interface support
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.service;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.regex.Matcher;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.logging.ControlCommandLogger;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.preferences.ControlPreferences;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandInput;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandResult;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandShell;

/**
 * Factory to create LTTngControlService instances depending on the version of
 * the LTTng Trace Control installed on the remote host.
 *
 * @author Bernd Hufmann
 */
@NonNullByDefault
public final class LTTngControlServiceFactory {

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Constructor
     */
    private LTTngControlServiceFactory() {
    }

    // ------------------------------------------------------------------------
    // Factory method
    // ------------------------------------------------------------------------
    /**
     * Gets the LTTng Control Service implementation based on the version of the
     * remote LTTng Tools.
     *
     * @param shell
     *            - the shell implementation to pass to the service
     * @return - LTTng Control Service implementation
     * @throws ExecutionException
     *             If the command fails
     */
    public static ILttngControlService getLttngControlService(ICommandShell shell) throws ExecutionException {
        // get the version
        boolean machineInterfaceMode = true;

        // Looking for a machine interface on LTTng side
        ICommandInput command = shell.createCommand();
        command.add(LTTngControlServiceConstants.CONTROL_COMMAND);
        command.add(LTTngControlServiceConstants.CONTROL_COMMAND_MI_OPTION);
        command.add(LTTngControlServiceConstants.CONTROL_COMMAND_MI_XML);
        command.add(LTTngControlServiceConstants.COMMAND_VERSION);
        ICommandResult result = executeCommand(shell, command);

        if (result.getResult() != 0) {
            machineInterfaceMode = false;
            // Fall back if no machine interface is present
            command = shell.createCommand();
            command.add(LTTngControlServiceConstants.CONTROL_COMMAND);
            command.add(LTTngControlServiceConstants.COMMAND_VERSION);
            result = executeCommand(shell, command);
        }

        if ((result.getResult() == 0) && (!result.getOutput().isEmpty())) {
            if (machineInterfaceMode) {
                LTTngControlServiceMI service = new LTTngControlServiceMI(shell, LTTngControlServiceMI.parseVersion(result));
                return service;
            }

            for (String line : result.getOutput()) {
                line = line.replace("-", ".");  //$NON-NLS-1$//$NON-NLS-2$
                Matcher versionMatcher = LTTngControlServiceConstants.VERSION_PATTERN.matcher(line);
                if (versionMatcher.matches()) {
                    String version = versionMatcher.group(1).trim();
                    Matcher matcher = LTTngControlServiceConstants.VERSION_2_PATTERN.matcher(version);
                    if (matcher.matches()) {
                        LTTngControlService service = new LTTngControlService(shell);
                        service.setVersion(checkNotNull(version));
                        return service;
                    }
                    throw new ExecutionException(Messages.TraceControl_UnsupportedVersionError + ": " + version); //$NON-NLS-1$
                }
            }
        }
        throw new ExecutionException(Messages.TraceControl_GettingVersionError);
    }

    private static ICommandResult executeCommand(ICommandShell shell, ICommandInput command) throws ExecutionException {
        // Logging
        if (ControlPreferences.getInstance().isLoggingEnabled()) {
            ControlCommandLogger.log(command.toString());
        }

        ICommandResult result = null;

        try {
            result = shell.executeCommand(command, new NullProgressMonitor());
        } catch (ExecutionException e) {
            throw new ExecutionException(Messages.TraceControl_GettingVersionError + ": " + e); //$NON-NLS-1$
        }

        // Output logging
        if (ControlPreferences.getInstance().isLoggingEnabled()) {
            ControlCommandLogger.log(result.toString());
        }
        return result;
    }
}
