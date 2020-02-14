/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.cpuusage;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swtchart.ITitle;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.cpuusage.CpuUsageDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.ui.widgets.timegraph.BaseXYPresentationProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.linechart.TmfFilteredXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.linechart.TmfXYChartSettings;

import com.google.common.base.Joiner;

/**
 * CPU usage viewer with XY line chart. It displays the total CPU usage and that
 * of the threads selected in the CPU usage tree viewer.
 *
 * @author Geneviève Bastien
 */
public class CpuUsageXYViewer extends TmfFilteredXYChartViewer {

    /**
     * Constructor
     *
     * @param parent
     *            parent composite
     * @param settings
     *            See {@link TmfXYChartSettings} to know what it contains
     */
    public CpuUsageXYViewer(Composite parent, TmfXYChartSettings settings) {
        super(parent, settings, CpuUsageDataProvider.ID);
        getSwtChart().getTitle().setVisible(true);
        getSwtChart().getLegend().setVisible(false);
    }

    @Override
    protected @NonNull Map<String, Object> createQueryParameters(long start, long end, int nb) {
        Map<@NonNull String, @NonNull Object> parameters = FetchParametersUtils.selectionTimeQueryToMap(new SelectionTimeQueryFilter(start, end, nb, getSelected()));
        parameters.put(CpuUsageDataProvider.REQUESTED_CPUS_KEY, CpuUsageView.getCpus(getTrace()));
        return parameters;
    }

    @Override
    public OutputElementStyle getSeriesStyle(@NonNull Long seriesId) {
        return getPresentationProvider().getSeriesStyle(seriesId);    }

    /**
     * Update the {@link CpuUsageXYViewer} title to append the current cpu numbers
     */
    protected void setTitle() {
        ITitle title = getSwtChart().getTitle();
        Set<Integer> cpus = CpuUsageView.getCpus(getTrace());
        if (cpus.isEmpty()) {
            title.setText(Messages.CpuUsageView_Title);
        } else {
            title.setText(Messages.CpuUsageView_Title + ' ' + Joiner.on(", ").join(cpus)); //$NON-NLS-1$
        }
    }

    @Override
    protected BaseXYPresentationProvider createPresentationProvider(ITmfTrace trace) {
        CPUUsagePresentationProvider presProvider = CPUUsagePresentationProvider.getForTrace(trace);
        return presProvider;
    }
}
