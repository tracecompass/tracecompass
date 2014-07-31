/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Jonathan Rajotte - machine interface support
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.service;

import java.util.regex.Matcher;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.logging.ControlCommandLogger;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.preferences.ControlPreferences;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.remote.ICommandResult;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.remote.ICommandShell;

/**
 * Factory to create LTTngControlService instances depending on the version of
 * the LTTng Trace Control installed on the remote host.
 *
 * @author Bernd Hufmann
 */
public class LTTngControlServiceFactory {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The singleton instance.
     */
    private static LTTngControlServiceFactory fInstance = null;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Constructor
     */
    private LTTngControlServiceFactory() {
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * @return the LTTngControlServiceFactory singleton instance.
     */
    public static synchronized LTTngControlServiceFactory getInstance() {
        if (fInstance == null) {
            fInstance = new LTTngControlServiceFactory();
        }
        return fInstance;
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
    public ILttngControlService getLttngControlService(ICommandShell shell) throws ExecutionException {
        // get the version
        boolean machineInterfaceMode = true;
        String command = LTTngControlServiceConstants.CONTROL_COMMAND + LTTngControlServiceConstants.COMMAND_VERSION;
        String commandMi = LTTngControlServiceConstants.CONTROL_COMMAND_MI_XML + LTTngControlServiceConstants.COMMAND_VERSION;

        // Logging
        if (ControlPreferences.getInstance().isLoggingEnabled()) {
            ControlCommandLogger.log(commandMi);
        }

        ICommandResult result = null;

        // Looking for a machine interface on LTTng side
        try {
            result = shell.executeCommand(commandMi, new NullProgressMonitor());
        } catch (ExecutionException e) {
            throw new ExecutionException(Messages.TraceControl_GettingVersionError, e);
        }

        // Output logging
        if (ControlPreferences.getInstance().isLoggingEnabled()) {
            ControlCommandLogger.log(LTTngControlService.formatOutput(result));
        }

        if (result.getResult() != 0) {
            machineInterfaceMode = false;
            // Fall back if no machine interface is present

            // Logging
            if (ControlPreferences.getInstance().isLoggingEnabled()) {
                ControlCommandLogger.log(command);
            }

            try {
                result = shell.executeCommand(command, new NullProgressMonitor());
            } catch (ExecutionException e) {
                throw new ExecutionException(Messages.TraceControl_GettingVersionError + ": " + e); //$NON-NLS-1$
            }

            // Output logging
            if (ControlPreferences.getInstance().isLoggingEnabled()) {
                ControlCommandLogger.log(LTTngControlService.formatOutput(result));
            }
        }


        if ((result != null) && (result.getResult() == 0) && (result.getOutput().length >= 1)) {
            if (machineInterfaceMode) {
                LTTngControlServiceMI service = new LTTngControlServiceMI(shell, LTTngControlService.class.getResource(LTTngControlServiceConstants.MI_XSD_FILENAME));
                service.setVersion(result.getOutput());
                return service;
            }
            int index = 0;
            while (index < result.getOutput().length) {
                String line = result.getOutput()[index];
                Matcher versionMatcher = LTTngControlServiceConstants.VERSION_PATTERN.matcher(line);
                if (versionMatcher.matches()) {
                    String version = versionMatcher.group(1).trim();
                    Matcher matcher = LTTngControlServiceConstants.VERSION_2_PATTERN.matcher(version);
                    if (matcher.matches()) {
                        LTTngControlService service = new LTTngControlService(shell);
                        service.setVersion(version);
                        return service;
                    }
                    throw new ExecutionException(Messages.TraceControl_UnsupportedVersionError + ": " + version); //$NON-NLS-1$
                }
                index++;
            }
        }
        throw new ExecutionException(Messages.TraceControl_GettingVersionError);
    }
}
