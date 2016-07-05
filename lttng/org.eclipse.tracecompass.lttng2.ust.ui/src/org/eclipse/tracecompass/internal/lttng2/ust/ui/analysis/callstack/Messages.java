/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.lttng2.ust.ui.analysis.callstack;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for the call stack analysis module
 *
 * @author Sonia Farrah
 */

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.lttng2.ust.ui.analysis.callstack.messages"; //$NON-NLS-1$

    /**
     * Self time of a function
     */
    public static String SegmentStoreStaticsViewer_selfTime;
    /**
    * Total time of a function
    */
    public static String SegmentStoreStaticsViewer_totalTime;

    /**
    * Adress of a function
    */
    public static String Function_address;
    /**
     * Total self Time of a function
     */
    public static String SegmentStoreStaticsViewer_totalSelfTime;

    /**
     * Total calls of a function
     */
    public static String SegmentStoreStaticsViewer_totalCalls;
    /**
     *The callers of a function
     */
    public static String SegmentStoreStaticsViewer_Callers;
    /**
     *The callees of a function
     */
    public static String SegmentStoreStaticsViewer_Callees;
    /**
     *The depth of a function
     */
    public static String SegmentStoreStaticsViewer_Depth;
    /**
     *The duration of a function
     */
    public static String FlameGraph_Duration;
    /**
     *The time percentage of a function
     */
    public static String FlameGraph_Percentage;
    /**
     *The number of calls of a function
     */
    public static String FlameGraph_NbreCalls;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    /** Information regarding events loading prior to the analysis execution */
    public static String LttnUstCallStackAnalysisModule_EventsLoadingInformation;

    private Messages() {
    }
}
