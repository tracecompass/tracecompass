/*******************************************************************************
 * Copyright (c) 2010, 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Philippe Sawicki (INF4990.A2010@gmail.com)   - Initial API and implementation
 *   Mathieu Denis    (mathieu.denis55@gmail.com) - Refactored code
 *   Bernd Hufmann - Changed display interface implementation 
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.latency.listeners;

import org.eclipse.linuxtools.lttng.ui.views.latency.AbstractViewer;

/**
 * <b><u>TimePointerListener</u></b>
 * <p>
 * Displays a tooltip showing the approximate values of the point under the mouse cursor.
 * 
 * @author Philippe Sawicki
 */
public class TimePointerListener extends AbstractMouseTrackListener {

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
    protected GraphPaintListener fGraph;

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
    public TimePointerListener(AbstractViewer view, GraphPaintListener graphPaintListener) {
        fView = view;
        fGraph = graphPaintListener;
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
        fView.setToolTipText(fGraph.formatToolTipLabel(fMouseX, fMouseY));
    }
}