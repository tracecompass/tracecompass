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

import org.eclipse.tracecompass.internal.tmf.ui.viewers.xychart.TmfXyUiUtils;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.IAxis;

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
    protected Chart getChart() {
        return (Chart) fChartViewer.getControl();
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

        IAxis xAxis = getChart().getAxisSet().getXAxis(0);
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
    protected abstract void register();

    /**
     * Method to deregister the provider from chart viewer.
     */
    protected abstract void deregister();

}