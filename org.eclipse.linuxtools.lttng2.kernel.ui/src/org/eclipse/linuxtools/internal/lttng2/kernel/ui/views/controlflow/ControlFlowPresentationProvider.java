/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.controlflow;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.internal.lttng2.kernel.ui.Messages;
import org.eclipse.linuxtools.lttng2.kernel.core.trace.Attributes;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateSystemQuerier;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.swt.graphics.RGB;

public class ControlFlowPresentationProvider extends TimeGraphPresentationProvider {

    private enum State {
        UNKNOWN     (new RGB(100, 100, 100)),
        WAIT        (new RGB(200, 200, 0)),
        USERMODE    (new RGB(0, 200, 0)),
        SYSCALL     (new RGB(0, 0, 200)),
        INTERRUPTED (new RGB(200, 100, 100));

        public final RGB rgb;

        private State (RGB rgb) {
            this.rgb = rgb;
        }
    }

    @Override 
    public String getStateTypeName() {
        return Messages.ControlFlowView_stateTypeName;
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
    public int getStateTableIndex(ITimeEvent event) {
        if (event instanceof ControlFlowEvent) {
            int status = ((ControlFlowEvent) event).getStatus();
            if (status == Attributes.STATUS_WAIT) {
                return State.WAIT.ordinal();
            } else if (status == Attributes.STATUS_RUN_USERMODE) {
                return State.USERMODE.ordinal();
            } else if (status == Attributes.STATUS_RUN_SYSCALL) {
                return State.SYSCALL.ordinal();
            } else if (status == Attributes.STATUS_INTERRUPTED) {
                return State.INTERRUPTED.ordinal();
            }
        }
        return State.UNKNOWN.ordinal();
    }

    @Override
    public String getEventName(ITimeEvent event) {
        if (event instanceof ControlFlowEvent) {
            int status = ((ControlFlowEvent) event).getStatus();
            if (status == Attributes.STATUS_WAIT) {
                return State.WAIT.toString();
            } else if (status == Attributes.STATUS_RUN_USERMODE) {
                return State.USERMODE.toString();
            } else if (status == Attributes.STATUS_RUN_SYSCALL) {
                return State.SYSCALL.toString();
            } else if (status == Attributes.STATUS_INTERRUPTED) {
                return State.INTERRUPTED.toString();
            }
        }
        return State.UNKNOWN.toString();
    }

    @Override
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event) {
        Map<String, String> retMap = new HashMap<String, String>();
        if (event instanceof ControlFlowEvent) {
            int status = ((ControlFlowEvent) event).getStatus();
            if (status == Attributes.STATUS_RUN_SYSCALL) {
                ControlFlowEntry entry = (ControlFlowEntry) event.getEntry();
                IStateSystemQuerier ssq = entry.getTrace().getStateSystem();
                try {
                    int syscallQuark = ssq.getQuarkRelative(entry.getThreadQuark(), Attributes.SYSTEM_CALL);
                    ITmfStateInterval value = ssq.querySingleState(event.getTime(), syscallQuark);
                    if (!value.getStateValue().isNull()) {
                        ITmfStateValue state = value.getStateValue();
                        retMap.put(Messages.ControlFlowView_attributeSyscallName, state.toString());
                    }

                } catch (AttributeNotFoundException e) {
                    e.printStackTrace();
                } catch (TimeRangeException e) {
                    e.printStackTrace();
                }
            } 
        }

        return retMap;
    }

}
