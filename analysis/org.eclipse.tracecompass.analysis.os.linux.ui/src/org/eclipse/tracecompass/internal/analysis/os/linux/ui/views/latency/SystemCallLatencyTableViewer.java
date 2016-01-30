/*******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   France Lapointe Nguyen - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.SystemCallLatencyAnalysis;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * Displays the latency analysis data in a column table
 *
 * @author France Lapointe Nguyen
 */
public class SystemCallLatencyTableViewer extends AbstractSegmentStoreTableViewer {

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param tableViewer
     *            The table viewer
     */
    public SystemCallLatencyTableViewer(TableViewer tableViewer) {
        super(tableViewer);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected @Nullable ISegmentStoreProvider getSegmentStoreProvider(ITmfTrace trace) {
          return TmfTraceUtils.getAnalysisModuleOfClass(trace, SystemCallLatencyAnalysis.class, SystemCallLatencyAnalysis.ID);
    }
}
