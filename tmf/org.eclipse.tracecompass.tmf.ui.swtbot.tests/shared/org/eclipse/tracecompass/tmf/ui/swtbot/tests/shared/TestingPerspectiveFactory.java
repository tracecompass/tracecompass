/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared;

import org.eclipse.tracecompass.tmf.ui.project.wizards.NewTmfProjectWizard;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * The Testing perspective definition.
 */
public class TestingPerspectiveFactory implements IPerspectiveFactory {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /** The Perspective ID */
    public static final String ID = "org.eclipse.tracecompass.tmf.ui.swtbot.tests.perspective"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // IPerspectiveFactory
    // ------------------------------------------------------------------------

    @Override
    public void createInitialLayout(IPageLayout layout) {

        // Editor area
        layout.setEditorAreaVisible(true);

        // Create the left folder
        IFolderLayout leftFolder = layout.createFolder(
                "leftFolder", IPageLayout.LEFT, 0.25f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
        leftFolder.addView(IPageLayout.ID_PROJECT_EXPLORER);

        // Create the bottom right folder
        IFolderLayout bottomRightFolder = layout.createFolder(
                "bottomRightFolder", IPageLayout.BOTTOM, 0.5f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
        bottomRightFolder.addView(IPageLayout.ID_PROP_SHEET);

        // Populate menus, etc
        layout.addPerspectiveShortcut(ID);
        layout.addNewWizardShortcut(NewTmfProjectWizard.ID);
    }

}
