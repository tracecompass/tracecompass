/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Geneviève Bastien - Move code to provide base classes for time graph view
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.StateValues;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Activator;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Messages;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.registry.LinuxStyle;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Presentation provider for the control flow view
 */
public class ControlFlowPresentationProvider extends TimeGraphPresentationProvider {

    private static final Map<Integer, StateItem> STATE_MAP;
    private static final List<StateItem> STATE_LIST;

    private static StateItem createState(LinuxStyle style) {
        return new StateItem(style.getColor().rgb, style.getLabel());
    }

    static {
        ImmutableMap.Builder<Integer, StateItem> builder = new ImmutableMap.Builder<>();
        builder.put(StateValues.PROCESS_STATUS_UNKNOWN, createState(LinuxStyle.UNKNOWN));
        builder.put(StateValues.PROCESS_STATUS_RUN_USERMODE, createState(LinuxStyle.USERMODE));
        builder.put(StateValues.PROCESS_STATUS_RUN_SYSCALL, createState(LinuxStyle.SYSCALL));
        builder.put(StateValues.PROCESS_STATUS_INTERRUPTED, createState(LinuxStyle.INTERRUPTED));
        builder.put(StateValues.PROCESS_STATUS_WAIT_BLOCKED, createState(LinuxStyle.WAIT_BLOCKED));
        builder.put(StateValues.PROCESS_STATUS_WAIT_FOR_CPU, createState(LinuxStyle.WAIT_FOR_CPU));
        builder.put(StateValues.PROCESS_STATUS_WAIT_UNKNOWN, createState(LinuxStyle.WAIT_UNKNOWN));
        STATE_MAP = builder.build();
        STATE_LIST = ImmutableList.copyOf(STATE_MAP.values());
    }

    /**
     * Average width of the characters used for state labels. Is computed in the
     * first call to postDrawEvent(). Is null before that.
     */
    private Integer fAverageCharacterWidth = null;

    /**
     * Default constructor
     */
    public ControlFlowPresentationProvider() {
        super(Messages.ControlFlowView_stateTypeName);
    }

    @Override
    public StateItem[] getStateTable() {
        return STATE_LIST.toArray(new StateItem[STATE_LIST.size()]);
    }

    @Override
    public int getStateTableIndex(ITimeEvent event) {
        if (event instanceof TimeEvent && ((TimeEvent) event).hasValue()) {
            int status = ((TimeEvent) event).getValue();
            return STATE_LIST.indexOf(getMatchingState(status));
        }
        if (event instanceof NullTimeEvent) {
            return INVISIBLE;
        }
        return TRANSPARENT;
    }

    @Override
    public String getEventName(ITimeEvent event) {
        if (event instanceof TimeEvent) {
            TimeEvent ev = (TimeEvent) event;
            if (ev.hasValue()) {
                return getMatchingState(ev.getValue()).getStateString();
            }
        }
        return Messages.ControlFlowView_multipleStates;
    }

    private static StateItem getMatchingState(int status) {
        return STATE_MAP.getOrDefault(status, STATE_MAP.get(StateValues.PROCESS_STATUS_WAIT_UNKNOWN));
    }

