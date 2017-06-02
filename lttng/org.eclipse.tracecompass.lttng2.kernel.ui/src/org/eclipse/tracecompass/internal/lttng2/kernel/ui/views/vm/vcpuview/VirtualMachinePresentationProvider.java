/*******************************************************************************
 * Copyright (c) 2016, 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.ui.views.vm.vcpuview;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.StateValues;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.VcpuStateValues;
import org.eclipse.tracecompass.internal.lttng2.kernel.ui.views.vm.vcpuview.VirtualMachineCommon.Type;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;

/**
 * Presentation provider for the Virtual Machine view, based on the generic TMF
 * presentation provider.
 *
 * @author Mohamad Gebai
 */
public class VirtualMachinePresentationProvider extends TimeGraphPresentationProvider {

    private static final int ALPHA = 70;

    /*
     * TODO: Some of it is copy-pasted from the control flow presentation
     * provider because it actually is the same data as from the control flow
     * view. Ideally, we should reuse what is there instead of rewriting it here
     */
    private enum State {
        UNKNOWN(new RGB(100, 100, 100)),
        IDLE(new RGB(200, 200, 200)),
        USERMODE(new RGB(0, 200, 0)),
        WAIT_VMM(new RGB(200, 0, 0)),
        VCPU_PREEMPTED(new RGB(120, 40, 90)),
        THREAD_UNKNOWN(new RGB(100, 100, 100)),
        THREAD_WAIT_BLOCKED(new RGB(200, 200, 0)),
        THREAD_WAIT_FOR_CPU(new RGB(200, 100, 0)),
        THREAD_USERMODE(new RGB(0, 200, 0)),
        THREAD_SYSCALL(new RGB(0, 0, 200)),
        THREAD_INTERRUPTED(new RGB(200, 0, 100));

        private final RGB fRgb;

        private State(RGB rgb) {
            fRgb = rgb;
        }
    }

    private static final StateItem[] STATE_TABLE;
    static {
        State[] states = getStateValues();
        STATE_TABLE = new StateItem[states.length];
        for (int i = 0; i < STATE_TABLE.length; i++) {
            State state = states[i];
            STATE_TABLE[i] = new StateItem(state.fRgb, state.toString());
        }
    }

    /**
     * Default constructor
     */
    public VirtualMachinePresentationProvider() {
        super();
    }

    private static State[] getStateValues() {
        return State.values();
    }

    private static State getStateForVcpu(int value) {
        if ((value & VcpuStateValues.VCPU_PREEMPT) > 0) {
            return State.VCPU_PREEMPTED;
        } else if ((value & VcpuStateValues.VCPU_VMM) > 0) {
            return State.WAIT_VMM;
        } else if (value == 2) {
            return State.USERMODE;
        } else if (value == 1) {
            return State.IDLE;
        } else {
            return State.UNKNOWN;
        }
    }

    private static @Nullable State getStateForThread(int value) {
        if (value == VcpuStateValues.VCPU_PREEMPT) {
            return null;
        }
        switch (value) {
        case StateValues.PROCESS_STATUS_RUN_USERMODE:
            return State.THREAD_USERMODE;
        case StateValues.PROCESS_STATUS_RUN_SYSCALL:
            return State.THREAD_SYSCALL;
        case StateValues.PROCESS_STATUS_WAIT_FOR_CPU:
            return State.THREAD_WAIT_FOR_CPU;
        case StateValues.PROCESS_STATUS_WAIT_BLOCKED:
            return State.THREAD_WAIT_BLOCKED;
        case StateValues.PROCESS_STATUS_INTERRUPTED:
            return State.THREAD_INTERRUPTED;
        case StateValues.PROCESS_STATUS_UNKNOWN:
        case StateValues.PROCESS_STATUS_WAIT_UNKNOWN:
            return State.THREAD_UNKNOWN;
        default:
            return null;
        }
    }

    private static @Nullable State getEventState(TimeEvent event) {
        if (event.hasValue()) {
            VirtualMachineViewEntry entry = (VirtualMachineViewEntry) event.getEntry();
            int value = event.getValue();

            if (entry.getType() == Type.VCPU) {
                return getStateForVcpu(value);
            } else if (entry.getType() == Type.THREAD) {
                return getStateForThread(value);
            }
        }
        return null;
    }

    @Override
    public int getStateTableIndex(@Nullable ITimeEvent event) {
        if (event == null) {
            return TRANSPARENT;
        }
        State state = getEventState((TimeEvent) event);
        if (state != null) {
            return state.ordinal();
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
    public @Nullable String getEventName(@Nullable ITimeEvent event) {
        if (event == null) {
            return null;
        }
        State state = getEventState((TimeEvent) event);
        if (state != null) {
            return state.toString();
        }
        if (event instanceof NullTimeEvent) {
            return null;
        }
        return Messages.VmView_multipleStates;
    }

    @Override
    public void postDrawEvent(@Nullable ITimeEvent event, @Nullable Rectangle bounds, @Nullable GC gc) {
        if (bounds == null || gc == null || !(event instanceof TimeEvent)) {
            return;
        }
        boolean visible = bounds.width == 0 ? false : true;
        if (!visible) {
            return;
        }
        TimeEvent ev = (TimeEvent) event;
        /*
         * FIXME: There seems to be a bug when multiple events should be drawn
         * under a alpha event. See FIXME comment in
         * VirtualMachineView#getEventList
         */
        if (ev.hasValue()) {
            VirtualMachineViewEntry entry = (VirtualMachineViewEntry) event.getEntry();

            if (entry.getType() == Type.THREAD) {
                int value = ev.getValue();
                if ((value & VcpuStateValues.VCPU_PREEMPT) != 0) {
                    /*
                     * If the status was preempted at this time, draw an alpha
                     * over this state
                     */
                    Color alphaColor = Display.getDefault().getSystemColor(SWT.COLOR_RED);

                    int alpha = gc.getAlpha();
                    Color background = gc.getBackground();
                    // fill all rect area
                    gc.setBackground(alphaColor);
                    gc.setAlpha(ALPHA);
                    gc.fillRectangle(bounds);

                    gc.setBackground(background);
                    gc.setAlpha(alpha);
                }
            }
        }
    }

}