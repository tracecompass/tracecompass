/*******************************************************************************
 * Copyright (c) 2015, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density2;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.osgi.util.NLS;

/**
 * @author Marc-Andre Laperle
 * @since 4.1
 */
@NonNullByDefault({})
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density2.messages"; //$NON-NLS-1$

    /**
     * Label for the count axis of the density chart.
     */
    public static String AbstractSegmentStoreDensityViewer_CountAxisLabel;

    /**
     * Label for the time axis of the density chart.
     */
    public static String AbstractSegmentStoreDensityViewer_TimeAxisLabel;

    /**
     * Tool-tip for the Zoom out action
     */
    public static String AbstractSegmentStoreDensityViewer_ZoomOutActionToolTipText;

    /**
     * Label for the series of the density chart.
     */
    public static String AbstractSegmentStoreDensityViewer_SeriesLabel;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
