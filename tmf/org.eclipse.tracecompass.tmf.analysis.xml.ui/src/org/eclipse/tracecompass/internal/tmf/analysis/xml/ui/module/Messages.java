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
package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.module;

import org.eclipse.osgi.util.NLS;

/**
 * Message for the XML analysis output
 *
 * @author Jean-Christian Kouame
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.module.messages"; //$NON-NLS-1$
    /** Density chart title */
    public static String TmfXmlAnalysisOutputSource_DensityChartTitle;
    /** Statistics Table Title */
    public static String TmfXmlAnalysisOutputSource_LatencyStatisticsTitle;
    /** Latency table */
    public static String TmfXmlAnalysisOutputSource_LatencyTable;
    /** Scatter graph title */
    public static String TmfXmlAnalysisOutputSource_ScatterGraphTitle;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
