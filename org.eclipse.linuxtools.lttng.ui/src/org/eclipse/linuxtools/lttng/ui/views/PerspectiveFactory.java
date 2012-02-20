/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.views;

import org.eclipse.linuxtools.lttng.ui.views.controlflow.ControlFlowView;
import org.eclipse.linuxtools.lttng.ui.views.histogram.HistogramView;
import org.eclipse.linuxtools.lttng.ui.views.resources.ResourcesView;
import org.eclipse.linuxtools.lttng.ui.views.statistics.StatisticsView;
import org.eclipse.linuxtools.tmf.ui.views.events.TmfEventsView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * <b><u>PerspectiveFactory</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class PerspectiveFactory implements IPerspectiveFactory {

    // Perspective ID
    public static final String ID = "org.eclipse.linuxtools.lttng.ui.perspective"; //$NON-NLS-1$

    // LTTng views
    private static final String EVENTS_VIEW_ID = TmfEventsView.ID;
    private static final String CONTROL_FLOW_VIEW_ID = ControlFlowView.ID;
    private static final String RESOURCES_VIEW_ID = ResourcesView.ID;
    private static final String STATISTICS_VIEW_ID = StatisticsView.ID;
    private static final String HISTOGRAM_VIEW_ID = HistogramView.ID;

    // Standard Eclipse views
    private static final String PROJECT_VIEW_ID = IPageLayout.ID_PROJECT_EXPLORER;
    private static final String PROPERTIES_VIEW_ID = IPageLayout.ID_PROP_SHEET;
    private static final String BOOKMARKS_VIEW_ID = IPageLayout.ID_BOOKMARKS;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
     */
    @Override
    public void createInitialLayout(IPageLayout layout) {

        layout.setEditorAreaVisible(false);

        addFastViews(layout);
        addViewShortcuts(layout);
        addPerspectiveShortcuts(layout);

        // Create the top left folder
        IFolderLayout topLeftFolder = layout.createFolder(
                "topLeftFolder", IPageLayout.LEFT, 0.15f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
        topLeftFolder.addView(PROJECT_VIEW_ID);

        // Create the top right folder
        IFolderLayout topRightFolder = layout.createFolder(
                "topRightFolder", IPageLayout.TOP, 0.40f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
        topRightFolder.addView(CONTROL_FLOW_VIEW_ID);
        topRightFolder.addView(RESOURCES_VIEW_ID);
        topRightFolder.addView(STATISTICS_VIEW_ID);

        // Create the middle right folder
        IFolderLayout middleRightFolder = layout.createFolder(
                "middleRightFolder", IPageLayout.BOTTOM, 0.40f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$ //$NON-NLS-2$
        middleRightFolder.addView(EVENTS_VIEW_ID);

        // Create the bottom right folder
        IFolderLayout bottomRightFolder = layout.createFolder(
                "bottomRightFolder", IPageLayout.BOTTOM, 0.50f, "middleRightFolder"); //$NON-NLS-1$ //$NON-NLS-2$
        bottomRightFolder.addView(HISTOGRAM_VIEW_ID);
        bottomRightFolder.addView(PROPERTIES_VIEW_ID);
        bottomRightFolder.addView(BOOKMARKS_VIEW_ID);
    }

    /**
     * Add fast views to the perspective
     * 
     * @param layout
     */
    private void addFastViews(IPageLayout layout) {
        // TODO Auto-generated method stub
    }

    /**
     * Add view shortcuts to the perspective
     * 
     * @param layout
     */
    private void addViewShortcuts(IPageLayout layout) {
        // TODO Auto-generated method stub
    }

    /**
     * Add perspective shortcuts to the perspective
     * 
     * @param layout
     */
    private void addPerspectiveShortcuts(IPageLayout layout) {
        // TODO Auto-generated method stub
    }

}
