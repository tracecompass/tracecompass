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

import org.eclipse.osgi.util.NLS;

/**
 * <b><u>ViewsLabels</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class Labels extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.lttng.ui.views.labels"; //$NON-NLS-1$

    // Control Flow View
    public static String ControlFlowView_ID;

    // Control View
    public static String ControlView_ID;

    // Events View
    public static String EventsView_ID;

    // Histogram View
    public static String HistogramView_ID;

    // Project View
    public static String ProjectView_ID;

    // resources View
    public static String ResourcesView_ID;

    // Statistics View
    public static String StatisticsView_ID;

    // Time Frame view
    public static String TimeFrameView_ID;
    public static String TimeFrameView_Seconds;
    public static String TimeFrameView_Nanosec;
    public static String TimeFrameView_StartTime;
    public static String TimeFrameView_EndTime;
    public static String TimeFrameView_TimeRange;
    public static String TimeFrameView_CurrentTime;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Labels.class);
    }

    private Labels() {
    }

}
