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
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Moved from LTTng to TMF
 *   Francois Chouinard - Simplified constructor, handle interval format change
 *   Patrick Tasse - Update value handling
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.histogram;

import java.text.ParseException;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTimestampFormatUpdateSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * This control provides a group containing a text control.
 *
 * @author Francois Chouinard
 */
public class HistogramCurrentTimeControl extends HistogramTextControl {

    // ------------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------------

    /**
     * Standard constructor
     *
     * @param parentView A parent histogram view
     * @param parent A parent composite to draw in
     * @param label A label
     * @param value A value
     */
    public HistogramCurrentTimeControl(HistogramView parentView, Composite parent,
            String label, long value)
    {
        super(parentView, parent, label, value);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected void updateValue() {
        if (getValue() == Long.MIN_VALUE) {
            fTextValue.setText(""); //$NON-NLS-1$
            return;
        }
        String string = fTextValue.getText();
        long value = getValue();
        try {
            value = TmfTimestampFormat.getDefaulTimeFormat().parseValue(string, getValue());
        } catch (ParseException e) {
        }
        if (getValue() != value) {
            // Make sure that the new time is within range
            ITmfTrace trace = fParentView.getTrace();
            if (trace != null) {
                TmfTimeRange range = trace.getTimeRange();
                long startTime = range.getStartTime().toNanos();
                long endTime = range.getEndTime().toNanos();
                if (value < startTime) {
                    value = startTime;
                } else if (value > endTime) {
                    value = endTime;
                }
            }

            // Set and propagate
            setValue(value);
            updateSelectionTime(value);
        } else {
            setValue(value);
        }
    }

    /**
     * Update the selection time
     *
     * @param time
     *            the new selected time
     */
    protected void updateSelectionTime(long time) {
        fParentView.updateSelectionTime(time, time);
    }

    @Override
    public void setValue(long time) {
        if (time != Long.MIN_VALUE) {
            super.setValue(time, TmfTimestamp.fromNanos(time).toString());
        } else {
            super.setValue(time, ""); //$NON-NLS-1$
        }
    }

    // ------------------------------------------------------------------------
    // Signal Handlers
    // ------------------------------------------------------------------------

    /**
     * Format the timestamp and update the display. Compute the new text size,
     * adjust the text and group widgets and then refresh the view layout.
     *
     * @param signal the incoming signal
     */
    @TmfSignalHandler
    public void timestampFormatUpdated(TmfTimestampFormatUpdateSignal signal) {
        setValue(getValue());
    }

}
