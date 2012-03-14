/*******************************************************************************
 * Copyright (c) 2010, 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Philippe Sawicki (INF4990.A2010@gmail.com) - Initial API and implementation
 *   Bernd Hufmann - Changed display interface implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.views.latency.listeners;

import org.eclipse.linuxtools.internal.lttng.ui.views.latency.AbstractViewer;
import org.eclipse.linuxtools.internal.lttng.ui.views.latency.Messages;

/**
 * <b><u>TooltipListener</u></b>
 * <p>
 * Tooltip listener, displays the event count for each latency selected by the mouse click area on histogram.
 * 
 * @author Ali Jawhar
 */
public class TooltipListener extends AbstractMouseTrackListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * A reference to the observed view.
     */
    protected AbstractViewer fView;

    /**
     * A reference to the HistogramPaintListener.
     */
    protected HistogramPaintListener fHistogram;

    /**
     * Is the mouse over the warning icon, indicating that a bar is higher than the draw area due to zooming ?
     */
    protected boolean fDisplayWarning = false;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    
    /**
     * Constructor.
     * @param view
     *            A reference to the observed view.
     * @param histogramPaintListener
     *            A reference to the histogram's paintListener.
     */
    public TooltipListener(AbstractViewer view, HistogramPaintListener histogramPaintListener) {
        fView = view;
        fHistogram = histogramPaintListener;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.listeners.AbstractMouseTrackListener#display()
     */
    @Override
    protected void display() {
        displayWarningTooltip();
        displayTooltip();
    }

    // ------------------------------------------------------------------------
    // Helper Functions
    // ------------------------------------------------------------------------
    
    /**
     * Displays a tooltip if the mouse is over the warning icon indication that a bar cannot be draw entirely due to the
     * zoom factor.
     */
    protected void displayWarningTooltip() {
        if (fHistogram.barIsClipped() && fMouseX > 5 && fMouseX < 21 && fMouseY > 3 && fMouseY < 18) {
            fView.setToolTipText(Messages.LatencyView_ClippingWarning);
            fDisplayWarning = true;
        } else {
            fDisplayWarning = false;
        }
    }

    /**
     * Displays the tooltip showing the details of the histogram bar pointed by the mouse.
     */
    protected void displayTooltip() {
        if (!fDisplayWarning)
            fView.setToolTipText(fHistogram.formatToolTipLabel(fMouseX, fMouseY));
    }
}