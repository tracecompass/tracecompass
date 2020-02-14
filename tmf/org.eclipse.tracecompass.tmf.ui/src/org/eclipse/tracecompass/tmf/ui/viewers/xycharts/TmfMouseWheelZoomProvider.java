/*******************************************************************************
 * Copyright (c) 2013, 2019 Ericsson
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
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.xycharts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.xycharts.TmfXyUiUtils;
import org.swtchart.Chart;

/**
 * Class for providing zooming and scrolling based on mouse wheel. For zooming,
 * it centers the zoom on mouse position. For scrolling, it will move the zoom
 * window to another position while maintaining the window size. It also
 * notifies the viewer about a change of range.
 *
 * @author Bernd Hufmann
 * @deprecated use {@link org.eclipse.tracecompass.tmf.ui.viewers.xychart.TmfMouseWheelZoomProvider}
 */
@Deprecated
public class TmfMouseWheelZoomProvider extends TmfBaseProvider implements MouseWheelListener {

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Standard constructor.
     *
     * @param tmfChartViewer
     *            The parent histogram object
     */
    public TmfMouseWheelZoomProvider(ITmfChartTimeProvider tmfChartViewer) {
        super(tmfChartViewer);
        register();
    }

    // ------------------------------------------------------------------------
    // TmfBaseProvider
    // ------------------------------------------------------------------------
    @Override
    public void register() {
        getChart().getPlotArea().addMouseWheelListener(this);
    }

    @Override
    public void deregister() {
        if ((getChartViewer().getControl() != null) && !getChartViewer().getControl().isDisposed()) {
            getChart().getPlotArea().removeMouseWheelListener(this);
        }
    }

    @Override
    public void refresh() {
        // nothing to do
    }

    // ------------------------------------------------------------------------
    // MouseWheelListener
    // ------------------------------------------------------------------------
    @Override
    public synchronized void mouseScrolled(MouseEvent event) {
        final int count = event.count;
        if ((event.stateMask & SWT.CTRL) != 0) {
            final int x = event.x;
            zoom(count, x);
        } else if ((event.stateMask & SWT.SHIFT) != 0) {
            scroll(count);
        }
    }

    private void scroll(final int count) {
        ITmfChartTimeProvider viewer = getChartViewer();
        Chart chart = getChart();
        if ((viewer != null) && (chart != null) && (count != 0)) {
            TmfXyUiUtils.horizontalScroll(viewer, chart, count > 0);
        }
    }

    private void zoom(final int count, final int x) {
        ITmfChartTimeProvider viewer = getChartViewer();
        Chart chart = getChart();
        if ((viewer != null) && (chart != null) && (count != 0)) {
            TmfXyUiUtils.zoom(viewer, chart, count > 0, x);
        }
    }
}