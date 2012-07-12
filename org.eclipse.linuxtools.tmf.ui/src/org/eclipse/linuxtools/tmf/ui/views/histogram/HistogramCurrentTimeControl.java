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

import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.swt.widgets.Composite;

/**
 * This control provides a group containing a text control.
 * 
 * @version 1.0
 * @author Francois Chouinard
 */
public class HistogramCurrentTimeControl extends HistogramTextControl {

    // ------------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------------

    /**
     * Constructor with default group and text value.
     * 
     * @param parentView A parent histogram view
     * @param parent A parent composite to draw in
     * @param textStyle A test style 
     * @param groupStyle A group style
     */
    public HistogramCurrentTimeControl(HistogramView parentView, Composite parent, int textStyle, int groupStyle) {
        this(parentView, parent, textStyle, groupStyle, "", HistogramUtils.nanosecondsToString(0L)); //$NON-NLS-1$
    }

    /**
     * 
     * Constructor
     * @param parentView A parent histogram view
     * @param parent A parent composite to draw in
     * @param textStyle A test style 
     * @param groupStyle A group style
     * @param groupValue A group value
     * @param textValue A text value
     */
    public HistogramCurrentTimeControl(HistogramView parentView, Composite parent, int textStyle, int groupStyle, String groupValue, String textValue) {
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
            // Make sure that the new time is within range
            TmfExperiment<?> exp = TmfExperiment.getCurrentExperiment();
            if (exp != null) {
                TmfTimeRange range = exp.getTimeRange();
                long startTime = range.getStartTime().getValue();
                long endTime = range.getEndTime().getValue();
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

}
