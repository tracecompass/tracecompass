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
 * <b><u>IHistogramDataModel</u></b>
 * <p>
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
     * @param width
     * @param height
     * @param barWidth
     * @return the result array of size [width] and where the highest value
     *         doesn't exceed [height] considering the bar width [barWidth]
     */
    public HistogramScaledData scaleTo(int width, int height, int barWidth);

}
