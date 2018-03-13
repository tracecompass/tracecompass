/******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   France Lapointe Nguyen - Initial API and implementation
 *   Bernd Hufmann -  Extracted implementation to a abstract class
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.scatter.AbstractSegmentStoreScatterChartViewer;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.SystemCallLatencyAnalysis;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfXYChartSettings;

/**
 * Displays the latency analysis data in a scatter graph
 *
 * @author France Lapointe Nguyen
 * @author Matthew Khouzam - reduced memory usage
 * @since 1.0
 */
public class SystemCallLatencyScatterGraphViewer extends AbstractSegmentStoreScatterChartViewer {

    /**
     * Constructor
     *
     * @param parent
     *            parent composite
     * @param title
     *            name of the graph
     * @param xLabel
     *            name of the x axis
     * @param yLabel
     *            name of the y axis
     */
    public SystemCallLatencyScatterGraphViewer(Composite parent, String title, String xLabel, String yLabel) {
        super(parent, new TmfXYChartSettings(title, xLabel, yLabel, 1), SystemCallLatencyAnalysis.ID);
    }

}