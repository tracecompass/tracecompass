/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.scatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.osgi.util.NLS;

/**
 * @author France Bernd Hufmann
 */
@NonNullByDefault({})
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.scatter.messages"; //$NON-NLS-1$

    /**
     * Category
     */
    public static String SegmentStoreScatterGraphTooltipProvider_category;

    /**
     * Title of the scatter graph
     */
    public static String SegmentStoreScatterGraphViewer_title;

    /**
     * Title of the x axis of the scatter graph
     */
    public static String SegmentStoreScatterGraphViewer_xAxis;

    /**
     * Title of the y axis of the scatter graph
     */
    public static String SegmentStoreScatterGraphViewer_yAxis;

    /**
     * Legend
     */
    public static String SegmentStoreScatterGraphViewer_legend;

    /**
     * Name of the compacting job
     */
    public static String SegmentStoreScatterGraphViewer_compactTitle;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
