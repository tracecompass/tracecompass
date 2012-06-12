/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Francois Chouinard - Moved from LTTng to TMF
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.histogram;

import org.eclipse.linuxtools.tmf.ui.views.distribution.model.IBaseDistributionModel;

/**
 * Histogram data model interface.
 * 
 * @version 1.0
 * @author Bernd Hufmann
 */
public interface IHistogramDataModel extends IBaseDistributionModel {
    /**
     * Add event to the correct bucket, compacting the if needed.
     * 
     * @param eventCount the event to count 
     * @param timestamp the timestamp of the event to count
     */
    public void countEvent(long eventCount, long timestamp);
    /**
     * Scale the model data to the width, height and bar width requested.
     *
     * @param width A width of the histogram canvas
     * @param height A height of the histogram canvas
     * @param barWidth A width (in pixel) of a histogram bar
     * @return the result array of size [width] and where the highest value doesn't exceed [height]
     *         while considering the bar width [barWidth]
     */
    public HistogramScaledData scaleTo(int width, int height, int barWidth);

}
