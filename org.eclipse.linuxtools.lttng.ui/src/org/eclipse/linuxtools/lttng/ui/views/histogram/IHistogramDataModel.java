/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.histogram;

/**
 * <b><u>IHistogramDataModel</u></b>
 * <p>
 */
import org.eclipse.linuxtools.lttng.ui.views.distribution.model.IBaseDistributionModel;

public interface IHistogramDataModel extends IBaseDistributionModel {
    /**
     * Add event to the correct bucket, compacting the if needed.
     * 
     * @param timestamp the timestamp of the event to count
     */
    public void countEvent(long eventCount, long timestamp);
    
    /**
     * Scale the model data to the width, height and bar width requested.
     * 
     * @param width
     * @param height
     * @param bar width
     * @return the result array of size [width] and where the highest value
     *         doesn't exceed [height] considering the bar width [barWidth]
     */
    public HistogramScaledData scaleTo(int width, int height, int barWidth);
}
