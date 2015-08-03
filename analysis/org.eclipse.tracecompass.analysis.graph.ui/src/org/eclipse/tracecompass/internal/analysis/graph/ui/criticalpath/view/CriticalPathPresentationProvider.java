/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.graph.ui.criticalpath.view;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;

/**
 * Presentation provider for the critical path view
 *
 * @author Geneviève Bastien
 */
public class CriticalPathPresentationProvider extends TimeGraphPresentationProvider {

    /**
     * The enumeration of possible states for the view
     */
    public static enum State {
        /** Worker is running */
        RUNNING         (new RGB(0x33, 0x99, 0x00)),
        /** Worker is interrupted */
        INTERRUPTED     (new RGB(0xff, 0xdc, 0x00)),
        /** Worker has been preempted */
        PREEMPTED       (new RGB(0xc8, 0x64, 0x00)),
        /** Worker waiting on a timer */
        TIMER           (new RGB(0x33, 0x66, 0x99)),
        /** Worker is blocked, waiting on a device */
        BLOCK_DEVICE    (new RGB(0x66, 0x00, 0xcc)),
        /** Worker is waiting for user input */
        USER_INPUT      (new RGB(0x5a, 0x01, 0x01)),
        /** Worker is waiting on network */
        NETWORK         (new RGB(0xff, 0x9b, 0xff)),
        /** Any other reason */
        UNKNOWN         (new RGB(0x40, 0x3b, 0x33));

        /** RGB color associated with a state */
        public final RGB rgb;

        private State (RGB rgb) {
            this.rgb = rgb;
        }
    }

    @Override
    public String getStateTypeName() {
        return Messages.getMessage(Messages.CriticalFlowView_stateTypeName);
    }

    @Override
    public StateItem[] getStateTable() {
        StateItem[] stateTable = new StateItem[State.values().length];
        for (int i = 0; i < stateTable.length; i++) {
            State state = State.values()[i];
            stateTable[i] = new StateItem(state.rgb, state.toString());
        }
        return stateTable;
    }

    @Override
    public int getStateTableIndex(@Nullable ITimeEvent event) {
        if (event instanceof TimeEvent && ((TimeEvent) event).hasValue()) {
            return ((TimeEvent) event).getValue();
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
}

