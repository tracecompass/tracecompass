/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Changed to updated histogram data model   
 *   Francois Chouinard - Moved from LTTng to TMF
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.histogram;

import org.eclipse.swt.widgets.Composite;

/**
 * <b><u>TimeRangeHistogram</u></b>
 * <p>
 * A basic histogram with the following additional features:
 * <ul>
 * <li>zoom in: mouse wheel up (or forward)
 * <li>zoom out: mouse wheel down (or backward)
 * </ul>
 */
public class TimeRangeHistogram extends Histogram {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    HistogramZoom fZoom = null;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    public TimeRangeHistogram(HistogramView view, Composite parent) {
        super(view, parent);
        fZoom = new HistogramZoom(this, fCanvas, getStartTime(), getTimeLimit());
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void updateTimeRange(long startTime, long endTime) {
        ((HistogramView) fParentView).updateTimeRange(startTime, endTime);
    }

    @Override
    public synchronized void clear() {
        if (fZoom != null)
            fZoom.stop();
        super.clear();
    }

    public synchronized void setTimeRange(long startTime, long duration) {
        fZoom.setNewRange(startTime, duration);
    }

    public void setFullRange(long startTime, long endTime) {
        long currentFirstEvent = getStartTime();
        fZoom.setFullRange((currentFirstEvent == 0) ? startTime : currentFirstEvent, endTime);
    }

}
