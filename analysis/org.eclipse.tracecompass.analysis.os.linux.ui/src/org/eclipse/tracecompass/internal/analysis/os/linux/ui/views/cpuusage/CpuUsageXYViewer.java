/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.cpuusage;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.cpuusage.CpuUsageDataProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectedCpuQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.presentation.IYAppearance;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfFilteredXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfXYChartSettings;
import org.swtchart.ITitle;

import com.google.common.base.Joiner;

/**
 * CPU usage viewer with XY line chart. It displays the total CPU usage and that
 * of the threads selected in the CPU usage tree viewer.
 *
 * @author Geneviève Bastien
 */
public class CpuUsageXYViewer extends TmfFilteredXYChartViewer {

    private static final int DEFAULT_SERIES_WIDTH = 1;

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

    @Deprecated
    @Override
    protected TimeQueryFilter createQueryFilter(long start, long end, int nb) {
        return new SelectedCpuQueryFilter(start, end, nb, getSelected(), CpuUsageView.getCpus(getTrace()));
    }

    @Override
    protected @NonNull Map<String, Object> createQueryParameters(long start, long end, int nb) {
        Map<@NonNull String, @NonNull Object> parameters = FetchParametersUtils.selectionTimeQueryToMap(new SelectionTimeQueryFilter(start, end, nb, getSelected()));
        parameters.put(CpuUsageDataProvider.CPUS_PARAMETER_KEY, CpuUsageView.getCpus(getTrace()));
        return parameters;
    }

    @Override
    public IYAppearance getSeriesAppearance(@NonNull String seriesName) {
        if (seriesName.startsWith(CpuUsageDataProvider.TOTAL)) {
            return getPresentationProvider().getAppearance(seriesName, IYAppearance.Type.LINE, DEFAULT_SERIES_WIDTH);
        }
        return getPresentationProvider().getAppearance(seriesName, IYAppearance.Type.AREA, DEFAULT_SERIES_WIDTH);
    }

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
}
