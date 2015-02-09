/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.model;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.graph.core.base.IGraphWorker;
import org.eclipse.tracecompass.analysis.os.linux.core.model.HostThread;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.building.LttngKernelExecGraphProvider.ProcessStatus;

/**
 * This class represents the worker unit from the execution graph
 *
 * TODO: See if this class could be integrated inside HostThread instead.
 *
 * @author Geneviève Bastien
 */
public class LttngWorker implements IGraphWorker {

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
    public LttngWorker(HostThread ht, String name, long ts) {
        fHostTid = ht;
        fThreadName = name;
        fStart = ts;
    }

    @Override
    public String getHostId() {
        return fHostTid.getHost();
    }

    /**
     * Set the name of this worker
     *
     * @param name The name of this worker
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
     * Set the status, saving the old value that can still be accessed using {@link LttngWorker#getOldStatus()}
     *
     * @param status The new status of this
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
        if (obj instanceof LttngWorker) {
            return getHostThread().equals(((LttngWorker) obj).getHostThread());
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
