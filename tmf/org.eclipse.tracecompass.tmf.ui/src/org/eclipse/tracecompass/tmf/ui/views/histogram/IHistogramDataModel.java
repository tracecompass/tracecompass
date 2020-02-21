/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Francois Chouinard - Moved from LTTng to TMF
 *   Xavier Raynaud - Support multi-trace coloring
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.histogram;

import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.views.distribution.model.IBaseDistributionModel;

/**
 * Histogram data model interface.
 *
 * @author Bernd Hufmann
 */
public interface IHistogramDataModel extends IBaseDistributionModel {
    /**
     * Add event to the correct bucket, compacting the if needed.
     *
     * @param eventCount the event to count
     * @param timestamp the timestamp of the event to count
     * @param trace the trace corresponding to given events
     */
    void countEvent(long eventCount, long timestamp, ITmfTrace trace);

    /**
     * Scale the model data to the width, height and bar width requested.
     *
     * @param width A width of the histogram canvas
     * @param height A height of the histogram canvas
     * @param barWidth A width (in pixel) of a histogram bar
     * @return the result array of size [width] and where the highest value doesn't exceed [height]
     *         while considering the bar width [barWidth]
     */
    HistogramScaledData scaleTo(int width, int height, int barWidth);

}
