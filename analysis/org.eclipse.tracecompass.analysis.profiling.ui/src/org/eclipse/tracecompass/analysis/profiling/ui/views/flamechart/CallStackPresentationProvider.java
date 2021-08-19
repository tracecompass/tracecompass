/*******************************************************************************
 * Copyright (c) 2013, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.profiling.ui.views.flamechart;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callstack.provider.CallStackEntryModel;
import org.eclipse.tracecompass.internal.analysis.profiling.ui.views.flamechart.Messages;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.presentation.IPaletteProvider;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.core.presentation.RotatingPaletteProvider;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.ui.colors.RGBAUtil;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.BaseDataProviderTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NamedTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

/**
 * Presentation provider for the Call Stack view, based on the generic TMF
 * presentation provider.
 *
 * @author Patrick Tasse
 * @deprecated use output styles in the data provider instead
 */
@Deprecated
public class CallStackPresentationProvider extends TimeGraphPresentationProvider {

    /** Number of colors used for call stack events */
    public static final int NUM_COLORS = 360;

    private static final StateItem[] STATE_TABLE;
    static {
        STATE_TABLE = new StateItem[NUM_COLORS + 1];
        STATE_TABLE[0] = new StateItem(State.MULTIPLE.rgb, State.MULTIPLE.toString());
    }

    private IPaletteProvider fPalette = new RotatingPaletteProvider.Builder().setNbColors(NUM_COLORS).build();

    private enum State {
        MULTIPLE (new RGB(100, 100, 100)),
        EXEC     (new RGB(0, 200, 0));

        private final RGB rgb;

        private State (RGB rgb) {
            this.rgb = rgb;
        }
    }

    /**
     * Constructor
     *
     * @since 1.2
     */
    public CallStackPresentationProvider() {
        // Do nothing
    }

    @Override
    public @Nullable String getStateTypeName(@Nullable ITimeGraphEntry entry) {
        if (entry instanceof TimeGraphEntry) {
            ITmfTreeDataModel model = ((TimeGraphEntry) entry).getEntryModel();
            if (model instanceof CallStackEntryModel) {
                int type = ((CallStackEntryModel) model).getStackLevel();
                if (type >= 0) {
                    return Messages.CallStackPresentationProvider_Thread;
                } else if (type == -1) {
                    return Messages.CallStackPresentationProvider_Process;
                }
            }
        }
        return null;
    }

    @Override
    public StateItem[] getStateTable() {
        if (STATE_TABLE[1] == null) {
            int i = 1;
            for (RGBAColor color : fPalette.get()) {
                STATE_TABLE[i] = new StateItem(RGBAUtil.fromRGBAColor(color).rgb, color.toString());
                i++;
            }
        }
        return STATE_TABLE;
    }

    @Override
    public int getStateTableIndex(@Nullable ITimeEvent event) {
        if (event instanceof NamedTimeEvent) {
            NamedTimeEvent callStackEvent = (NamedTimeEvent) event;
            return Math.floorMod(callStackEvent.getValue(), fPalette.get().size()) + 1;
        } else if (event instanceof NullTimeEvent) {
            return INVISIBLE;
        }
        return State.MULTIPLE.ordinal();
    }

    @Override
    public String getEventName(@Nullable ITimeEvent event) {
        if (event instanceof NamedTimeEvent) {
            return ((NamedTimeEvent) event).getLabel();
        }
        return State.MULTIPLE.toString();
    }

    @Override
    @NonNullByDefault({})
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event, long hoverTime) {
        Map<String, String> retMap = super.getEventHoverToolTipInfo(event, hoverTime);
        if (retMap == null) {
            retMap = new LinkedHashMap<>(1);
        }

        if (!(event instanceof TimeEvent) || !((TimeEvent) event).hasValue() ||
                !(event.getEntry() instanceof TimeGraphEntry)) {
            return retMap;
        }

        TimeGraphEntry entry = (TimeGraphEntry) event.getEntry();
        ITimeGraphDataProvider<? extends TimeGraphEntryModel> dataProvider = BaseDataProviderTimeGraphView.getProvider(entry);
        TmfModelResponse<@NonNull Map<@NonNull String, @NonNull String>> response = dataProvider.fetchTooltip(
                FetchParametersUtils.selectionTimeQueryToMap(new SelectionTimeQueryFilter(hoverTime, hoverTime, 1, Collections.singletonList(entry.getEntryModel().getId()))), null);
        Map<@NonNull String, @NonNull String> tooltipModel = response.getModel();
        if (tooltipModel != null) {
            retMap.putAll(tooltipModel);
        }

        return retMap;
    }
}
