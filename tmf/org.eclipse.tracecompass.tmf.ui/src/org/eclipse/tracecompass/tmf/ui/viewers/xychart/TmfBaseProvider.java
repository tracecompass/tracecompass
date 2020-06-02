/**********************************************************************
 * Copyright (c) 2013, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.tmf.ui.viewers.xychart;

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
import org.eclipse.tracecompass.internal.tmf.ui.viewers.xychart.TmfXyUiUtils;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.xychart.XYAxis;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.xychart.XYSeries;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfAbstractToolTipHandler;

/**
 * Base class for any provider such as tool tip, zoom and selection providers.
 *
 * @author Bernd Hufmann
 * @since 6.0
 */
public abstract class TmfBaseProvider {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** Reference to the chart viewer */
    private final ITmfChartTimeProvider fChartViewer;
    private final IAxis fXaxis;
    private final IAxis fYaxis;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Standard constructor.
     *
     * @param tmfChartViewer
     *            The parent histogram object
     */
    public TmfBaseProvider(ITmfChartTimeProvider tmfChartViewer) {
        fChartViewer = tmfChartViewer;
        fXaxis = XYAxis.create(getChart().getAxisSet().getXAxis(0));
        fYaxis = XYAxis.create(getChart().getAxisSet().getYAxis(0));
        register();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * Returns the chart viewer reference.
     * @return the chart viewer reference
     */
    public ITmfChartTimeProvider getChartViewer() {
        return fChartViewer;
    }

    /**
     * Returns the SWT chart reference
     *
     * @return SWT chart reference.
     */
    private Chart getChart() {
        return (Chart) fChartViewer.getControl();
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
     * Get the Y Axis
     * @return the Y axis
     */
    public IAxis getYAxis() {
        return fYaxis;
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
     * Limits x data coordinate to window start and window end range
     *
     * @param x
     *          x to limit
     * @return  x if x >= begin && x <= end
     *          begin if x < begin
     *          end if x > end
     */
    protected long limitXDataCoordinate(double x) {
        return TmfXyUiUtils.limitXDataCoordinate(getChartViewer(), x);
    }

    /**
     * Limits x pixel coordinate to window start and window end range
     *
     * @param axisIndex
     *          index of x-axis
     * @param x
     *          x to limit
     * @return  x if x >= begin && x <= end
     *          begin if x < begin
     *          end if x > end
     */
    protected int limitXPixelCoordinate(int axisIndex, int x) {
        ITmfChartTimeProvider viewer = getChartViewer();
        long windowStartTime = viewer.getWindowStartTime() - viewer.getTimeOffset();
        long windowEndTime = viewer.getWindowEndTime() - viewer.getTimeOffset();

        IAxis xAxis = getXAxis();
        int startX = xAxis.getPixelCoordinate(windowStartTime);
        if (x < startX) {
            return startX;
        }

        int endX = xAxis.getPixelCoordinate(windowEndTime);
        if (x > endX) {
            return endX;
        }

        return x;
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
     * Method to refresh the viewer. It will redraw the viewer.
     */
    public void refresh() {
        if (!TmfXYChartViewer.getDisplay().isDisposed()) {
            TmfXYChartViewer.getDisplay().asyncExec(() -> {
                if (!getChart().isDisposed()) {
                    getChart().redraw();
                }
            });
        }
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
        if (this instanceof ICustomPaintListener) {
            plotArea.addCustomPaintListener((ICustomPaintListener) this);
        } else if (this instanceof PaintListener) {
            control.addPaintListener((PaintListener) this);
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
            if (this instanceof ICustomPaintListener) {
                plotArea.removeCustomPaintListener((ICustomPaintListener) this);
            } else if (this instanceof PaintListener) {
                control.removePaintListener((PaintListener) this);
            }

            TmfAbstractToolTipHandler tooltipHandler = getTooltipHandler();
            if(tooltipHandler != null) {
                tooltipHandler.deactivateHoverHelp(control);
            }
        }
    }

}