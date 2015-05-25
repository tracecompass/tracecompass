/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   France Lapointe Nguyen - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.osgi.util.NLS;

/**
 * @author France Lapointe Nguyen
 */
@NonNullByDefault({})
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency.messages"; //$NON-NLS-1$

    /**
     * Time vs Duration
     */
    public static String LatencyScatterView_title;

    /**
     * Time
     */
    public static String LatencyScatterView_xAxis;

    /**
     * Duration
     */
    public static String LatencyScatterView_yAxis;

    /**
     * Name of the duration column
     */
    public static String LatencyTableViewer_duration;

    /**
     * Name of the end time column
     */
    public static String LatencyTableViewer_endTime;

    /**
     * Name of the start time column
     */
    public static String LatencyTableViewer_startTime;

    /**
     * Title of action to goto start time time
     */
    public static String LatencyView_goToStartEvent;

    /**
     * Title of action to goto end event
     */
    public static String LatencyView_goToEndEvent;

    /**
     * Name of show statistics action
     */
    public static String LatencyTable_ShowStatisticsActionName;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
