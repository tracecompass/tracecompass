/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for the segment store analysis module
 *
 * @author Yonni Chen
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.messages"; //$NON-NLS-1$

    /**
     * XY series name
     */
    public static String SegmentStoreDataProvider_Duration;

    /**
     * XY chart title
     */
    public static String SegmentStoreScatterGraphViewer_title;

    /**
     * Error message to say that SegmentStore must be an IAnalysisModule
     */
    public static String SegmentStoreDataProvider_SegmentMustBeAnIAnalysisModule;

    /**
     * Error message to say that SegmentStore is not available
     */
    public static String SegmentStoreDataProvider_SegmentNotAvailable;

    /**
     * Externalized name of the Selection entry
     */
    public static String SegmentStoreStatisticsDataProvider_Selection;

    /**
     * Externalized name of the Total entry
     */
    public static String SegmentStoreStatisticsDataProvider_Total;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