    @Override
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event) {
        Map<String, String> retMap = new LinkedHashMap<>();
        if (!(event instanceof TimeEvent) || !((TimeEvent) event).hasValue() ||
                !(event.getEntry() instanceof ControlFlowEntry)) {
            return retMap;
        }
        ControlFlowEntry entry = (ControlFlowEntry) event.getEntry();
        ITmfStateSystem ssq = TmfStateSystemAnalysisModule.getStateSystem(entry.getTrace(), KernelAnalysisModule.ID);
        if (ssq == null) {
            return retMap;
        }
        int tid = entry.getThreadId();

        try {
            // Find every CPU first, then get the current thread
            int cpusQuark = ssq.getQuarkAbsolute(Attributes.CPUS);
            List<Integer> cpuQuarks = ssq.getSubAttributes(cpusQuark, false);
            for (Integer cpuQuark : cpuQuarks) {
                int currentThreadQuark = ssq.getQuarkRelative(cpuQuark, Attributes.CURRENT_THREAD);
                ITmfStateInterval interval = ssq.querySingleState(event.getTime(), currentThreadQuark);
                if (!interval.getStateValue().isNull()) {
                    ITmfStateValue state = interval.getStateValue();
                    int currentThreadId = state.unboxInt();
                    if (tid == currentThreadId) {
                        retMap.put(Messages.ControlFlowView_attributeCpuName, ssq.getAttributeName(cpuQuark));
                        break;
                    }
                }
            }

        } catch (AttributeNotFoundException | TimeRangeException | StateValueTypeException e) {
            Activator.getDefault().logError("Error in ControlFlowPresentationProvider", e); //$NON-NLS-1$
        } catch (StateSystemDisposedException e) {
            /* Ignored */
        }
        int status = ((TimeEvent) event).getValue();
        if (status == StateValues.PROCESS_STATUS_RUN_SYSCALL) {
            int syscallQuark = ssq.optQuarkRelative(entry.getThreadQuark(), Attributes.SYSTEM_CALL);
            if (syscallQuark == ITmfStateSystem.INVALID_ATTRIBUTE) {
                return retMap;
            }
            try {
                ITmfStateInterval value = ssq.querySingleState(event.getTime(), syscallQuark);
                if (!value.getStateValue().isNull()) {
                    ITmfStateValue state = value.getStateValue();
                    retMap.put(Messages.ControlFlowView_attributeSyscallName, state.toString());
                }

            } catch (TimeRangeException e) {
                Activator.getDefault().logError("Error in ControlFlowPresentationProvider", e); //$NON-NLS-1$
            } catch (StateSystemDisposedException e) {
                /* Ignored */
            }
        }

        return retMap;
    }

    @Override
    public void postDrawEvent(ITimeEvent event, Rectangle bounds, GC gc) {
        if (fAverageCharacterWidth == null) {
            fAverageCharacterWidth = gc.getFontMetrics().getAverageCharWidth();
        }
        if (bounds.width <= fAverageCharacterWidth) {
            return;
        }
        if (!(event instanceof TimeEvent)) {
            return;
        }
        ControlFlowEntry entry = (ControlFlowEntry) event.getEntry();
        ITmfStateSystem ss = TmfStateSystemAnalysisModule.getStateSystem(entry.getTrace(), KernelAnalysisModule.ID);
        if (ss == null) {
            return;
        }
        int status = ((TimeEvent) event).getValue();

        if (status != StateValues.PROCESS_STATUS_RUN_SYSCALL) {
            return;
        }
        int syscallQuark = ss.optQuarkRelative(entry.getThreadQuark(), Attributes.SYSTEM_CALL);
        if (syscallQuark == ITmfStateSystem.INVALID_ATTRIBUTE) {
            return;
        }
        try {
            ITmfStateInterval value = ss.querySingleState(event.getTime(), syscallQuark);
            if (!value.getStateValue().isNull()) {
                ITmfStateValue state = value.getStateValue();
                gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));

                /*
                 * Remove the "sys_" or "syscall_entry_" or similar from what we
                 * draw in the rectangle. This depends on the trace's event
                 * layout.
                 */
                int beginIndex = 0;
                ITmfTrace trace = entry.getTrace();
                if (trace instanceof IKernelTrace) {
                    IKernelAnalysisEventLayout layout = ((IKernelTrace) trace).getKernelEventLayout();
                    beginIndex = layout.eventSyscallEntryPrefix().length();
                }

                Utils.drawText(gc, state.toString().substring(beginIndex), bounds.x, bounds.y, bounds.width, bounds.height, true, true);
            }
        } catch (TimeRangeException e) {
            Activator.getDefault().logError("Error in ControlFlowPresentationProvider", e); //$NON-NLS-1$
        } catch (StateSystemDisposedException e) {
            /* Ignored */
        }
    }
}
