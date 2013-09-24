/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views;

import org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.controlflow.ControlFlowView;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources.ResourcesView;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.ControlView;
import org.eclipse.linuxtools.tmf.ui.project.wizards.NewTmfProjectWizard;
import org.eclipse.linuxtools.tmf.ui.views.histogram.HistogramView;
import org.eclipse.linuxtools.tmf.ui.views.statistics.TmfStatisticsView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * The default LTTng perspective.
 */
public class PerspectiveFactory implements IPerspectiveFactory {

    /** Perspective ID */
    public static final String ID = "org.eclipse.linuxtools.lttng2.ui.perspective"; //$NON-NLS-1$

    // LTTng views
    private static final String HISTOGRAM_VIEW_ID = HistogramView.ID;
    private static final String CONTROL_VIEW_ID = ControlView.ID;
    private static final String CONTROLFLOW_VIEW_ID = ControlFlowView.ID;
    private static final String RESOURCES_VIEW_ID = ResourcesView.ID;
    private static final String STATISTICS_VIEW_ID = TmfStatisticsView.ID;

    // Standard Eclipse views
    private static final String PROJECT_VIEW_ID = IPageLayout.ID_PROJECT_EXPLORER;
    private static final String PROPERTIES_VIEW_ID = IPageLayout.ID_PROP_SHEET;
    private static final String BOOKMARKS_VIEW_ID = IPageLayout.ID_BOOKMARKS;

    @Override
    public void createInitialLayout(IPageLayout layout) {

        layout.setEditorAreaVisible(true);

        addFastViews(layout);
        addViewShortcuts(layout);
        addPerspectiveShortcuts(layout);

        // Create the top left folder
        IFolderLayout topLeftFolder = layout.createFolder(
                "topLeftFolder", IPageLayout.LEFT, 0.15f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
        topLeftFolder.addView(PROJECT_VIEW_ID);

        // Create the bottom left folder
        IFolderLayout bottomLeftFolder = layout.createFolder(
                "bottomLeftFolder", IPageLayout.BOTTOM, 0.70f, "topLeftFolder"); //$NON-NLS-1$ //$NON-NLS-2$
        bottomLeftFolder.addView(CONTROL_VIEW_ID);

        // Create the top right folder
        IFolderLayout topRightFolder = layout.createFolder(
                "topRightFolder", IPageLayout.TOP, 0.40f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
        topRightFolder.addView(CONTROLFLOW_VIEW_ID);
        topRightFolder.addView(RESOURCES_VIEW_ID);
        topRightFolder.addView(STATISTICS_VIEW_ID);

        // Create the bottom right folder
        IFolderLayout bottomRightFolder = layout.createFolder(
                "bottomRightFolder", IPageLayout.BOTTOM, 0.50f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
        bottomRightFolder.addView(HISTOGRAM_VIEW_ID);
        bottomRightFolder.addView(PROPERTIES_VIEW_ID);
        bottomRightFolder.addView(BOOKMARKS_VIEW_ID);

        layout.addNewWizardShortcut(NewTmfProjectWizard.ID);
    }

    /**
     * Add fast views to the perspective
     *
     * @param layout
     */
    private void addFastViews(IPageLayout layout) {
    }

    /**
     * Add view shortcuts to the perspective
     *
     * @param layout
     */
    private void addViewShortcuts(IPageLayout layout) {
    }

    /**
     * Add perspective shortcuts to the perspective
     *
     * @param layout
     */
    private void addPerspectiveShortcuts(IPageLayout layout) {
    }

}
