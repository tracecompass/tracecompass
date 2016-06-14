/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.lttng2.control.ui.swtbot.tests;

import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * Perspective factory used during ControlView SWTBot test
 */
public class ControlPerspectiveFactory implements IPerspectiveFactory {

    private static final String PROJECT_VIEW_ID = IPageLayout.ID_PROJECT_EXPLORER;
    private static final String CONTROL_VIEW_ID = ControlView.ID;
    private static final String PROPERTIES_VIEW_ID = IPageLayout.ID_PROP_SHEET;

    @Override
    public void createInitialLayout(IPageLayout layout) {
        layout.setEditorAreaVisible(true);

        // Create the top left folder
        IFolderLayout topLeftFolder = layout.createFolder(
                "topLeftFolder", IPageLayout.LEFT, 0.15f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
        topLeftFolder.addView(PROJECT_VIEW_ID);

        // Create the bottom left folder
        IFolderLayout bottomLeftFolder = layout.createFolder(
                "bottomLeftFolder", IPageLayout.BOTTOM, 0.70f, "topLeftFolder"); //$NON-NLS-1$ //$NON-NLS-2$
        bottomLeftFolder.addView(CONTROL_VIEW_ID);

        // Create the bottom right folder
        IFolderLayout bottomRightFolder = layout.createFolder(
                "bottomRightFolder", IPageLayout.BOTTOM, 0.50f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
        bottomRightFolder.addView(PROPERTIES_VIEW_ID);
    }

}
