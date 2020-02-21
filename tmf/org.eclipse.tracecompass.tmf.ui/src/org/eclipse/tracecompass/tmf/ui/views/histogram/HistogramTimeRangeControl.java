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
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;

/**
 * This control provides a group containing a text control.
 *
 * @author Francois Chouinard
 */
public class HistogramTimeRangeControl extends HistogramTextControl {

    // ------------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------------

    /**
     * Constructor with given group and text values.
     *
     * @param parentView The parent histogram view.
     * @param parent The parent composite
     * @param groupLabel A group value
     * @param value A text value
     */
    public HistogramTimeRangeControl(HistogramView parentView, Composite parent,
            String groupLabel, long value)
    {
        super(parentView, parent, groupLabel, value);
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
            value = TmfTimestampFormat.getDefaulIntervalFormat().parseValue(string);
            if (value < 1) {
                value = getValue();
            }
        } catch (ParseException e) {
        }
        if (getValue() != value) {
            fParentView.updateTimeRange(value);
        } else {
            setValue(value);
        }
    }

    @Override
    public void setValue(long time) {
        if (time != Long.MIN_VALUE) {
            ITmfTimestamp ts = TmfTimestamp.fromNanos(time);
            super.setValue(time, ts.toString(TmfTimestampFormat.getDefaulIntervalFormat()));
        } else {
            super.setValue(time, ""); //$NON-NLS-1$
        }
    }

    // ------------------------------------------------------------------------
    // Signal Handlers
    // ------------------------------------------------------------------------

    /**
     * Format the interval and update the display. Compute the new text size,
     * adjust the text and group widgets and then refresh the view layout.
     *
     * @param signal the incoming signal
     */
    @TmfSignalHandler
    public void intervalFormatUpdated(TmfTimestampFormatUpdateSignal signal) {
        setValue(getValue());
    }

}
