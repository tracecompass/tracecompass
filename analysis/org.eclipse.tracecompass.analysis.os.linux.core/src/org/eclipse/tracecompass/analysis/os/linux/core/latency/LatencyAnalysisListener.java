/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.latency;

import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;

/**
 * End of latency analysis listener for latency viewers
 *
 * @author France Lapointe Nguyen
 * @since 2.0
 */
public interface LatencyAnalysisListener {

    /**
     * Called at the end of the latency analysis
     *
     * @param activeAnalysis
     *            latency analysis that is running
     *
     * @param data
     *            results of the latency analysis
     */
    void onComplete(LatencyAnalysis activeAnalysis, ISegmentStore<ISegment> data);

}
