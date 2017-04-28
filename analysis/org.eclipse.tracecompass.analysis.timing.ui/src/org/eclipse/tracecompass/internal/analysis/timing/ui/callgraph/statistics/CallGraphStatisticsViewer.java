/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.timing.ui.callgraph.statistics;

import java.util.Objects;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.statistics.AbstractSegmentsStatisticsViewer;
import org.eclipse.tracecompass.internal.analysis.timing.core.callgraph.CallGraphStatisticsAnalysis;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.symbols.ISymbolProvider;
import org.eclipse.tracecompass.tmf.core.symbols.SymbolProviderManager;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * A tree viewer implementation for displaying function duration statistics
 *
 * @author Matthew Khouzam
 *
 */
public class CallGraphStatisticsViewer extends AbstractSegmentsStatisticsViewer {

    private static final class SymbolFormatter implements Function<SegmentStoreStatisticsEntry, String> {

        private final @Nullable ISymbolProvider fSymbolProvider;

        public SymbolFormatter(@Nullable ITmfTrace trace) {
            fSymbolProvider = trace != null ? SymbolProviderManager.getInstance().getSymbolProvider(trace) : null;
        }

        @Override
        public String apply(@Nullable SegmentStoreStatisticsEntry stat) {

            String original = (stat == null) ? "null" : stat.getName(); //$NON-NLS-1$
            ISymbolProvider symbolProvider = fSymbolProvider;
            if (symbolProvider == null) {
                return original;
            }
            try {
                Long address = Long.decode(original);
                String res = symbolProvider.getSymbolText(address);
                if (res != null) {
                    return res;
                }
                return "0x" + Long.toHexString(address); //$NON-NLS-1$
            } catch (NumberFormatException e) {
                // it's OK, ignore it
            }
            return original;
        }
    }

    /**
     * Constructor
     *
     * @param parent
     *            the parent composite
     */
    public CallGraphStatisticsViewer(Composite parent) {
        super(Objects.requireNonNull(parent));
        setLabelProvider(new SegmentStoreStatisticsLabelProvider() {
            @Override
            public @NonNull String getColumnText(@Nullable Object element, int columnIndex) {
                if (columnIndex == 0 && (element instanceof SegmentStoreStatisticsEntry)) {
                    SegmentStoreStatisticsEntry entry = (SegmentStoreStatisticsEntry) element;
                    SymbolFormatter fe = new SymbolFormatter(getTrace());
                    return String.valueOf(fe.apply(entry));
                }
                return super.getColumnText(element, columnIndex);
            }
        });
    }

    /**
     * Gets the statistics analysis module
     *
     * @return the statistics analysis module
     */
    @Override
    protected @Nullable TmfAbstractAnalysisModule createStatisticsAnalysiModule() {
        return new CallGraphStatisticsAnalysis();
    }
}
