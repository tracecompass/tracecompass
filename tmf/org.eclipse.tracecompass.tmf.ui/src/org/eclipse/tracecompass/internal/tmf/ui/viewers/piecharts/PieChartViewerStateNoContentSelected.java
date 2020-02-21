/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexis Cabana-Loriaux - Initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.viewers.piecharts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

/**
 * Implementation of the IPieChartViewerState interface to represent the state
 * of the layout when there is no content currently selected.
 *
 * @author Alexis Cabana-Loriaux
 * @since 2.0
 *
 */
public class PieChartViewerStateNoContentSelected implements IPieChartViewerState {

    /**
     * Default constructor
     *
     * @param context
     *            The current context
     */
    public PieChartViewerStateNoContentSelected(final TmfPieChartViewer context) {
        if (context.isDisposed()) {
            return;
        }

        Display.getDefault().asyncExec(() -> {
            synchronized (context) {
                if (!context.isDisposed()) {
                    // Have to get rid of the time-range PieChart
                    if (context.getTimeRangePC() != null) {
                        if (!context.getTimeRangePC().isDisposed()) {
                            context.getTimeRangePC().dispose();
                        }
                        context.setTimeRangePC(null);
                    }

                    context.updateGlobalPieChart();
                    // update the global chart so it takes all the place
                    context.getGlobalPC().getLegend().setPosition(SWT.RIGHT);
                    context.layout();
                }
            }
        });
    }

    @Override
    public void newSelection(final TmfPieChartViewer context) {
        context.setCurrentState(new PieChartViewerStateContentSelected(context));
    }

    @Override
    public void newEmptySelection(final TmfPieChartViewer context) {
        // do nothing
    }

    @Override
    public void newGlobalEntries(final TmfPieChartViewer context) {
        Display.getDefault().asyncExec(() -> {
            synchronized (context) {
                if (!context.isDisposed()) {
                    context.updateGlobalPieChart();
                    context.getGlobalPC().redraw();
                }
            }
        });
    }
}
