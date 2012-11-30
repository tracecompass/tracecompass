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
 *   Francois Chouinard - Moved from LTTng to TMF
 *   Francois Chouinard - Simplified constructor, handle interval format change
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.histogram;

import java.text.ParseException;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestampFormat;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimestampFormatUpdateSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.swt.widgets.Composite;

/**
 * This control provides a group containing a text control.
 *
 * @version 1.1
 * @author Francois Chouinard
 */
public class HistogramCurrentTimeControl extends HistogramTextControl {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private long fTraceStartTime;

    // ------------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------------

    /**
     * Standard constructor
     *
     * @param parentView A parent histogram view
     * @param parent A parent composite to draw in
     * @param groupLabel A group value
     * @param value A value
     * @since 2.0
     */
    public HistogramCurrentTimeControl(HistogramView parentView, Composite parent,
            String groupLabel, long value)
    {
        super(parentView, parent, groupLabel, value);
        TmfSignalManager.register(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.histogram.HistogramTextControl#dispose()
     */
    @Override
    public void dispose() {
        TmfSignalManager.deregister(this);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected void updateValue() {
        String string = fTextValue.getText();
        long value = 0;
        try {
            value = TmfTimestampFormat.getDefaulTimeFormat().parseValue(string, fTraceStartTime);
        } catch (ParseException e) {
        }
        if (getValue() != value) {
            // Make sure that the new time is within range
            ITmfTrace trace = fParentView.getTrace();
            if (trace != null) {
                TmfTimeRange range = trace.getTimeRange();
                long startTime = range.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                long endTime = range.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                if (value < startTime) {
                    value = startTime;
                } else if (value > endTime) {
                    value = endTime;
                }
            }

            // Set and propagate
            setValue(value);
            fParentView.updateCurrentEventTime(value);
        }
    }

    @Override
    public void setValue(long time) {
        super.setValue(time, new TmfTimestamp(time, ITmfTimestamp.NANOSECOND_SCALE).toString());
    }

    // ------------------------------------------------------------------------
    // Signal Handlers
    // ------------------------------------------------------------------------

    /**
     * Update the initial time value
     *
     * @param signal the time range signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void traceUpdated(final TmfTraceUpdatedSignal signal) {
        fTraceStartTime = signal.getTrace().getTimeRange().getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
    }

    // ------------------------------------------------------------------------
    // Signal Handlers
    // ------------------------------------------------------------------------

    /**
     * Format the timestamp and update the display. Compute the new text size,
     * adjust the text and group widgets and then refresh the view layout.
     *
     * @param signal the incoming signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void timestampFormatUpdated(TmfTimestampFormatUpdateSignal signal) {
        setValue(getValue());
    }

}
