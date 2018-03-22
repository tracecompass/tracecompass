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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.StateValues;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.resourcesstatus.ResourcesEntryModel;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.resourcesstatus.ResourcesEntryModel.Type;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Messages;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.registry.LinuxStyle;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.ITimeGraphEntryModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.internal.provisional.tmf.core.presentation.RotatingPaletteProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils.Resolution;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils.TimeFormat;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.BaseDataProviderTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEventStyleStrings;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NamedTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Presentation provider for the Resource view, based on the generic TMF
 * presentation provider.
 *
 * @author Patrick Tasse
 */
public class ResourcesPresentationProvider extends TimeGraphPresentationProvider {

    private Color fColorWhite;
    private Color fColorGray;
    private Integer fAverageCharWidth;
    private static final int NUM_COLORS = 25;
    private static final float BRIGHTNESS = 0.8f;
    private static final float SATURATION = 0.8f;
    private static final List<RGBAColor> PALETTE =  new RotatingPaletteProvider.Builder()
            .setNbColors(NUM_COLORS)
            .setBrightness(BRIGHTNESS)
            .setSaturation(SATURATION)
            .build().get();
    private static final int COLOR_DIFFERENCIATION_FACTOR = NUM_COLORS / 2 + 2;
    /**
     * State table index for an idle event (light grey)
     */
    private static final int IDLE_THREAD = 0;

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
            case TRACE:
                return null;
            default:
                return null;
            }

        }
        return null;
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
                // Check for IRQ or Soft_IRQ type
                if (resourcesModel.getType().equals(Type.IRQ) || resourcesModel.getType().equals(Type.SOFT_IRQ)) {

                    // Get CPU of IRQ or SoftIRQ and provide it for the tooltip
                    // display
                    int cpu = tcEvent.getValue();
                    if (cpu >= 0) {
                        Map<String, String> retMap = new LinkedHashMap<>(1);
                        retMap.put(Messages.ResourcesView_attributeCpuName, String.valueOf(cpu));
                        return retMap;
                    }
                }

                // Check for type CPU
                else if (resourcesModel.getType().equals(Type.CPU) || resourcesModel.getType().equals(Type.CURRENT_THREAD)) {
                    int status = tcEvent.getValue();
                    ITimeGraphDataProvider<? extends TimeGraphEntryModel> provider = BaseDataProviderTimeGraphView.getProvider((TimeGraphEntry) entry);
                    if (provider != null) {
                        return getTooltipForCpu(provider, model.getId(), hoverTime, status);
                    }
                }
            }
        }
        return Collections.emptyMap();
    }

    private static Map<String, String> getTooltipForCpu(ITimeGraphDataProvider<? extends TimeGraphEntryModel> provider, long id, long hoverTime, int status) {
        SelectionTimeQueryFilter filter = new SelectionTimeQueryFilter(Collections.singletonList(hoverTime), Collections.singleton(id));
        TmfModelResponse<Map<String, String>> response = provider.fetchTooltip(filter, null);
        Map<String, String> tooltip = response.getModel();
        if (tooltip == null) {
            return Collections.emptyMap();
        }

        Map<String, String> retMap = new LinkedHashMap<>();
        if (status == StateValues.CPU_STATUS_IRQ) {
            // In IRQ state get the IRQ that caused the interruption
            String irq = tooltip.get(Attributes.IRQS);
            if (irq != null) {
                retMap.put(Messages.ResourcesView_attributeIrqName, irq);
            }
        } else if (status == StateValues.CPU_STATUS_SOFTIRQ) {
            // In SOFT_IRQ state get the SOFT_IRQ that caused the interruption
            String irq = tooltip.get(Attributes.SOFT_IRQS);
            if (irq != null) {
                retMap.put(Messages.ResourcesView_attributeSoftIrqName, irq);
            }
        } else if (status == StateValues.CPU_STATUS_RUN_USERMODE || status == StateValues.CPU_STATUS_RUN_SYSCALL || status > StateValues.CPU_STATUS_IRQ) {
            // In running state get the current TID
            retMap.put(Messages.ResourcesView_attributeHoverTime, FormatTimeUtils.formatTime(hoverTime, TimeFormat.CALENDAR, Resolution.NANOSEC));
            String tidName = tooltip.get(Attributes.CURRENT_THREAD);
            if (tidName != null) {
                retMap.put(Messages.ResourcesView_attributeTidName, tidName);
            }
            String execName = tooltip.get(Attributes.EXEC_NAME);
            if (execName != null) {
                retMap.put(Messages.ResourcesView_attributeProcessName, execName);
            }
            String syscallName = tooltip.get(Attributes.SYSTEM_CALL);
            if (syscallName != null) {
                retMap.put(Messages.ResourcesView_attributeSyscallName, syscallName);
            }
        }
        return retMap;
    }

    /**
     * Returns the average character width, measured in pixels, of the font
     * described by the receiver.
     *
     * @param gc
     *            The graphic context
     * @return the average character width of the font
     */
    @Deprecated
    private static int getAverageCharWidth(GC gc) {
        return gc.getFontMetrics().getAverageCharWidth();
    }

    @Override
    public Map<String, Object> getSpecificEventStyle(ITimeEvent event) {
        if (isType(event.getEntry(), Type.CURRENT_THREAD) && event instanceof TimeEvent) {
            int threadEventValue = ((TimeEvent) event).getValue();
            if (threadEventValue == IDLE_THREAD) {
                return ImmutableMap.of(ITimeEventStyleStrings.fillColor(), 0);
            }
            RGBAColor color = PALETTE.get(Math.floorMod(threadEventValue + COLOR_DIFFERENCIATION_FACTOR, NUM_COLORS));
            return ImmutableMap.of(ITimeEventStyleStrings.fillColor(), color.toInt(),
                    ITimeEventStyleStrings.label(), String.valueOf(threadEventValue));

        }
        return super.getSpecificEventStyle(event);
    }

    @Override
    public void postDrawEvent(ITimeEvent event, Rectangle bounds, GC gc) {
        if (fColorGray == null) {
            fColorGray = gc.getDevice().getSystemColor(SWT.COLOR_GRAY);
        }
        if (fColorWhite == null) {
            fColorWhite = gc.getDevice().getSystemColor(SWT.COLOR_WHITE);
        }
        if (fAverageCharWidth == null) {
            fAverageCharWidth = getAverageCharWidth(gc);
        }

        if (bounds.width <= fAverageCharWidth || !(event instanceof NamedTimeEvent)) {
            return;
        }
        NamedTimeEvent tcEvent = (NamedTimeEvent) event;
        gc.setForeground(fColorWhite);

        String label = tcEvent.getLabel();

        Utils.drawText(gc, label, bounds.x, bounds.y, bounds.width, bounds.height, true, true);

        gc.setForeground(fColorGray);
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
