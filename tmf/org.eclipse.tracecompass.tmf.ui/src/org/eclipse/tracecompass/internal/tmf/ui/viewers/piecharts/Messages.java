/*******************************************************************************
 * Copyright (c) 2015, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexis Cabana-Loriaux - Initial API and Implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.viewers.piecharts;

import org.eclipse.osgi.util.NLS;

/**
 * Messages file for statistics view strings.
 *
 * @author Alexis Cabana-Loriaux
 * @since 2.0
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.ui.viewers.piecharts.messages"; //$NON-NLS-1$

    /**
     * String shown on top of the time-range selection piechart
     */
    public static String TmfStatisticsView_TimeRangeSelectionPieChartName;

    /**
     * String given to the slice in the piechart containing the too little
     * slices
     */
    public static String TmfStatisticsView_PieChartOthersSliceName;

    /**
     * String for the top of the global selection piechart
     */
    public static String TmfStatisticsView_GlobalSelectionPieChartName;

    /**
     * The string in the tooltip text of the piecharts
     */
    public static String TmfStatisticsView_PieChartToolTipTextName;

    /**
     * The string in the tooltip text of the piecharts
     */
    public static String TmfStatisticsView_PieChartToolTipTextEventCount;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
