/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial API and Implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.viewers.statistics;

import org.eclipse.osgi.util.NLS;

/**
 * Messages file for statistics view strings.
 *
 * @author Mathieu Denis
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.tmf.ui.viewers.statistics.messages"; //$NON-NLS-1$

    /**
     * String for unknown trace name.
     */
    public static String TmfStatisticsView_UnknownTraceName;

    /**
     * String shown on top of the time-range selection piechart
     *
     * @since 2.0
     */
    public static String TmfStatisticsView_TimeRangeSelectionPieChartName;

    /**
     * String given to the slice in the piechart containing the too little
     * slices
     * @since 2.0
     */
    public static String TmfStatisticsView_PieChartOthersSliceName;

    /**
     * String for the top of the global selection piechart
     * @since 2.0
     */
    public static String TmfStatisticsView_GlobalSelectionPieChartName;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
