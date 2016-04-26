/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.viewers;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiChartModel;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiResultTable;

/**
 * Common interface for all Lami viewers.
 *
 * @author Alexandre Montplaisir
 */
public interface ILamiViewer {

    /**
     * Dispose the viewer widget.
     */
    void dispose();

    /**
     * Factory method to create a new Table viewer.
     *
     * @param parent
     *            The parent composite
     * @param resultTable
     *            The result table to display
     * @return The new viewer
     */
    static ILamiViewer createLamiTable(Composite parent, LamiResultTable resultTable) {
        TableViewer tableViewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL);
        return new LamiTableViewer(tableViewer, resultTable);
    }

    /**
     * Factory method to create a new chart viewer. The chart type is specified
     * by the 'chartModel' parameter.
     *
     * @param parent
     *            The parent composite
     * @param resultTable
     *            The result table to use as a data source
     * @param chartModel
     *            The information about the chart to display
     * @return The new viewer
     */
    static ILamiViewer createLamiChart(Composite parent, LamiResultTable resultTable, LamiChartModel chartModel) {
        switch (chartModel.getChartType()) {
        case BAR_CHART:
            return new LamiBarChartViewer(parent, resultTable, chartModel);
        case XY_SCATTER:
            return new LamiScatterViewer(parent, resultTable, chartModel);
        case PIE_CHART:
        default:
            throw new UnsupportedOperationException("Unsupported chart type: " + chartModel.toString()); //$NON-NLS-1$
        }
    }
}
