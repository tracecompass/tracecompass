/**********************************************************************
 * Copyright (c) 2014, 2017 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Geneviève Bastien - Create and use base class for XY plots
 **********************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.ui.views.memusage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.common.core.format.DataSizeWithUnitFormat;
import org.eclipse.tracecompass.lttng2.ust.core.analysis.memory.UstMemoryUsageDataProvider;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXAxisChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfXYChartSettings;
import org.swtchart.Chart;

/**
 * Memory usage view
 *
 * @author Matthew Khouzam
 */
public class MemoryUsageViewer extends TmfCommonXAxisChartViewer {

    /**
     * Constructor
     *
     * @param parent
     *            parent view
     * @param settings
     *            See {@link TmfXYChartSettings} to know what it contains
     */
    public MemoryUsageViewer(Composite parent, TmfXYChartSettings settings) {
        super(parent, settings);
        Chart chart = getSwtChart();
        chart.getLegend().setPosition(SWT.LEFT);
        chart.getAxisSet().getYAxis(0).getTick().setFormat(DataSizeWithUnitFormat.getInstance());
    }

    @Override
    protected void initializeDataProvider() {
        ITmfTrace trace = getTrace();
        UstMemoryUsageDataProvider provider = DataProviderManager.getInstance().getDataProvider(trace,
                UstMemoryUsageDataProvider.ID, UstMemoryUsageDataProvider.class);
        setDataProvider(provider);
    }
}
