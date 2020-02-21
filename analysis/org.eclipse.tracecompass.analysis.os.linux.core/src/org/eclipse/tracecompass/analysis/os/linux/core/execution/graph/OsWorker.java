/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.execution.graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.graph.core.base.IGraphWorker;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelThreadInformationProvider;
import org.eclipse.tracecompass.analysis.os.linux.core.model.HostThread;
import org.eclipse.tracecompass.analysis.os.linux.core.model.OsStrings;
import org.eclipse.tracecompass.analysis.os.linux.core.model.ProcessStatus;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.execution.graph.Messages;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * This class represents the worker unit from the execution graph
 *
 * TODO: See if this class could be integrated inside HostThread instead.
 *
 * @author Geneviève Bastien
 * @since 2.4
 */
public class OsWorker implements IGraphWorker {

    private final HostThread fHostTid;
    private final long fStart;

    private String fThreadName;
    private ProcessStatus fStatus = ProcessStatus.UNKNOWN;
    private ProcessStatus fOldStatus = ProcessStatus.UNKNOWN;

    /**
     * Constructor
     *
     * @param ht
     *            The host thread represented by this worker
     * @param name
     *            The name of this thread
     * @param ts
     *            The timestamp
     */
    public OsWorker(HostThread ht, String name, long ts) {
        fHostTid = ht;
        fThreadName = name;
        fStart = ts;
    }

    @Override
    public String getHostId() {
        return fHostTid.getHost();
    }

    @Override
    public @NonNull Map<@NonNull String, @NonNull String> getWorkerInformation() {
        int tid = fHostTid.getTid();
        if (tid == -1) {
            return Collections.emptyMap();
        }
        return Collections.singletonMap(OsStrings.tid(), String.valueOf(tid));
    }

    @Override
    public @NonNull Map<@NonNull String, @NonNull Object> getWorkerAspects() {
        int tid = fHostTid.getTid();
        if (tid == -1) {
            return Collections.emptyMap();
        }
        return Collections.singletonMap(OsStrings.tid(), tid);
    }

    @SuppressWarnings("null")
    @Override
    public @NonNull Map<@NonNull String, @NonNull String> getWorkerInformation(long t) {
        int tid = fHostTid.getTid();
        if (tid == -1) {
            return Collections.emptyMap();
        }
        Map<String, String> workerInfo = new HashMap<>();
        workerInfo.put("tid", String.valueOf(tid)); //$NON-NLS-1$
        Optional<@Nullable KernelAnalysisModule> kam = TmfTraceManager.getInstance().getActiveTraceSet()
                .stream()
                .filter(trace -> trace.getHostId().equals(getHostId()))
                .map(trace -> TmfTraceUtils.getAnalysisModuleOfClass(trace, KernelAnalysisModule.class, KernelAnalysisModule.ID))
                .filter(Objects::nonNull)
                .findFirst();
        if (!kam.isPresent()) {
            return Collections.emptyMap();
        }

        int priority = KernelThreadInformationProvider.getThreadPriority(kam.get(), tid, t);
        if (priority != -1) {
            return Collections.singletonMap(NonNullUtils.nullToEmptyString(Messages.OsWorker_threadPriority), Integer.toString(priority));
        }
        return Collections.emptyMap();
    }

    /**
     * Set the name of this worker
     *
     * @param name
     *            The name of this worker
     */
    public void setName(String name) {
        fThreadName = name;
    }

    /**
     * Get the name of this worker
     *
     * @return The name of the worker
     */
    public String getName() {
        return fThreadName;
    }

    /**
     * Set the status, saving the old value that can still be accessed using
     * {@link OsWorker#getOldStatus()}
     *
     * @param status
     *            The new status of this
     */
    public void setStatus(ProcessStatus status) {
        fOldStatus = fStatus;
        fStatus = status;
    }

    /**
     * Get the status of this thread
     *
     * @return The thread status
     */
    public ProcessStatus getStatus() {
        return fStatus;
    }

    /**
     * Return the previous status this worker was in
     *
     * @return The previous status of this worker
     */
    public ProcessStatus getOldStatus() {
        return fOldStatus;
    }

    /**
     * Get the host thread associated with this worker
     *
     * @return The {@link HostThread} associated with this worker
     */
    public HostThread getHostThread() {
        return fHostTid;
    }

    /**
     * Get the start time of this worker
     *
     * @return The start time in nanoseconds
     */
    public long getStart() {
        return fStart;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof OsWorker) {
            return getHostThread().equals(((OsWorker) obj).getHostThread());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return fHostTid.hashCode();
    }

    @Override
    public String toString() {
        return '[' + fThreadName + ',' + fHostTid.getTid() + ']';
    }

}
