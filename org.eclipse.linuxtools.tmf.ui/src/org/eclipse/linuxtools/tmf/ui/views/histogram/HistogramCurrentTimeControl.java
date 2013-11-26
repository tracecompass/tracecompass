/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
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
 *   Patrick Tasse - Update value handling
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.histogram;

import java.text.ParseException;

import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimestampFormatUpdateSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestampFormat;
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
    // Construction
    // ------------------------------------------------------------------------

    /**
     * Standard constructor
     *
     * @param parentView A parent histogram view
     * @param parent A parent composite to draw in
     * @param label A label
     * @param value A value
     * @since 2.0
     */
    public HistogramCurrentTimeControl(HistogramView parentView, Composite parent,
            String label, long value)
    {
        super(parentView, parent, label, value);
        TmfSignalManager.register(this);
    }

    @Override
    public void dispose() {
        TmfSignalManager.deregister(this);
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
     * @since 2.2
     */
    protected void updateSelectionTime(long time) {
        fParentView.updateSelectionTime(time, time);
    }

    @Override
    public void setValue(long time) {
        if (time != Long.MIN_VALUE) {
            super.setValue(time, new TmfTimestamp(time, ITmfTimestamp.NANOSECOND_SCALE).toString());
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
     * @since 2.0
     */
    @TmfSignalHandler
    public void timestampFormatUpdated(TmfTimestampFormatUpdateSignal signal) {
        setValue(getValue());
    }

}
