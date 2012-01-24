/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.latency.listeners;

import org.eclipse.linuxtools.lttng.ui.views.latency.GraphViewer;
import org.eclipse.linuxtools.lttng.ui.views.latency.model.LatencyGraphModel;

/**
 * <b><u>GraphMouseListener</u></b>
 * <p>
 */
public class GraphMouseListener extends AbstractMouseListener {
    
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * A reference to the observed view.
     */
    protected GraphViewer fView;

    /**
     * A reference to the HistogramPaintListener.
     */
    protected GraphPaintListener fGraph;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    
    /**
     * Constructor.
     * @param view
     *            A reference to the observed view.
     * @param histogramPaintListener
     *            A reference to the histogram's paintListener.
     */
    public GraphMouseListener(GraphViewer view, GraphPaintListener graphPaintListener) {
        fView = view;
        fGraph = graphPaintListener;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.listeners.AbstractMouseListener#display()
     */
    @Override
    protected void display() {
        long currentTime = fGraph.getCurrentTimeFromHorizontalValue(fMouseX);
        ((LatencyGraphModel)fView.getModel()).setCurrentEventNotifyListeners(currentTime);
    }
}
