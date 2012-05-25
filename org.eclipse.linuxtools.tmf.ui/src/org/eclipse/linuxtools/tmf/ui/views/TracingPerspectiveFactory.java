/*******************************************************************************
 * Copyright (c) 2010, 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views;

import org.eclipse.linuxtools.tmf.ui.views.events.TmfEventsView;
import org.eclipse.linuxtools.tmf.ui.views.histogram.HistogramView;
import org.eclipse.linuxtools.tmf.ui.views.statistics.TmfStatisticsView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * <b><u>TracingPerspectiveFactory</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TracingPerspectiveFactory implements IPerspectiveFactory {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    // Perspective ID
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.perspective.tracing"; //$NON-NLS-1$

    // Standard TMF views
    private static final String EVENTS_VIEW_ID = TmfEventsView.ID;
    private static final String HISTOGRAM_VIEW_ID = HistogramView.ID;

    // Standard Eclipse views
    private static final String PROJECT_VIEW_ID = IPageLayout.ID_PROJECT_EXPLORER;
    private static final String STATISTICS_VIEW_ID = TmfStatisticsView.ID;
    private static final String PROPERTIES_VIEW_ID = IPageLayout.ID_PROP_SHEET;
    private static final String BOOKMARKS_VIEW_ID = IPageLayout.ID_BOOKMARKS;

    // ------------------------------------------------------------------------
    // IPerspectiveFactory
    // ------------------------------------------------------------------------

    @Override
    public void createInitialLayout(IPageLayout layout) {

        // No editor part
        layout.setEditorAreaVisible(false);

        // Goodies
        addFastViews(layout);
        addViewShortcuts(layout);
        addPerspectiveShortcuts(layout);

        // Create the top left folder
        IFolderLayout topLeftFolder = layout.createFolder(
                "topLeftFolder", IPageLayout.LEFT, 0.15f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
        topLeftFolder.addView(PROJECT_VIEW_ID);

//        // Create the bottom left folder
//        @SuppressWarnings("unused")
//        IFolderLayout bottomLeftFolder = layout.createFolder(
//                "bottomLeftFolder", IPageLayout.BOTTOM, 0.50f, "topLeftFolder"); //$NON-NLS-1$//$NON-NLS-2$

        // Create the top right folder
        IFolderLayout topRightFolder = layout.createFolder(
                "topRightFolder", IPageLayout.TOP, 0.40f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
        topRightFolder.addView(EVENTS_VIEW_ID);

        // Create the middle right folder
        IFolderLayout middleRightFolder = layout.createFolder(
                "middleRightFolder", IPageLayout.BOTTOM, 0.40f, "topRightFolder"); //$NON-NLS-1$//$NON-NLS-2$
        middleRightFolder.addView(STATISTICS_VIEW_ID);

        // Create the bottom right folder
        IFolderLayout bottomRightFolder = layout.createFolder(
                "bottomRightFolder", IPageLayout.BOTTOM, 0.50f, "middleRightFolder"); //$NON-NLS-1$ //$NON-NLS-2$
        bottomRightFolder.addView(HISTOGRAM_VIEW_ID);
        bottomRightFolder.addView(PROPERTIES_VIEW_ID);
        bottomRightFolder.addView(BOOKMARKS_VIEW_ID);
    }

    // ------------------------------------------------------------------------
    // Helper functions
    // ------------------------------------------------------------------------

    private void addFastViews(IPageLayout layout) {
    }

    private void addViewShortcuts(IPageLayout layout) {
    }

    private void addPerspectiveShortcuts(IPageLayout layout) {
    }

}
