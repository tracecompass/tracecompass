/*******************************************************************************
 * Copyright (c) 2015 Keba AG
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Christian Mansky - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus.ThreadEntryModel;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus.ThreadStatusDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.ITimeGraphEntryActiveProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Provides Functionality for check Active / uncheck inactive
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @since 1.0
 */
public final class ControlFlowCheckActiveProvider implements ITimeGraphEntryActiveProvider {

    String fLabel;
    String fTooltip;
    private TmfTimeRange fRange = null;
    private ThreadStatusDataProvider fProvider = null;
    private @NonNull Set<Long> fActive = Collections.emptySet();

    /**
     * @param label
     *            Button label
     * @param tooltip
     *            Button tooltip
     */
    public ControlFlowCheckActiveProvider(String label, String tooltip) {
        fLabel = label;
        fTooltip = tooltip;
    }

    @Override
    public String getLabel() {
        return fLabel;
    }

    @Override
    public String getTooltip() {
        return fTooltip;
    }

    @Override
    public boolean isActive(ITimeGraphEntry element) {
        if (element instanceof ControlFlowEntry) {
            ControlFlowEntry cfe = (ControlFlowEntry) element;

            TmfTraceManager traceManager = TmfTraceManager.getInstance();
            TmfTraceContext traceContext = traceManager.getCurrentTraceContext();
            TmfTimeRange range = traceContext.getSelectionRange();

            /* Take precedence of selection over window range. */
            if (Objects.equals(range.getStartTime(), range.getEndTime())) {
                range = traceContext.getWindowRange();
            }

            Set<Long> ids = getActiveIds(cfe, range);
            return ids.contains(cfe.getModel().getId());
        }

        return false;
    }

    private Set<Long> getActiveIds(ControlFlowEntry cfe, TmfTimeRange range) {
        ThreadStatusDataProvider dataProvider = ControlFlowView.getProvider(cfe);
        if (range.equals(fRange) && dataProvider.equals(fProvider)) {
            return fActive;
        }
        TimeQueryFilter filter = new TimeQueryFilter(range.getStartTime().toNanos(), range.getEndTime().toNanos(), 2);
        TmfModelResponse<List<ThreadEntryModel>> response = dataProvider.fetchTree(filter, null);
        List<ThreadEntryModel> model = response.getModel();
        if (model == null) {
            // query must have failed, return empty and don't invalidate the cache.
            return Collections.emptySet();
        }
        fRange = range;
        fActive = Sets.newHashSet(Iterables.transform(model, thread -> thread.getId()));
        return fActive;

    }

}
