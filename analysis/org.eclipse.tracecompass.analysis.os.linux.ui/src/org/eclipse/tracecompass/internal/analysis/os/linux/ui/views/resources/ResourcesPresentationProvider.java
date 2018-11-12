/*******************************************************************************
 * Copyright (c) 2012, 2018 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.resources;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tracecompass.analysis.os.linux.core.kernel.StateValues;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.resourcesstatus.ResourcesEntryModel;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.resourcesstatus.ResourcesEntryModel.Type;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Messages;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.registry.LinuxStyle;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphEntryModelWeighted;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.core.presentation.RotatingPaletteProvider;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.BaseDataProviderTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEventStyleStrings;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Presentation provider for the Resource view, based on the generic TMF
 * presentation provider.
 *
 * @author Patrick Tasse
 */
public class ResourcesPresentationProvider extends TimeGraphPresentationProvider {

    private static final int SEPARATOR_HEIGHT = 4;
    private static final int NUM_COLORS = 25;
    private static final float BRIGHTNESS = 0.8f;
    private static final float SATURATION = 0.8f;
    private static final List<RGBAColor> PALETTE =  new RotatingPaletteProvider.Builder()
            .setNbColors(NUM_COLORS)
            .setBrightness(BRIGHTNESS)
            .setSaturation(SATURATION)
            .build().get();
    private static final int COLOR_DIFFERENCIATION_FACTOR = NUM_COLORS / 2 + 2;

    private static final Map<Integer, StateItem> STATE_MAP;

    private static final List<StateItem> STATE_LIST;
    private static final StateItem[] STATE_TABLE;

    private static StateItem createState(LinuxStyle style) {
        return new StateItem(style.toMap());
    }

    static {
        ImmutableMap.Builder<Integer, StateItem> builder = new ImmutableMap.Builder<>();
        builder.put(StateValues.CPU_STATUS_IDLE, new StateItem(LinuxStyle.IDLE.toMap()));
        builder.put(StateValues.CPU_STATUS_RUN_USERMODE, new StateItem(LinuxStyle.USERMODE.toMap()));
        builder.put(StateValues.CPU_STATUS_RUN_SYSCALL, new StateItem(LinuxStyle.SYSCALL.toMap()));
        builder.put(StateValues.CPU_STATUS_IRQ, createState(LinuxStyle.INTERRUPTED));
        builder.put(StateValues.CPU_STATUS_SOFTIRQ, createState(LinuxStyle.SOFT_IRQ));
        builder.put(StateValues.CPU_STATUS_SOFT_IRQ_RAISED, createState(LinuxStyle.SOFT_IRQ_RAISED));
        STATE_MAP = builder.build();
        STATE_LIST = ImmutableList.copyOf(STATE_MAP.values());
        STATE_TABLE = STATE_LIST.toArray(new StateItem[STATE_LIST.size()]);
    }

    /**
     * Default constructor
     */
    public ResourcesPresentationProvider() {
        super();
    }

    private static StateItem getEventState(TimeEvent event) {
        if (event instanceof NullTimeEvent) {
            return null;
        }
        ITimeGraphEntry entry = event.getEntry();
        if (entry instanceof TimeGraphEntry
                && ((TimeGraphEntry) entry).getModel() instanceof ResourcesEntryModel) {
            int value = event.getValue();
            ResourcesEntryModel resourcesModel = (ResourcesEntryModel) ((TimeGraphEntry) entry).getModel();
            Type type = resourcesModel.getType();
            switch (type) {
            case CPU:
                return STATE_MAP.get(value);
            case IRQ:
                return STATE_MAP.get(StateValues.CPU_STATUS_IRQ);
            case SOFT_IRQ:
                if (value == StateValues.CPU_STATUS_SOFT_IRQ_RAISED) {
                    return STATE_MAP.get(StateValues.CPU_STATUS_SOFT_IRQ_RAISED);
                }
                return STATE_MAP.get(StateValues.CPU_STATUS_SOFTIRQ);
            case CURRENT_THREAD:
                if (!event.hasValue()) {
                    return null;
                }
                return STATE_MAP.get(StateValues.CPU_STATUS_RUN_USERMODE);
            case GROUP:
                return null;
            case FREQUENCY:
                if (!event.hasValue()) {
                    return null;
                }
                return STATE_MAP.get(StateValues.CPU_STATUS_RUN_USERMODE);
            default:
                return null;
            }

        }
        return null;
    }

    @Override
    public int getItemHeight(ITimeGraphEntry entry) {
        if (!entry.hasTimeEvents() && entry.getParent() != null) {
            return SEPARATOR_HEIGHT;
        }
        return super.getItemHeight(entry);
    }

