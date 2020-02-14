/*******************************************************************************
 * Copyright (c) 2017, 2020 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.scatter;

import java.util.Comparator;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.IAnalysisProgressListener;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.SegmentStoreScatterDataProvider;
import org.eclipse.tracecompass.internal.tmf.ui.commands.Messages;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractSelectTreeViewer2;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeColumnDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfGenericTreeEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeColumnData;

import com.google.common.collect.ImmutableList;

/**
 * Abstract tree viewer to go along with the scatter chart viewer in segment
 * store scatter views
 *
 * @author Geneviève Bastien
 * @since 4.1
 */
public class AbstractSegmentStoreScatterChartTreeViewer2 extends AbstractSelectTreeViewer2 {

    private final String fAnalysisId;
    private @Nullable ISegmentStoreProvider fSegmentProvider = null;
    private SegmentStoreProviderProgressListener fListener = new SegmentStoreProviderProgressListener();

    private final class SegStoreScatterLabelProvider extends TreeLabelProvider {

        @Override
        public @Nullable Image getColumnImage(@Nullable Object element, int columnIndex) {
            if (columnIndex == 1 && element instanceof TmfGenericTreeEntry && isChecked(element)) {
                TmfGenericTreeEntry<TmfTreeDataModel> entry = (TmfGenericTreeEntry<TmfTreeDataModel>) element;
                if (!entry.hasChildren()) {
                    // ensures that only leaf nodes return images
                    return getLegendImage(entry.getModel().getId());
                }
            }
            return null;
        }
    }

    /**
     * Listener to update the model with the segment store provider results once
     * its store is fully completed
     */
    private final class SegmentStoreProviderProgressListener implements IAnalysisProgressListener {
        @Override
        public void onComplete(ISegmentStoreProvider activeProvider, ISegmentStore<ISegment> data) {
            /*
             * Check if the active trace was changed while the provider was building its
             * segment store
             */
            if (activeProvider.equals(fSegmentProvider)) {
                updateContent(getWindowStartTime(), getWindowEndTime(), false);
            }
        }
    }

    /**
     * Constructor
     *
     * @param parent
     *            The parent composite
     * @param analysisId
     *            The ID of the analysis to show in this viewer
     */
    public AbstractSegmentStoreScatterChartTreeViewer2(Composite parent, String analysisId) {
        super(parent, 1, SegmentStoreScatterDataProvider.ID);
        fAnalysisId = analysisId;
        setLabelProvider(new SegStoreScatterLabelProvider());
    }

    /**
     * Get the analysis ID used to retrieve the scatter data provider. Extenders may
     * override this method to return another analysis ID than the one in the
     * constructor
     *
     * @return The analysis ID
     */
    protected String getAnalysisId() {
        return fAnalysisId;
    }

    @Override
    protected @Nullable ITmfTreeDataProvider<@NonNull ITmfTreeDataModel> getProvider(@NonNull ITmfTrace trace) {
        String analysisId = getAnalysisId();
        /* Support legacy code, get the analysis ID of the segment store */
        if (analysisId.isEmpty()) {
            return null;
        }
        /* End support of legacy */
        // TODO: Find another mechanism to update the view rather than listeners, so
        // that we don't need to expose the analysis to the view
        IAnalysisModule module = TmfTraceUtils.getAnalysisModuleOfClass(trace, IAnalysisModule.class, analysisId);
        if (!(module instanceof ISegmentStoreProvider)) {
            return null;
        }
        fSegmentProvider = (ISegmentStoreProvider) module;
        ((ISegmentStoreProvider) module).addListener(fListener);
        return DataProviderManager.getInstance().getDataProvider(trace, SegmentStoreScatterDataProvider.ID + ':' + analysisId, ITmfTreeXYDataProvider.class);
    }

    @Override
    protected ITmfTreeColumnDataProvider getColumnDataProvider() {
        return () -> ImmutableList.of(
                    createColumn(Messages.AbstractSegmentStoreScatterView_Type, Comparator.comparing(TmfGenericTreeEntry::getName)),
                    new TmfTreeColumnData(Messages.AbstractSegmentStoreScatterView_Legend));
    }

}
