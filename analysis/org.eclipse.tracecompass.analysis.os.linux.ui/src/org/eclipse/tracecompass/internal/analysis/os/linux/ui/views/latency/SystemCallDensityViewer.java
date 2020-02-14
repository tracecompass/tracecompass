/******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density2.AbstractSegmentStoreDensityViewer;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.SystemCallLatencyAnalysis;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * System Call density viewer
 *
 * @author Matthew Khouzam
 * @author Marc-Andre Laperle
 */
public class SystemCallDensityViewer extends AbstractSegmentStoreDensityViewer {

    /**
     * Constructs a new density viewer.
     *
     * @param parent
     *            the parent of the viewer
     */
    public SystemCallDensityViewer(Composite parent) {
        super(parent);
    }

    @Override
    protected @Nullable ISegmentStoreProvider getSegmentStoreProvider(ITmfTrace trace) {
        return TmfTraceUtils.getAnalysisModuleOfClass(trace, SystemCallLatencyAnalysis.class, SystemCallLatencyAnalysis.ID);
    }
}
