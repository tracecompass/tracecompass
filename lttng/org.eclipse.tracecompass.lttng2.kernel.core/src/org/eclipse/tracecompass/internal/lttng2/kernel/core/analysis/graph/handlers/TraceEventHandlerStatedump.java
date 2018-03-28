/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.handlers;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsExecutionGraphProvider;
import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsSystemModel;
import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsWorker;
import org.eclipse.tracecompass.analysis.os.linux.core.model.HostThread;
import org.eclipse.tracecompass.analysis.os.linux.core.model.ProcessStatus;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.model.EventField;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

/**
 * Handles the LTTng statedump events necessary for the initialization of the
 * system model
 *
 * @author Francis Giraldeau
 */
public class TraceEventHandlerStatedump extends BaseHandler {

    /**
     * Constructor
     *
     * @param provider
     *            The parent graph provider
     * @param priority
     *            The priority of this handler. It will determine when it will be
     *            executed
     */
    public TraceEventHandlerStatedump(OsExecutionGraphProvider provider, int priority) {
        super(provider, priority);
    }

    @Override
    public void handleEvent(ITmfEvent event) {
        IKernelAnalysisEventLayout eventLayout = getProvider().getEventLayout(event.getTrace());
        OsSystemModel system = getProvider().getSystem();
        String eventName = event.getName();
        if (!eventName.equals(eventLayout.eventStatedumpProcessState())) {
            return;
        }
        ITmfEventField content = event.getContent();
        Integer tid = content.getFieldValue(Integer.class, eventLayout.fieldTid());
        String name = EventField.getOrDefault(event, eventLayout.fieldName(), nullToEmptyString(Messages.TraceEventHandlerSched_UnknownThreadName));
        Integer status = content.getFieldValue(Integer.class, eventLayout.fieldStatus());
        if (tid == null || status == null) {
            // Insufficient data, ignore this event
            return;
        }

        String host = event.getTrace().getHostId();
        long ts = event.getTimestamp().getValue();

        HostThread ht = new HostThread(host, tid);
        OsWorker task = system.findWorker(ht);
        if (task == null) {
            task = new OsWorker(ht, name, ts);
            system.addWorker(task);
        } else {
            task.setName(name);
        }

        task.setStatus(ProcessStatus.getStatusFromStatedump(status));
    }

}
