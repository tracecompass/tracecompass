/**********************************************************************
 * Copyright (c) 2015, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.density2;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.ICustomPaintListener;
import org.eclipse.swtchart.IPlotArea;
import org.eclipse.swtchart.ISeries;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density2.AbstractSegmentStoreDensityViewer;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.xychart.XYAxis;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.xychart.XYSeries;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfAbstractToolTipHandler;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.IAxis;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.IXYSeries;

/**
 * Base class for any mouse provider such as tool tip, zoom and selection providers.
 *
 * @author Bernd Hufmann
 * @author Marc-Andre Laperle
 */
public abstract class BaseMouseProvider {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------


    private AbstractSegmentStoreDensityViewer fDensityViewer;
    private IAxis fXaxis;

    /**
     * Standard constructor.
     *
     * @param densityViewer
     *            The parent density viewer
     */
    public BaseMouseProvider(AbstractSegmentStoreDensityViewer densityViewer) {
        fDensityViewer = densityViewer;
        fXaxis = XYAxis.create(densityViewer.getControl().getAxisSet().getXAxis(0));
        register();
    }


    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns the SWT chart reference
     *
     * @return SWT chart reference.
     */
    protected Chart getChart() {
        return fDensityViewer.getControl();
    }

    /**
     * @return the density viewer
     */
    public AbstractSegmentStoreDensityViewer getDensityViewer() {
        return fDensityViewer;
    }

    /**
     * Gets the series
     *
     * @return the series
     */
    public List<@NonNull IXYSeries> getSeries() {
        List<@NonNull IXYSeries> retVal = new ArrayList<>();
        for (ISeries<?> series : getChart().getSeriesSet().getSeries()) {
            XYSeries xySeries = XYSeries.create(series);
            if (xySeries != null) {
                retVal.add(xySeries);
            }
        }
        return retVal;
    }

    /**
     * Get the X axis
     * @return the X axis
     */
    public IAxis getXAxis() {
        return fXaxis;
    }

    /**
     * Causes the window to be redrawn by invoking a paint request.
     */
    public void redraw() {
        getChart().redraw();
    }

    /**
     * Sets the basic tooltip text it is recommended to use
     * {@link TmfAbstractToolTipHandler} instead
     *
     * @param tooltip
     *            the tooltip string
     */
    public void setToolTipText(String tooltip) {
        getChart().getPlotArea().setToolTipText(tooltip);
    }

    /**
     * Get a tooltip handler, if available
     *
     * @return the tooltip handler
     */
    public TmfAbstractToolTipHandler getTooltipHandler() {
        return null;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Method deregisters provider from chart viewer. Subclasses may override this method
     * to dispose any resources.
     */
    public void dispose() {
        deregister();
    }

    /**
     * Method to register the provider to chart viewer.
     */
    protected void register() {
        IPlotArea plotArea = getChart().getPlotArea();
        Control control = plotArea.getControl();
        if (this instanceof MouseListener) {
            control.addMouseListener((MouseListener) this);
        }
        if (this instanceof MouseMoveListener) {
            control.addMouseMoveListener((MouseMoveListener) this);
        }
        if (this instanceof MouseWheelListener) {
            control.addMouseWheelListener((MouseWheelListener) this);
        }
        if (this instanceof MouseTrackListener) {
            control.addMouseTrackListener((MouseTrackListener) this);
        }
        if (this instanceof PaintListener) {
            control.addPaintListener((PaintListener) this);
        }
        if (this instanceof ICustomPaintListener) {
            plotArea.addCustomPaintListener((ICustomPaintListener) this);
        }
        TmfAbstractToolTipHandler tooltipHandler = getTooltipHandler();
        if(tooltipHandler != null) {
            tooltipHandler.activateHoverHelp(control);
        }
    }

    /**
     * Method to deregister the provider from chart viewer.
     */
    protected void deregister() {
        IPlotArea plotArea = getChart().getPlotArea();
        if (plotArea == null) {
            return;
        }
        Control control = plotArea.getControl();
        if (!control.isDisposed()) {
            if (this instanceof MouseListener) {
                control.removeMouseListener((MouseListener) this);
            }
            if (this instanceof MouseMoveListener) {
                control.removeMouseMoveListener((MouseMoveListener) this);
            }
            if (this instanceof MouseWheelListener) {
                control.removeMouseWheelListener((MouseWheelListener) this);
            }
            if (this instanceof MouseTrackListener) {
                control.removeMouseTrackListener((MouseTrackListener) this);
            }
            if (this instanceof PaintListener) {
                control.removePaintListener((PaintListener) this);
            }
            if (this instanceof ICustomPaintListener) {
                plotArea.removeCustomPaintListener((ICustomPaintListener) this);
            }
            TmfAbstractToolTipHandler tooltipHandler = getTooltipHandler();
            if(tooltipHandler != null) {
                tooltipHandler.deactivateHoverHelp(control);
            }
        }
    }

}