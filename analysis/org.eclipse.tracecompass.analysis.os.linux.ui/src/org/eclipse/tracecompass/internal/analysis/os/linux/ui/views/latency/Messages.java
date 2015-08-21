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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * @author France Lapointe Nguyen
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency.messages"; //$NON-NLS-1$

    /**
     * Name of the duration column
     */
    public static @Nullable String LatencyTableViewer_duration;

    /**
     * Name of the end time column
     */
    public static @Nullable String LatencyTableViewer_endTime;

    /**
     * Name of the start time column
     */
    public static @Nullable String LatencyTableViewer_startTime;

    /**
     * Title of the scatter graph
     */
    public static @Nullable String LatencyView_title;

    /**
     * Title of the x axis of the scatter graph
     */
    public static @Nullable String LatencyView_xAxis;

    /**
     * Title of the y axis of the scatter graph
     */
    public static @Nullable String LatencyView_yAxis;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
