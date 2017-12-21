/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.scatter;

import java.util.Comparator;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.IAnalysisProgressListener;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.SegmentStoreScatterDataProvider;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.Messages;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractSelectTreeViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeColumnDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfGenericTreeEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeColumnData;

import com.google.common.collect.ImmutableList;

/**
 * Abstract tree viewer to go along with the scatter chart viewer in segment
 * store scatter views
 *
 * @author Geneviève Bastien
 * @since 2.2
 */
public abstract class AbstractSegmentStoreScatterChartTreeViewer extends AbstractSelectTreeViewer {

    private @Nullable ISegmentStoreProvider fSegmentProvider = null;
    private SegmentStoreProviderProgressListener fListener = new SegmentStoreProviderProgressListener();

    private final class SegStoreScatterLabelProvider extends TreeLabelProvider {

        @Override
        public @Nullable Image getColumnImage(@Nullable Object element, int columnIndex) {
            if (columnIndex == 1 && element instanceof TmfGenericTreeEntry && isChecked(element)) {
                TmfGenericTreeEntry<TmfTreeDataModel> entry = (TmfGenericTreeEntry<TmfTreeDataModel>) element;
                if (!entry.hasChildren()) {
                    // ensures that only leaf nodes return images
                    return getLegendImage(getFullPath(entry));
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

    public AbstractSegmentStoreScatterChartTreeViewer(Composite parent) {
        super(parent, 1, SegmentStoreScatterDataProvider.ID);
        setLabelProvider(new SegStoreScatterLabelProvider());
    }

    /**
     * Returns the segment store provider
     *
     * @param trace
     *            The trace to consider
     * @return the segment store provider
     */
    protected abstract @Nullable ISegmentStoreProvider getSegmentStoreProvider(ITmfTrace trace);

    @Override
    protected @Nullable ITmfTreeDataProvider<@NonNull TmfTreeDataModel> getProvider(@NonNull ITmfTrace trace) {
        final ISegmentStoreProvider segmentStoreProvider = getSegmentStoreProvider(trace);
        if (segmentStoreProvider == null) {
            return null;
        }
        fSegmentProvider = segmentStoreProvider;
        segmentStoreProvider.addListener(fListener);
        return SegmentStoreScatterDataProvider.getOrCreate(trace, segmentStoreProvider);
    }

    @Override
    protected ITmfTreeColumnDataProvider getColumnDataProvider() {
        return () -> ImmutableList.of(
                    createColumn(Messages.AbstractSegmentStoreScatterView_Type, Comparator.comparing(TmfGenericTreeEntry::getName)),
                    new TmfTreeColumnData(Messages.AbstractSegmentStoreScatterView_Legend));
    }

}
