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
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.swt.widgets.Composite;

/**
 * This control provides a group containing a text control.
 *
 * @version 2.0
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
     * @since 2.0
     */
    public HistogramTimeRangeControl(HistogramView parentView, Composite parent,
            String groupLabel, long value)
    {
        super(parentView, parent, groupLabel, value);
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
            value = TmfTimestampFormat.getDefaulIntervalFormat().parseValue(string);
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
            ITmfTimestamp ts = new TmfTimestamp(time, ITmfTimestamp.NANOSECOND_SCALE);
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
     * @since 2.0
     */
    @TmfSignalHandler
    public void intervalFormatUpdated(TmfTimestampFormatUpdateSignal signal) {
        setValue(getValue());
    }

}
