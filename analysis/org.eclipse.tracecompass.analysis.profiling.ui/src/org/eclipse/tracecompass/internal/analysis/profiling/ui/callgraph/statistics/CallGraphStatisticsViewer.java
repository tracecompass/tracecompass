/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.profiling.ui.callgraph.statistics;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.profiling.core.symbols.ISymbolProvider;
import org.eclipse.tracecompass.analysis.profiling.core.symbols.SymbolProviderManager;
import org.eclipse.tracecompass.analysis.profiling.core.symbols.SymbolProviderUtils;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.SegmentStoreStatisticsModel;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.statistics.AbstractSegmentsStatisticsViewer;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callgraph.CallGraphStatisticsAnalysis;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfGenericTreeEntry;

/**
 * A tree viewer implementation for displaying function duration statistics
 *
 * @author Matthew Khouzam
 *
 */
public class CallGraphStatisticsViewer extends AbstractSegmentsStatisticsViewer {

    private static final class SymbolFormatter implements Function<@NonNull TmfGenericTreeEntry<@NonNull SegmentStoreStatisticsModel>, String> {

        private final @NonNull Collection<@NonNull ISymbolProvider> fSymbolProviders;

        public SymbolFormatter(@Nullable ITmfTrace trace) {
            fSymbolProviders = trace != null ? SymbolProviderManager.getInstance().getSymbolProviders(trace) : Collections.emptySet();
        }

        @Override
        public String apply(@NonNull TmfGenericTreeEntry<@NonNull SegmentStoreStatisticsModel> stat) {
            String original = stat.getName();
            try {
                Long address = Long.decode(original);
                return SymbolProviderUtils.getSymbolText(fSymbolProviders, address);

            } catch (NumberFormatException e) {
                return original;
            }
        }
    }

    /**
     * Constructor
     *
     * @param parent
     *            the parent composite
     */
    public CallGraphStatisticsViewer(Composite parent) {
        super(Objects.requireNonNull(parent), CallGraphStatisticsAnalysis.ID);
        setLabelProvider(new SegmentStoreStatisticsLabelProvider() {
            @Override
            public @NonNull String getColumnText(@Nullable Object element, int columnIndex) {
                if (columnIndex == 0 && (element instanceof TmfGenericTreeEntry)) {
                    TmfGenericTreeEntry<@NonNull SegmentStoreStatisticsModel> entry = (TmfGenericTreeEntry<@NonNull SegmentStoreStatisticsModel>) element;
                    SymbolFormatter fe = new SymbolFormatter(getTrace());
                    return String.valueOf(fe.apply(entry));
                }
                return super.getColumnText(element, columnIndex);
            }
        });
    }
}
