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
 * This control provides a group containing a text control.
 * 
 * @version 1.0
 * @author Francois Chouinard
 */
public class HistogramTimeRangeControl extends HistogramTextControl {

    // ------------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------------
    /**
     * Constructor default values
     * @param parentView The parent histogram view.
     * @param parent The parent composite
     * @param textStyle The text style bits.
     * @param groupStyle The group style bits.
     */
    public HistogramTimeRangeControl(HistogramView parentView, Composite parent, int textStyle, int groupStyle) {
        this(parentView, parent, textStyle, groupStyle, "", HistogramUtils.nanosecondsToString(0L)); //$NON-NLS-1$
    }

    /**
     * Constructor with given group and text values.
     * 
     * @param parentView The parent histogram view.
     * @param parent The parent composite
     * @param textStyle The text style bits.
     * @param groupStyle The group style bits.
     * @param groupValue A group value
     * @param textValue A text value
     */
    public HistogramTimeRangeControl(HistogramView parentView, Composite parent, int textStyle, int groupStyle, String groupValue, String textValue) {
        super(parentView, parent, textStyle, groupStyle, groupValue, textValue);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.histogram.HistogramTextControl#updateValue()
     */
    @Override
    protected void updateValue() {
        String stringValue = fTextValue.getText();
        long value = HistogramUtils.stringToNanoseconds(stringValue);

        if (getValue() != value) {
            fParentView.updateTimeRange(value);
        }
    }

}
