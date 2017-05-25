/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.handlers;

import org.eclipse.tracecompass.analysis.os.linux.core.kernel.LinuxValues;
import org.eclipse.tracecompass.analysis.os.linux.core.model.HostThread;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.building.LttngKernelExecGraphProvider;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.building.LttngKernelExecGraphProvider.ProcessStatus;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.model.EventField;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.model.LttngSystemModel;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.model.LttngWorker;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * Provides the current task running on a CPU according to scheduling events
 *
 * @author Francis Giraldeau
 * @author Geneviève Bastien
 */
public class TraceEventHandlerSched extends BaseHandler {

    /**
     * Constructor
     *
     * @param provider
     *            The parent graph provider
     */
    public TraceEventHandlerSched(LttngKernelExecGraphProvider provider) {
        super(provider);
    }

    @Override
    public void handleEvent(ITmfEvent ev) {
        String eventName = ev.getName();
        IKernelAnalysisEventLayout eventLayout = getProvider().getEventLayout(ev.getTrace());

        if (eventName.equals(eventLayout.eventSchedSwitch())) {
            handleSchedSwitch(ev);
        } else if (eventName.equals(eventLayout.eventSchedProcessFork())) {
            handleSchedProcessFork(ev);
        } else if (eventName.equals(eventLayout.eventSchedProcessExit())) {
            handleSchedProcessExit(ev);
        } else if (eventName.equals(eventLayout.eventSchedProcessExec())) {
            handleSchedProcessExec(ev);
        } else if (isWakeupEvent(ev)) {
            handleSchedWakeup(ev);
        }
    }

    private void handleSchedSwitch(ITmfEvent event) {
        Integer cpu = NonNullUtils.checkNotNull(TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event));
        IKernelAnalysisEventLayout eventLayout = getProvider().getEventLayout(event.getTrace());
        LttngSystemModel system = getProvider().getSystem();

        Integer next = EventField.getInt(event, eventLayout.fieldNextTid());
        Integer prev = EventField.getInt(event, eventLayout.fieldPrevTid());
        long ts = event.getTimestamp().getValue();
        long prev_state = EventField.getLong(event, eventLayout.fieldPrevState());
        prev_state = (long) ((int) prev_state) & (LinuxValues.TASK_STATE_RUNNING | LinuxValues.TASK_INTERRUPTIBLE | LinuxValues.TASK_UNINTERRUPTIBLE);
        String host = event.getTrace().getHostId();

        system.cacheTidOnCpu(cpu, new HostThread(event.getTrace().getHostId(), next));

        HostThread nextHt = new HostThread(host, next);
        LttngWorker nextTask = system.findWorker(nextHt);
        if (nextTask == null) {
            String name = EventField.getOrDefault(event, eventLayout.fieldNextComm(), NonNullUtils.checkNotNull(Messages.TraceEventHandlerSched_UnknownThreadName));
            nextTask = new LttngWorker(nextHt, name, ts);
            system.addWorker(nextTask);
        }
        nextTask.setStatus(ProcessStatus.RUN);

        HostThread prevHt = new HostThread(host, prev);
        LttngWorker prevTask = system.findWorker(prevHt);
        String name = EventField.getOrDefault(event, eventLayout.fieldPrevComm(), NonNullUtils.checkNotNull(Messages.TraceEventHandlerSched_UnknownThreadName));
        if (prevTask == null) {
            prevTask = new LttngWorker(prevHt, name, ts);
            system.addWorker(prevTask);
        } else if (prev != 0) {
            /* update the process name if changed at runtime */
            prevTask.setName(name);
        }
        /* prev_state == 0 means runnable, thus waits for cpu */
        if (prev_state == 0) {
            prevTask.setStatus(ProcessStatus.WAIT_CPU);
        } else {
            prevTask.setStatus(ProcessStatus.WAIT_BLOCKED);
        }
    }

    private void handleSchedProcessFork(ITmfEvent event) {
        String host = event.getTrace().getHostId();
        IKernelAnalysisEventLayout eventLayout = getProvider().getEventLayout(event.getTrace());
        LttngSystemModel system = getProvider().getSystem();

        Integer childTid = EventField.getInt(event, eventLayout.fieldChildTid());
        String name = EventField.getString(event, eventLayout.fieldChildComm());
        long ts = event.getTimestamp().getValue();

        HostThread childHt = new HostThread(host, childTid);

        LttngWorker childTask = system.findWorker(childHt);
        if (childTask == null) {
            childTask = new LttngWorker(childHt, name, ts);
            system.addWorker(childTask);
        }

        childTask.setStatus(ProcessStatus.WAIT_FORK);
    }

    private void handleSchedWakeup(ITmfEvent event) {
        String host = event.getTrace().getHostId();
        Integer cpu = NonNullUtils.checkNotNull(TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event));
        IKernelAnalysisEventLayout eventLayout = getProvider().getEventLayout(event.getTrace());
        LttngSystemModel system = getProvider().getSystem();

        Integer tid = EventField.getInt(event, eventLayout.fieldTid());
        HostThread targetHt = new HostThread(host, tid);

        LttngWorker target = system.findWorker(targetHt);
        LttngWorker current = system.getWorkerOnCpu(host, cpu);

        if (target == null) {
            String name = EventField.getOrDefault(event, eventLayout.fieldComm(), NonNullUtils.checkNotNull(Messages.TraceEventHandlerSched_UnknownThreadName));
            target = new LttngWorker(targetHt, name, event.getTimestamp().getValue());
            system.addWorker(target);
            target.setStatus(ProcessStatus.WAIT_BLOCKED);
        }
        // spurious wakeup
        ProcessStatus status = target.getStatus();
        if ((current != null && target.getHostThread().equals(current.getHostThread())) ||
                status == ProcessStatus.WAIT_CPU) {
            return;
        }
        if (status == ProcessStatus.WAIT_BLOCKED ||
                status == ProcessStatus.WAIT_FORK ||
                status == ProcessStatus.UNKNOWN) {
            target.setStatus(ProcessStatus.WAIT_CPU);
            return;
        }
    }

    private void handleSchedProcessExit(ITmfEvent event) {
        String host = event.getTrace().getHostId();
        IKernelAnalysisEventLayout eventLayout = getProvider().getEventLayout(event.getTrace());
        LttngSystemModel system = getProvider().getSystem();

        Integer tid = EventField.getInt(event, eventLayout.fieldTid());
        LttngWorker task = system.findWorker(new HostThread(host, tid));
        if (task == null) {
            return;
        }
        task.setStatus(ProcessStatus.EXIT);
    }

    private void handleSchedProcessExec(ITmfEvent event) {
        String host = event.getTrace().getHostId();
        Integer cpu = NonNullUtils.checkNotNull(TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event));
        IKernelAnalysisEventLayout eventLayout = getProvider().getEventLayout(event.getTrace());
        LttngSystemModel system = getProvider().getSystem();

        String filename = EventField.getString(event, eventLayout.fieldFilename());
        LttngWorker task = system.getWorkerOnCpu(host, cpu);
        if (task == null) {
            return;
        }
        task.setName(filename);
    }

}
