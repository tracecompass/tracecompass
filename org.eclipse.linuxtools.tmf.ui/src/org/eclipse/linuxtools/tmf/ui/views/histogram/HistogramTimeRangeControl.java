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
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.histogram;

import org.eclipse.swt.widgets.Composite;

/**
 * <b><u>HistogramTimeRangeControl</u></b>
 * <p>
 * This control provides a group containing a text control.
 */
public class HistogramTimeRangeControl extends HistogramTextControl {

    // ------------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------------

    public HistogramTimeRangeControl(HistogramView parentView, Composite parent, int textStyle, int groupStyle) {
        this(parentView, parent, textStyle, groupStyle, "", HistogramUtils.nanosecondsToString(0L)); //$NON-NLS-1$
    }

    public HistogramTimeRangeControl(HistogramView parentView, Composite parent, int textStyle, int groupStyle, String groupValue, String textValue) {
        super(parentView, parent, textStyle, groupStyle, groupValue, textValue);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected void updateValue() {
        String stringValue = fTextValue.getText();
        long value = HistogramUtils.stringToNanoseconds(stringValue);

        if (getValue() != value) {
            fParentView.updateTimeRange(value);
        }
    }

}
