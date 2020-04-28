/**********************************************************************
 * Copyright (c) 2017, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for the segment store analysis module
 *
 * @author Yonni Chen
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.messages"; //$NON-NLS-1$

    /** File size property title */
    public static @Nullable String SegmentStoreAnalysis_PropertiesFileSize;
    /**
     * Analysis not executed property text
     */
    public static @Nullable String SegmentStoreAnalysis_PropertiesAnalysisNotExecuted;
    /** Error getting file size property text */
    public static @Nullable String SegmentStoreAnalysis_ErrorGettingFileSize;

    /**
     * XY series name
     */
    public static @Nullable String SegmentStoreDataProvider_Duration;

    /**
     * XY chart title
     */
    public static @Nullable String SegmentStoreScatterGraphViewer_title;

    /**
     * Error message to say that SegmentStore must be an IAnalysisModule
     */
    public static @Nullable String SegmentStoreDataProvider_SegmentMustBeAnIAnalysisModule;

    /**
     * Error message to say that SegmentStore is not available
     */
    public static @Nullable String SegmentStoreDataProvider_SegmentNotAvailable;

    /**
     * Externalized name of the Selection entry
     */
    public static @Nullable String SegmentStoreStatisticsDataProvider_Selection;

    /**
     * Externalized name of the Total entry
     */
    public static @Nullable String SegmentStoreStatisticsDataProvider_Total;

    /**
     * Analysis name
     */
    public static @Nullable String SegmentStoreStatisticsDataProviderFactory_AnalysisName;

    /**
     * Title of the data provider
     */
    public static @Nullable String SegmentStoreScatterGraphDataProvider_title;

    /**
     * Description of the data provider
     */
    public static @Nullable String SegmentStoreScatterGraphDataProvider_description;

    /**
     * Name of the label column
     */
    public static @Nullable String SegmentStoreStatistics_Label;
    /**
     * Name of the minimum column
     */
    public static @Nullable String SegmentStoreStatistics_MinLabel;
    /**
     * Name of the maximum column
     */
    public static @Nullable String SegmentStoreStatistics_MaxLabel;
    /**
     * Name of the average column
     */
    public static @Nullable String SegmentStoreStatistics_AverageLabel;
    /**
     * Name of the count column
     */
    public static @Nullable String SegmentStoreStatistics_CountLabel;
    /**
     * Name of the total column
     */
    public static @Nullable String SegmentStoreStatistics_TotalLabel;
    /**
     * Name of the standard deviation column
     */
    public static @Nullable String SegmentStoreStatistics_StandardDeviationLabel;
    /**
     * Name of the minimum start time column
     */
    public static @Nullable String SegmentStoreStatistics_MinStartLabel;
    /**
     * Name of the minimum end time column
     */
    public static @Nullable String SegmentStoreStatistics_MinEndLabel;
    /**
     * Name of the maximum start time column
     */
    public static @Nullable String SegmentStoreStatistics_MaxStartLabel;
    /**
     * Name of the maximum end time column
     */
    public static @Nullable String SegmentStoreStatistics_MaxEndLabel;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
