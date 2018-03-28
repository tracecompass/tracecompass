/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.handlers;

import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsExecutionGraphProvider;
import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsSystemModel;
import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsWorker;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.LinuxValues;
import org.eclipse.tracecompass.analysis.os.linux.core.model.HostThread;
import org.eclipse.tracecompass.analysis.os.linux.core.model.ProcessStatus;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.model.EventField;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * Provides the current task running on a CPU according to scheduling events
 *
 * @author Francis Giraldeau
 * @author Geneviève Bastien
 */
public class TraceEventHandlerSched extends BaseHandler {

    private static final String TASK_UNKNOWN = "Unknown"; //$NON-NLS-1$

    /**
     * Constructor
     *
     * @param provider
     *            The parent graph provider
     * @param priority
     *            The priority of this handler. It will determine when it will be
     *            executed
     */
    public TraceEventHandlerSched(OsExecutionGraphProvider provider, int priority) {
        super(provider, priority);
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
        OsSystemModel system = getProvider().getSystem();
        ITmfEventField content = event.getContent();

        Integer next = content.getFieldValue(Integer.class, eventLayout.fieldNextTid());
        Integer prev = content.getFieldValue(Integer.class, eventLayout.fieldPrevTid());
        if (next == null || prev == null) {
            return;
        }
        long ts = event.getTimestamp().getValue();
        Integer prev_state = content.getFieldValue(Integer.class, eventLayout.fieldPrevState());
        prev_state = prev_state == null ? 0 : prev_state & (LinuxValues.TASK_STATE_RUNNING | LinuxValues.TASK_INTERRUPTIBLE | LinuxValues.TASK_UNINTERRUPTIBLE);
        String host = event.getTrace().getHostId();

        system.cacheTidOnCpu(cpu, new HostThread(event.getTrace().getHostId(), next));

        HostThread nextHt = new HostThread(host, next);
        OsWorker nextTask = system.findWorker(nextHt);
        if (nextTask == null) {
            String name = EventField.getOrDefault(event, eventLayout.fieldNextComm(), NonNullUtils.checkNotNull(Messages.TraceEventHandlerSched_UnknownThreadName));
            nextTask = new OsWorker(nextHt, name, ts);
            system.addWorker(nextTask);
        }
        nextTask.setStatus(ProcessStatus.RUN);

        HostThread prevHt = new HostThread(host, prev);
        OsWorker prevTask = system.findWorker(prevHt);
        String name = EventField.getOrDefault(event, eventLayout.fieldPrevComm(), NonNullUtils.checkNotNull(Messages.TraceEventHandlerSched_UnknownThreadName));
        if (prevTask == null) {
            prevTask = new OsWorker(prevHt, name, ts);
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
        OsSystemModel system = getProvider().getSystem();
        ITmfEventField content = event.getContent();

        Integer childTid = content.getFieldValue(Integer.class, eventLayout.fieldChildTid());
        String name = content.getFieldValue(String.class, eventLayout.fieldChildComm());
        if (childTid == null) {
            return;
        }
        name = (name == null ? String.valueOf(childTid) : name);
        long ts = event.getTimestamp().getValue();

        HostThread childHt = new HostThread(host, childTid);

        OsWorker childTask = system.findWorker(childHt);
        if (childTask == null) {
            childTask = new OsWorker(childHt, name, ts);
            system.addWorker(childTask);
        }

        childTask.setStatus(ProcessStatus.WAIT_FORK);
    }

    private void handleSchedWakeup(ITmfEvent event) {
        String host = event.getTrace().getHostId();
        Integer cpu = NonNullUtils.checkNotNull(TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event));
        IKernelAnalysisEventLayout eventLayout = getProvider().getEventLayout(event.getTrace());
        OsSystemModel system = getProvider().getSystem();

        Integer tid = event.getContent().getFieldValue(Integer.class, eventLayout.fieldTid());
        if (tid == null) {
            return;
        }
        HostThread targetHt = new HostThread(host, tid);

        OsWorker target = system.findWorker(targetHt);
        OsWorker current = system.getWorkerOnCpu(host, cpu);

        if (target == null) {
            String name = EventField.getOrDefault(event, eventLayout.fieldComm(), NonNullUtils.checkNotNull(Messages.TraceEventHandlerSched_UnknownThreadName));
            target = new OsWorker(targetHt, name, event.getTimestamp().getValue());
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
        OsSystemModel system = getProvider().getSystem();

        Integer tid = event.getContent().getFieldValue(Integer.class, eventLayout.fieldTid());
        if (tid == null) {
            return;
        }
        OsWorker task = system.findWorker(new HostThread(host, tid));
        if (task == null) {
            return;
        }
        task.setStatus(ProcessStatus.EXIT);
    }

    private void handleSchedProcessExec(ITmfEvent event) {
        String host = event.getTrace().getHostId();
        Integer cpu = NonNullUtils.checkNotNull(TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event));
        IKernelAnalysisEventLayout eventLayout = getProvider().getEventLayout(event.getTrace());
        OsSystemModel system = getProvider().getSystem();

        String filename = event.getContent().getFieldValue(String.class, eventLayout.fieldFilename());
        OsWorker task = system.getWorkerOnCpu(host, cpu);
        if (task == null) {
            return;
        }
        task.setName(filename == null ? TASK_UNKNOWN : filename);
    }

}