    @Override
    public int getStateTableIndex(ITimeEvent event) {
        StateItem state = getEventState((TimeEvent) event);
        if (state != null) {
            return STATE_LIST.indexOf(state);
        }
        if (event instanceof NullTimeEvent) {
            return INVISIBLE;
        }
        return TRANSPARENT;
    }

    @Override
    public StateItem[] getStateTable() {
        return STATE_TABLE;
    }

    @Override
    public String getEventName(ITimeEvent event) {
        StateItem state = getEventState((TimeEvent) event);
        if (state != null) {
            return state.getStateString();
        }
        if (event instanceof NullTimeEvent || isType(event.getEntry(), Type.CURRENT_THREAD)) {
            return null;
        }
        return Messages.ResourcesView_multipleStates;
    }

    @Override
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event, long hoverTime) {
        ITimeGraphEntry entry = event.getEntry();

        if (event instanceof TimeEvent && ((TimeEvent) event).hasValue() && entry instanceof TimeGraphEntry) {
            ITimeGraphEntryModel model = ((TimeGraphEntry) entry).getModel();

            TimeEvent tcEvent = (TimeEvent) event;

            if (tcEvent.hasValue() && model instanceof ResourcesEntryModel) {
                ResourcesEntryModel resourcesModel = (ResourcesEntryModel) model;
                if (resourcesModel.getType().equals(Type.IRQ) || resourcesModel.getType().equals(Type.SOFT_IRQ) ||
                        resourcesModel.getType().equals(Type.CPU) || resourcesModel.getType().equals(Type.CURRENT_THREAD) ||
                        resourcesModel.getType().equals(Type.FREQUENCY)) {
                    ITimeGraphDataProvider<? extends TimeGraphEntryModel> provider = BaseDataProviderTimeGraphView.getProvider((TimeGraphEntry) entry);
                    if (provider != null) {
                        return getTooltip(provider, model.getId(), hoverTime);
                    }
                }
            }
        }
        return Collections.emptyMap();
    }

    private static Map<String, String> getTooltip(ITimeGraphDataProvider<? extends TimeGraphEntryModel> provider, long id, long hoverTime) {
        SelectionTimeQueryFilter filter = new SelectionTimeQueryFilter(Collections.singletonList(hoverTime), Collections.singleton(id));
        TmfModelResponse<Map<String, String>> response = provider.fetchTooltip(FetchParametersUtils.selectionTimeQueryToMap(filter), null);
        Map<String, String> tooltip = response.getModel();

        if (tooltip == null) {
            return Collections.emptyMap();
        }

        Map<String, String> retMap = new LinkedHashMap<>();
        retMap.putAll(tooltip);
        return retMap;
    }

    @Override
    public Map<String, Object> getSpecificEventStyle(ITimeEvent event) {
        Map<String, Object> map = new HashMap<>(super.getSpecificEventStyle(event));
        Integer oldColor = (Integer) map.getOrDefault(ITimeEventStyleStrings.fillColor(), 255);
        RGBAColor rgbaColor = new RGBAColor(oldColor);
        short alpha = rgbaColor.getAlpha();
        if (isType(event.getEntry(), Type.CURRENT_THREAD) && event instanceof TimeEvent) {
            int threadEventValue = ((TimeEvent) event).getValue();
            RGBAColor color = PALETTE.get(Math.floorMod(threadEventValue + COLOR_DIFFERENCIATION_FACTOR, NUM_COLORS));
            RGBAColor newColor = new RGBAColor(color.getRed(), color.getGreen(), color.getBlue(), alpha);
            map.put(ITimeEventStyleStrings.fillColor(), newColor.toInt());
            map.put(ITimeEventStyleStrings.label(), String.valueOf(threadEventValue));

        } else if (event.getEntry() instanceof TimeGraphEntry &&
                ((TimeGraphEntry) event.getEntry()).getModel() instanceof ITimeGraphEntryModelWeighted) {
            ITimeGraphEntryModelWeighted model = (ITimeGraphEntryModelWeighted) ((TimeGraphEntry) event.getEntry()).getModel();
            int eventValue = ((TimeEvent) event).getValue();

            map.put(ITimeEventStyleStrings.heightFactor(), (float) model.getWeight(eventValue));
        }
        return map;
    }

    private static boolean isType(ITimeGraphEntry entry, Type type) {
        if (entry instanceof TimeGraphEntry) {
            ITimeGraphEntryModel model = ((TimeGraphEntry) entry).getModel();
            if (model instanceof ResourcesEntryModel) {
                return (((ResourcesEntryModel) model).getType().equals(type));
            }
        }
        return false;
    }
}
