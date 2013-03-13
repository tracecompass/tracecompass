/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Updated for TMF 2.0
 *******************************************************************************/

package org.eclipse.linuxtools.internal.gdbtrace.ui.views;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.linuxtools.internal.gdbtrace.ui.GdbTraceUIPlugin;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

/**
 * GDB Trace perspective factory
 * @author Francois Chouinard
 */
public class GdbPerspectiveFactory implements IPerspectiveFactory {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /** The perspective ID */
    public static final String ID = GdbTraceUIPlugin.PLUGIN_ID + ".perspective"; //$NON-NLS-1$

    // Folders
    private static final String EditorArea         = IPageLayout.ID_EDITOR_AREA;
    private static final String ProjectFolder      = "ProjectFolder";        //$NON-NLS-1$
    private static final String ConsoleFolder      = "ConsoleFolder";        //$NON-NLS-1$
    private static final String DebugFolder        = "DebugFolder";          //$NON-NLS-1$
    private static final String TraceControlFolder = "TraceControlFolder";   //$NON-NLS-1$

    // Standard Debug Views
    private static final String DEBUG_VIEW_ID = IDebugUIConstants.ID_DEBUG_VIEW;
    private static final String TRACE_CONTROL_VIEW_ID = "org.eclipse.cdt.dsf.gdb.ui.tracecontrol.view"; //$NON-NLS-1$

    // Standard Eclipse views
    private static final String PROJECT_VIEW_ID = IPageLayout.ID_PROJECT_EXPLORER;
    private static final String CONSOLE_VIEW_ID = IConsoleConstants.ID_CONSOLE_VIEW;

    // ------------------------------------------------------------------------
    // IPerspectiveFactory
    // ------------------------------------------------------------------------

    @Override
    public void createInitialLayout(IPageLayout layout) {

        layout.setEditorAreaVisible(true);

        // Create the project folder
        IFolderLayout projectFolder = layout.createFolder(ProjectFolder, IPageLayout.LEFT, 0.15f, EditorArea);
        projectFolder.addView(PROJECT_VIEW_ID);

        // Create the console folder
        IFolderLayout consoleFolder = layout.createFolder(ConsoleFolder, IPageLayout.BOTTOM, 0.50f, ProjectFolder);
        consoleFolder.addView(CONSOLE_VIEW_ID);

        // Create the debug folder
        IFolderLayout debugFolder = layout.createFolder(DebugFolder, IPageLayout.TOP, 0.50f, EditorArea);
        debugFolder.addView(DEBUG_VIEW_ID);

        // Create the middle right folder
        IFolderLayout traceControlFolder = layout.createFolder(TraceControlFolder, IPageLayout.RIGHT, 0.50f, DebugFolder);
        traceControlFolder.addView(TRACE_CONTROL_VIEW_ID);
    }
}
