/*******************************************************************************
 * Copyright (c) 2016, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.xyscatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.osgi.util.NLS;

/**
 * @author France Bernd Hufmann
 */
@NonNullByDefault({})
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.xyscatter.messages"; //$NON-NLS-1$

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
