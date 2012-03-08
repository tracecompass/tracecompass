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
 *   Bernd Hufmann - Adapted to new model-view-controller design
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.latency;

import org.eclipse.linuxtools.lttng.ui.views.latency.listeners.AbstractMouseListener;
import org.eclipse.linuxtools.lttng.ui.views.latency.listeners.AbstractMouseTrackListener;
import org.eclipse.linuxtools.lttng.ui.views.latency.listeners.AbstractPaintListener;
import org.eclipse.linuxtools.lttng.ui.views.latency.listeners.ZoomListener;
import org.eclipse.linuxtools.tmf.ui.views.distribution.model.IBaseDistributionModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * <b><u>AbstractViewer</u></b>
 * <p>
 * Abstract viewer.
 * 
 * @author Philippe Sawicki
 */
public abstract class AbstractViewer extends Canvas {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Parent composite node.
     */
    protected Composite fParent;

    /**
     * Paint listener.
     */
    protected AbstractPaintListener fPaintListener;

    /**
     * Zoom listener, to zoom in and out of a graph using the scroll wheel.
     */
    protected ZoomListener fZoomListener;

    /**
     * Tool tip listener.
     */
    protected AbstractMouseTrackListener fMouseTraceListener;

    /**
     * Mouse listener
     */
    protected AbstractMouseListener fMouseListener;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    
    /**
     * Constructor.
     * @param parent
     *            The parent composite node.
     * @param style
     *            The SWT style to use to render the view.
     */
    public AbstractViewer(Composite parent, int style) {
        super(parent, style);

        fParent = parent;
    }

    // ------------------------------------------------------------------------
    // Abstract interface
    // ------------------------------------------------------------------------
    
    /**
     * Clears the view.
     */
    abstract public void clear();

    /**
     * Clears the background of the view but keeps min and max values.
     */
    abstract public void clearBackground();

    /**
     * Method to increase bar width
     */
    abstract public void increaseBarWidth();
    
    /**
     * Method to decrease bar width
     */
    abstract public void decreaseBarWidth();

    /**
     * Return data model
     */
    abstract public IBaseDistributionModel getModel();
    
    
    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns the zoom factor for the canvas.
     * @return The zoom factor for the canvas.
     */
    public int getZoomFactor() {
        if (fZoomListener != null) {
            return fZoomListener.getZoomFactor();
        } else {
            return 1;
        }
    }

    /**
     * Returns the zoom increment for the canvas.
     * @return The zoom increment for the canvas.
     */
    public int getZoomIncrement() {
        if (fZoomListener != null) {
            return fZoomListener.getZoomIncrement();
        } else {
            return 1;
        }
    }

    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    
    /**
     * Draw horizontal label each "nbTicks" ticks.
     * @param nbTicks
     *            The draw interval.
     */
    public void setDrawLabelEachNTicks(int nbTicks) {
        fPaintListener.setDrawLabelEachNTicks(nbTicks);
    }

    /**
     * Sets the title of the graph.
     * @param graphTitle
     *            The title of the graph.
     */
    public void setGraphTitle(String graphTitle) {
        fPaintListener.setGraphTitle(graphTitle);
    }

    /**
     * Sets the horizontal axis label.
     * @param xLabel
     *            The horizontal axis label.
     * @param offset
     *            The horizontal axis draw offset (in pixels).
     */
    public void setXAxisLabel(String xLabel, int offset) {
        fPaintListener.setXAxisLabel(xLabel, offset);
    }

    /**
     * Sets the vertical axis label.
     * @param yLabel
     *            The vertical axis label.
     */
    public void setYAxisLabel(String yLabel) {
        fPaintListener.setYAxisLabel(yLabel);
    }

    /**
     * Asks for the view to be redrawn, synchronously or asynchronously.
     * @param asyncRedraw
     *            If "true", the view will be redrawn asynchronously, otherwise it will be redraw synchronously.
     */
    public void askForRedraw(boolean asyncRedraw) {
        if (asyncRedraw == true) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    try {
                        redraw();
                    } catch (SWTException e) {
                        // ...
                    }
                }
            });
        } else {
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    try {
                        redraw();
                    } catch (SWTException e) {
                        // ...
                    }
                }
            });
        }
    }

    /**
     * Asks for the view to be redrawn (asynchronously).
     */
    public void askForRedraw() {
        askForRedraw(true);
    }

    /**
     * Redraws the title after a zoom to display the new zoom factor.
     */
    public void redrawTitle() {
        fPaintListener.paintGraphTitle();
    }

    /**
     * Removes the view's listeners before disposing of it.
     */
    @Override
    public void dispose() {
        try {
            if (fPaintListener != null) {
                removePaintListener(fPaintListener);
                fPaintListener = null;
            }
            if (fZoomListener != null) {
                removeListener(SWT.MouseWheel, fZoomListener);
                fZoomListener = null;
            }
            if (fMouseTraceListener != null) {
                removeListener(SWT.MouseMove, fMouseTraceListener);
                fMouseTraceListener = null;
            }
        } catch (SWTException e) {
            // This exception will be thrown if the user closes the view
            // while it is receiving data from the Analyzer.

            // ...
        }

        super.dispose();
    }
}