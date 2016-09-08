/*******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.statistics;

import org.eclipse.osgi.util.NLS;

/**
 * Messages used in the LTTng kernel CPU usage view and viewers.
 *
 * @author Bernd Hufmann
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.statistics.messages"; //$NON-NLS-1$

    /** Default "total" label in statistics viewer */
    public static String AbstractSegmentStoreStatisticsViewer_total;
    /** Default "category" label in statistics viewer */
    public static String AbstractSegmentStoreStatisticsViewer_types;

    /** Name of level column */
    public static String SegmentStoreStatistics_LevelLabel;
    /** Name of the minimum column */
    public static String SegmentStoreStatistics_Statistics_MinLabel;
    /** Name of maximum column */
    public static String SegmentStoreStatistics_MaxLabel;
    /** Name of average column */
    public static String SegmentStoreStatistics_AverageLabel;
    /** Name of count column */
    public static String SegmentStoreStatisticsViewer_Count;
    /**
     * Name of count column
     *
     * @since 1.1
     */
    public static String SegmentStoreStatisticsViewer_Total;
    /** Name of average column */
    public static String SegmentStoreStatisticsViewer_StandardDeviation;
    /** Menu item for go to minimum duration */
    public static String SegmentStoreStatisticsViewer_GotoMinAction;
    /** Menu item for go to maximum duration */
    public static String SegmentStoreStatisticsViewer_GotoMaxAction;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
