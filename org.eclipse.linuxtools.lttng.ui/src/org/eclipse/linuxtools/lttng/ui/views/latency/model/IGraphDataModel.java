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
package org.eclipse.linuxtools.lttng.ui.views.latency.model;

import org.eclipse.linuxtools.tmf.ui.views.distribution.model.IBaseDistributionModel;

/**
 * <b><u>IGraphDataModel</u></b>
 * <p>
 */
public interface IGraphDataModel extends IBaseDistributionModel {

    /**
     * Add event to the correct bucket, compacting the if needed.
     * 
     * @param eventCount - the event count
     * @param timestamp - the timestamp (x-coordinate) of the event to count
     * @param time - the time (y-coordinate) of the event to count
     */
    public void countEvent(int eventCount, long timestamp, long time);
    
    /**
     * Scale the model data to the width and height requested.
     * 
     * @param width - width of graph
     * @param height - height of graph
     * @param barWidth - width of bar
     * @return the result array of size [width] and where the highest value
     *         doesn't exceed [height]
     */
    public GraphScaledData scaleTo(int width, int height, int barWidth);
}
