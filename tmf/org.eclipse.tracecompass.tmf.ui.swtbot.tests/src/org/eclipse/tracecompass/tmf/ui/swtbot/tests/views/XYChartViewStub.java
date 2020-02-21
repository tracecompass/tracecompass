/**********************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.tmf.ui.swtbot.tests.views;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.tmf.core.model.TmfXyResponseFactory;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.YModel;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfXYDataProvider;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfXyModel;
import org.eclipse.tracecompass.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractSelectTreeViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeColumnDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfGenericTreeEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeColumnData;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfFilteredXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfXYChartSettings;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Longs;

/**
 * XYChart View Stub
 *
 * @author Bernd Hufmann
 */
@SuppressWarnings("restriction")
public class XYChartViewStub extends TmfChartView {

    /** View ID. */
    public static final String ID = "org.eclipse.tracecompass.tmf.ui.swtbot.tests.views.xychart.stub"; //$NON-NLS-1$

    /** View ID. */
    @NonNull
    public static final String PROVIDER_ID = "org.eclipse.tracecompass.tmf.ui.swtbot.tests.views.xychart.stub.provider"; //$NON-NLS-1$

    /** Title of the chart viewer */
    public static final String VIEW_TITLE = "XY Chart View Stub"; //$NON-NLS-1$

    /**
     * Constructor
     */
    public XYChartViewStub() {
        super(VIEW_TITLE);
    }

    @Override
    protected TmfXYChartViewer createChartViewer(Composite parent) {
        TmfXYChartSettings settings = new TmfXYChartSettings(null, null, null, 1);
        return new TmfFilteredXYChartViewer(parent, settings, PROVIDER_ID) {
            @Override
            protected ITmfXYDataProvider initializeDataProvider(@NonNull ITmfTrace trace) {
                return new MyTmfXyDataProvider();
            }
        };
    }

    @Override
    protected @NonNull TmfViewer createLeftChildViewer(Composite parent) {
        return new MyAbsractSelectTreeViewer(parent, PROVIDER_ID);
    }

    private class MyTmfXyDataProvider implements ITmfTreeXYDataProvider<@NonNull ITmfTreeDataModel> {
        @SuppressWarnings("null")
        @Override
        public final TmfModelResponse<ITmfXyModel> fetchXY(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
            List<Long> xValues = DataProviderParameterUtils.extractTimeRequested(fetchParameters);
            if (xValues == null) {
                return TmfXyResponseFactory.createFailedResponse("No requested time values provided");
            }
            double[] yValues = new double[xValues.size()];
            for (int i = 0; i < yValues.length; i++) {
                yValues[i] = xValues.get(i) * 0.000001;
            }
            @NonNull
            IYModel series = new YModel(1, "Top", yValues);
            Map<@NonNull String, @NonNull IYModel> yModels = Collections.singletonMap(Long.toString(series.getId()), series);
            return TmfXyResponseFactory.create("Top", Objects.requireNonNull(Longs.toArray(xValues)), yModels, true);
        }

        @Override
        public final TmfModelResponse<TmfTreeModel<@NonNull ITmfTreeDataModel>> fetchTree(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
            @NonNull
            List<@NonNull ITmfTreeDataModel> list = Collections.singletonList(new TmfTreeDataModel(1, -1L, "Top"));
            TmfTreeModel<@NonNull ITmfTreeDataModel> tree = new TmfTreeModel<>(Collections.emptyList(), list);
            return new TmfModelResponse<>(tree, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }

        @Override
        public @NonNull String getId() {
            return PROVIDER_ID;
        }

        @SuppressWarnings("null")
        @Deprecated
        @Override
        public @NonNull TmfModelResponse<@NonNull ITmfXyModel> fetchXY(@NonNull TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
            @NonNull Map<@NonNull String, @NonNull Object> fetchParameters = FetchParametersUtils.timeQueryToMap(filter);
            return fetchXY(fetchParameters, monitor);
        }

        @Deprecated
        @Override
        public @NonNull TmfModelResponse<@NonNull List<@NonNull ITmfTreeDataModel>> fetchTree(@NonNull TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
            @NonNull Map<@NonNull String, @NonNull Object> fetchParameters = FetchParametersUtils.timeQueryToMap(filter);
            TmfModelResponse<TmfTreeModel<@NonNull ITmfTreeDataModel>> response = fetchTree(fetchParameters, monitor);
            TmfTreeModel<@NonNull ITmfTreeDataModel> tree = response.getModel();
            if (tree == null) {
                return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.STATE_SYSTEM_FAILED);
            }
            return new TmfModelResponse<>(tree.getEntries(), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }
    }

    private class MyAbsractSelectTreeViewer extends AbstractSelectTreeViewer {

        class MyTreeLabelProvider extends TreeLabelProvider { }

        public MyAbsractSelectTreeViewer(Composite parent, String id) {
            super(parent, 1, id);
            setLabelProvider(new MyTreeLabelProvider());
        }

        @Override
        protected ITmfTreeColumnDataProvider getColumnDataProvider() {
            return () -> ImmutableList.of(
                        createColumn("Name", Comparator.comparing(TmfGenericTreeEntry::getName)),
                        new TmfTreeColumnData("Legend"));
        }

        @Override
        protected ITmfTreeDataProvider<@NonNull ITmfTreeDataModel> getProvider(@NonNull ITmfTrace trace) {
            return new MyTmfXyDataProvider();
        }
    }

}
