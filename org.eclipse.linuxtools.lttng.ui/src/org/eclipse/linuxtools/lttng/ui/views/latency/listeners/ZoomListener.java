/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Philippe Sawicki (INF4990.A2010@gmail.com)   - Initial API and implementation
 *   Mathieu Denis    (mathieu.denis55@gmail.com) - Refactored code
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.latency.listeners;

import org.eclipse.linuxtools.lttng.ui.views.latency.AbstractViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * <b><u>ZoomListener</u></b>
 * <p>
 * 
 * Canvas zoom listener.
 * 
 * @author Philippe Sawicki
 */
public class ZoomListener implements Listener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * A reference to the observed view.
     */
    protected AbstractViewer fView;
    /**
     * Default zoom factor.
     */
    protected int fZoomFactor;
    /**
     * Zoom increment.
     */
    protected int fZoomIncrement = 30;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     * @param view
     *            A reference to the observed view.
     * @param defaultZoomFactor
     *            Default zoom factor.
     */
    public ZoomListener(AbstractViewer view, int defaultZoomFactor) {
        fView = view;
        fZoomFactor = defaultZoomFactor;
    }

    /**
     * Constructor.
     * @param view
     *            A reference to the observed view.
     */
    public ZoomListener(AbstractViewer view) {
        this(view, 1);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns the zoom factor.
     * @return The zoom factor.
     */
    public int getZoomFactor() {
        if (fZoomFactor < 1)
            return 1;
        else
            return fZoomFactor;
    }

    /**
     * Returns the zoom increment.
     * @return The zoom increment.
     */
    public int getZoomIncrement() {
        return fZoomIncrement;
    }
    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void handleEvent(Event event) {
        switch (event.type) {
            case SWT.MouseWheel:
                boolean scrollDown = (event.count == 0 ? false : (event.count > 0 ? false : true));
                int zoomStep = fZoomIncrement;
                if (scrollDown)
                    zoomStep = -fZoomIncrement;
                fZoomFactor = Math.max(0, fZoomFactor + zoomStep);

                Canvas canvas = (Canvas) event.widget;
                if (fView != null) {
                    // clear the background to allow redraw of values of the vertical axis.
                    fView.clearBackground();
                    fView.redrawTitle();
                    fView.askForRedraw();
                }
                canvas.redraw();
                break;
        }
    }

    /**
     * Sets the zoom increment.
     * @param zoomIncrement
     *            The new zoom increment.
     */
    public void setZoomIncrement(int zoomIncrement) {
        fZoomIncrement = zoomIncrement;
    }
}