/*******************************************************************************
 * Copyright (c) 2015, 2017 École Polytechnique de Montréal and others.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.graph.ui.criticalpath.view;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.BaseDataProviderTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ILinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEventStyleStrings;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

import com.google.common.collect.ImmutableMap;

/**
 * Presentation provider for the critical path view
 *
 * @author Geneviève Bastien
 */
public class CriticalPathPresentationProvider extends TimeGraphPresentationProvider {

    /**
     * The enumeration of possible states for the view
     */
    public enum State {
        /** Worker is running */
        RUNNING(new RGB(0x33, 0x99, 0x00)),
        /** Worker is interrupted */
        INTERRUPTED(new RGB(0xff, 0xdc, 0x00)),
        /** Worker has been preempted */
        PREEMPTED(new RGB(0xc8, 0x64, 0x00)),
        /** Worker waiting on a timer */
        TIMER(new RGB(0x33, 0x66, 0x99)),
        /** Worker is blocked, waiting on a device */
        BLOCK_DEVICE(new RGB(0x66, 0x00, 0xcc)),
        /** Worker is waiting for user input */
        USER_INPUT(new RGB(0x5a, 0x01, 0x01)),
        /** Worker is waiting on network */
        NETWORK(new RGB(0xff, 0x9b, 0xff)),
        /** Worker is waiting for an IPI */
        IPI(new RGB(0x66, 0x66, 0xcc)),
        /** Any other reason */
        UNKNOWN(new RGB(0x40, 0x3b, 0x33)),
        /** Network communication arrow*/
        NETWORK_ARROW(new RGB(0xff, 0x9b, 0xff)),
        /** Unknown arrow*/
        UNKNOWN_ARROW(new RGB(0x40, 0x3b, 0x33));

        /** RGB color associated with a state */
        public final RGB rgb;

        private State(RGB rgb) {
            this.rgb = rgb;
        }
    }

    /**
     * The state table index for the network arrow
     */
    public static final int NETWORK_ARROW_INDEX;
    /**
     * The state table index for the unkown arrow
     */
    public static final int UNKNOWN_ARROW_INDEX;

    private static final StateItem[] STATE_TABLE;
    static {
        int networkArrowIndex = -1;
        int unknownNetworkIndex = -1;
        STATE_TABLE = new StateItem[State.values().length];
        for (int i = 0; i < STATE_TABLE.length; i++) {
            State state = State.values()[i];

            float heightFactor = 1.0f;
            if (state.equals(State.NETWORK_ARROW)) {
                networkArrowIndex = i;
                heightFactor = 0.1f;
            } else if (state.equals(State.UNKNOWN_ARROW)) {
                unknownNetworkIndex = i;
                heightFactor = 0.1f;
            }

            RGB stateColor = state.rgb;
            String stateType = state.equals(State.NETWORK_ARROW) || state.equals(State.UNKNOWN_ARROW) ? ITimeEventStyleStrings.linkType() : ITimeEventStyleStrings.stateType();
            ImmutableMap<String, Object> styleMap = ImmutableMap.of(
                    ITimeEventStyleStrings.fillStyle(), ITimeEventStyleStrings.solidColorFillStyle(),
                    ITimeEventStyleStrings.fillColor(), new RGBAColor(stateColor.red, stateColor.green, stateColor.blue).toInt(),
                    ITimeEventStyleStrings.label(), String.valueOf(state.toString()),
                    ITimeEventStyleStrings.heightFactor(), heightFactor,
                    ITimeEventStyleStrings.itemTypeProperty(), stateType);
                    STATE_TABLE[i] = new StateItem(styleMap);
        }

        NETWORK_ARROW_INDEX = networkArrowIndex;
        UNKNOWN_ARROW_INDEX = unknownNetworkIndex;
    }

    @Override
    public String getStateTypeName() {
        return Messages.getMessage(Messages.CriticalFlowView_stateTypeName);
    }

    @Override
    public StateItem[] getStateTable() {
        return STATE_TABLE;
    }

    @Override
    public int getStateTableIndex(@Nullable ITimeEvent event) {
        if (event instanceof TimeEvent && ((TimeEvent) event).hasValue()) {
            int value = ((TimeEvent) event).getValue();
            if (event instanceof ILinkEvent) {
                //return the right arrow item index
                return getStateTable()[value].getStateString().equals(State.NETWORK.toString()) ? NETWORK_ARROW_INDEX : UNKNOWN_ARROW_INDEX;
            }
            return value;
        }
        return TRANSPARENT;
    }

    private static State getMatchingState(int status) {
        switch (status) {
        case 0:
            return State.RUNNING;
        case 1:
            return State.INTERRUPTED;
        case 2:
            return State.PREEMPTED;
        case 3:
            return State.TIMER;
        case 4:
            return State.BLOCK_DEVICE;
        case 5:
            return State.USER_INPUT;
        case 6:
            return State.NETWORK;
        case 7:
            return State.IPI;
        default:
            return State.UNKNOWN;
        }
    }

    @Override
    public String getEventName(@Nullable ITimeEvent event) {
        if (event instanceof TimeEvent) {
            TimeEvent ev = (TimeEvent) event;
            if (ev.hasValue()) {
                return NonNullUtils.nullToEmptyString(getMatchingState(ev.getValue()));
            }
        }
        return Messages.getMessage(Messages.CriticalFlowView_multipleStates);
    }

    @Override
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event, long hoverTime) {
        Map<String, String> eventHoverToolTipInfo = super.getEventHoverToolTipInfo(event, hoverTime);
        if (eventHoverToolTipInfo == null) {
            eventHoverToolTipInfo = new LinkedHashMap<>();
        }
        ITimeGraphEntry entry = event.getEntry();
        if (entry instanceof TimeGraphEntry) {
            long id = ((TimeGraphEntry) entry).getModel().getId();
            ITimeGraphDataProvider<? extends TimeGraphEntryModel> provider = BaseDataProviderTimeGraphView.getProvider((TimeGraphEntry) entry);
            SelectionTimeQueryFilter filter = new SelectionTimeQueryFilter(Collections.singletonList(hoverTime), Collections.singleton(id));
            TmfModelResponse<@NonNull Map<@NonNull String, @NonNull String>> tooltipResponse = provider.fetchTooltip(FetchParametersUtils.selectionTimeQueryToMap(filter), null);
            Map<@NonNull String, @NonNull String> tooltipModel = tooltipResponse.getModel();
            if (tooltipModel != null) {
                eventHoverToolTipInfo.putAll(tooltipModel);
            }
        }
        return eventHoverToolTipInfo;
    }
}
