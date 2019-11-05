/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.tracing.rcp.ui.messages;

import org.eclipse.osgi.util.NLS;

/**
 * Messages file for the tracing RCP.
 *
 * @author Bernd Hufmann
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tracing.rcp.ui.messages.messages"; //$NON-NLS-1$

    /** Error title for error during workspace creation */
    public static String Application_WorkspaceCreationError;
    /** Error message if workspace root doesn't exist */
    public static String Application_WorkspaceRootNotExistError;
    /** Error message if workspace root is write protected */
    public static String Application_WorkspaceRootPermissionError;
    /** Error message if workspace is already in use */
    public static String Application_WorkspaceInUseError;
    /** Error message if workspace can't be saved during shutdown */
    public static String Application_WorkspaceSavingError;
    /** Error message for internal errors */
    public static String Application_InternalError;

    /** Version string */
    public static String SplahScreen_VersionString;

    /** List capabilities description */
    public static String CliParser_ListCapabilitiesDescription;
    /** Open trace description */
    public static String CliParser_OpenTraceDescription;
    /** List of supported tracetypes text */
    public static String CliParser_ListSupportedTraceTypes;
    /** List of supported tracetypes text */
    public static String CliParser_OpeningTraces;
    /** The path to the trace does not exist */
    public static String CliParser_TraceDoesNotExist;
    /** The trace opening was canceled while there were still traces to open */
    public static String CliParser_CancelStillWaiting;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
