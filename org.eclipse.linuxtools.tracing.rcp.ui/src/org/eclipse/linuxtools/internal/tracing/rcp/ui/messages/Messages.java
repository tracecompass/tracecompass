/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.tracing.rcp.ui.messages;

import org.eclipse.osgi.util.NLS;

/**
 * Messages file for the tracing RCP.
 *
 * @author Bernd Hufmann
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.tracing.rcp.ui.messages.messages"; //$NON-NLS-1$

    /** The name of the default tracing project. */
    public static String ApplicationWorkbenchWindowAdvisor_DefaultProjectName;
    /** The RCP title. */
    public static String ApplicationWorkbenchWindowAdvisor_WindowTitle;
    /** Error title for error during workspace creation */
    public static String Application_WorkspaceCreationError;
    /** Error message if workspace root doesn't exist */
    public static String Application_WorkspaceRootNotExistError;
    /** Error message if workspace root is write protected */
    public static String Application_WorkspaceRootPermissionError;
    /** Malformed command */
    public static String CliParser_MalformedCommand;
    /** Unkown command */
    public static String CliParser_UnknownCommand;
    /** Select trace type title */
    public static String OpenDirHandler_SelectTraceType;
    /** Select trace file */
    public static String OpenFileHandler_SelectTraceFile;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
