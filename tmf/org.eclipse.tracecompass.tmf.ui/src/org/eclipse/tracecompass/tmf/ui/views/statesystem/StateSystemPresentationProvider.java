/*******************************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.statesystem;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.internal.tmf.core.statesystem.provider.StateSystemDataProvider.ModuleEntryModel;
import org.eclipse.tracecompass.internal.tmf.core.statesystem.provider.StateSystemDataProvider.StateSystemEntryModel;
import org.eclipse.tracecompass.internal.tmf.core.statesystem.provider.StateSystemDataProvider.TraceEntryModel;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.presentation.IPaletteProvider;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.core.presentation.RotatingPaletteProvider;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfAnalysisModuleWithStateSystems;
import org.eclipse.tracecompass.tmf.ui.colors.RGBAUtil;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.BaseDataProviderTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

/**
 * Presentation Provider for the state system time graph view.
 *
 * @author Loic Prieur-Drevon
 */
class StateSystemPresentationProvider extends TimeGraphPresentationProvider {

    /** Number of colors used for State system time events */
    public static final int NUM_COLORS = 9;

    private static final StateItem[] STATE_TABLE = new StateItem[NUM_COLORS + 1];

    static {
        // Set the last one to grey.
        STATE_TABLE[NUM_COLORS] = new StateItem(new RGB(192, 192, 192), "UNKNOWN"); //$NON-NLS-1$
    }

    private IPaletteProvider fPalette = new RotatingPaletteProvider.Builder().setNbColors(NUM_COLORS).build();

    @Override
    public StateItem[] getStateTable() {
        if (STATE_TABLE[0] == null) {
            List<@NonNull RGBAColor> colors = fPalette.get();
            for (int i = 0; i < colors.size(); i++) {
                RGBAColor rgbaColor = colors.get(i);
                STATE_TABLE[i] = new StateItem(RGBAUtil.fromInt(rgbaColor.toInt()).rgb, rgbaColor.toString());
            }
        }
        return STATE_TABLE;
    }

    @Override
    public int getStateTableIndex(ITimeEvent event) {
        if (event instanceof TimeEvent) {
            TimeEvent timeEvent = (TimeEvent) event;
            Object value = timeEvent.getLabel();
            if (value != null) {
                return Math.floorMod(value.hashCode(), NUM_COLORS);
            }

            ITimeGraphEntry entry = event.getEntry();
            if (entry != null) {
                ITmfTreeDataModel model = ((TimeGraphEntry) entry).getEntryModel();
                if (model instanceof StateSystemEntryModel || model instanceof ModuleEntryModel) {
                    // Those two model have an event just so they can have a
                    // tooltip
                    return INVISIBLE;
                }
            }
            // grey
            return NUM_COLORS;
        }
        return INVISIBLE;
    }

    @Override
    public String getEventName(ITimeEvent event) {
        if (event instanceof StateSystemEvent) {
            Object object = ((StateSystemEvent) event).getInterval().getValue();
            return object != null ? object.getClass().getSimpleName() : Messages.TypeNull;
        }
        return null;
    }

    @Override
    public String getStateTypeName(ITimeGraphEntry entry) {
        if (entry instanceof TimeGraphEntry) {
            ITmfTreeDataModel model = ((TimeGraphEntry) entry).getEntryModel();
            if (model instanceof TraceEntryModel) {
                return Messages.TraceEntry_StateTypeName;
            } else if (model instanceof ModuleEntryModel) {
                return Messages.ModuleEntry_StateTypeName;
            } else if (model instanceof StateSystemEntryModel) {
                return Messages.StateSystemEntry_StateTypeName;
            }
        }
        return Messages.AttributeEntry_StateTypeName;
    }

    @Override
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
        Map<@NonNull String, @NonNull Object> parameters = FetchParametersUtils.selectionTimeQueryToMap(new SelectionTimeQueryFilter(hoverTime, hoverTime, 1, Collections.singletonList(entry.getEntryModel().getId())));
        TmfModelResponse<@NonNull Map<@NonNull String, @NonNull String>> response = dataProvider.fetchTooltip(parameters, null);
        Map<@NonNull String, @NonNull String> map = response.getModel();
        if (map != null) {
            retMap.putAll(map);
        }

        return retMap;
    }

    @Override
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event) {
        Map<String, String> retMap = new LinkedHashMap<>();

        if (event instanceof TimeEvent) {
            ITimeGraphEntry entry = event.getEntry();
            ITmfTreeDataModel model = ((TimeGraphEntry) entry).getEntryModel();
            if (model instanceof StateSystemEntryModel) {
                TimeGraphEntry moduleEntry = (TimeGraphEntry) entry.getParent();
                ModuleEntryModel moduleModel = (ModuleEntryModel) moduleEntry.getEntryModel();
                ITmfAnalysisModuleWithStateSystems module = (moduleModel).getModule();
                if (module instanceof TmfAbstractAnalysisModule) {
                    retMap.putAll(((TmfAbstractAnalysisModule) module).getProperties());
                }
            } else if (model instanceof ModuleEntryModel) {
                ITmfAnalysisModuleWithStateSystems module = ((ModuleEntryModel) model).getModule();
                retMap.put(Messages.ModuleHelpText, module.getHelpText());
                retMap.put(Messages.ModuleIsAutomatic, Boolean.toString(module.isAutomatic()));
            }
        }

        return retMap;
    }
}
