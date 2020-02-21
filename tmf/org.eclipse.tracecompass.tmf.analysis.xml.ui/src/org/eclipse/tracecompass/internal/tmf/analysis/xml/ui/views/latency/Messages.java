/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.latency;

import org.eclipse.osgi.util.NLS;

/**
 * Message for XML analysis latency views
 *
 * @author Jean-Christian Kouame
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.latency.messages"; //$NON-NLS-1$
    /**
     * Scatter graph title
     */
    public static String PatternLatencyViews_ScatterGraphTitle;
    /**
     * Scatter graph X label
     */
    public static String PatternLatencyViews_ScatterGraphXLabel;
    /**
     * Scatter graph Y label
     */
    public static String PatternLatencyViews_ScatterGraphYLabel;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
