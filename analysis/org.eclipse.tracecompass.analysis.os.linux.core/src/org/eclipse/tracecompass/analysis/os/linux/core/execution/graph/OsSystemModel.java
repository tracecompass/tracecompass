/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.execution.graph;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.model.HostThread;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * This class contains the model of a Linux system
 *
 * TODO: This model is custom made for the LTTng dependency analysis for ease of
 * development of the feature, but most of it and the classes it uses also apply
 * to any Linux OS trace, so the classes in this package should be moved to
 * analysis.os.linux
 *
 * @author Francis Giraldeau
 * @author Geneviève Bastien
 * @since 2.4
 */
public class OsSystemModel {

    private final Table<String, Integer, HostThread> fCurrentTids = HashBasedTable.create();
    private final Table<String, Integer, Deque<OsInterruptContext>> fIntCtxStacks = HashBasedTable.create();
    private final Map<HostThread, OsWorker> fWorkerMap = new HashMap<>();

    /**
     * Cache the TID currently on the CPU of a host, for easier access later on
     *
     * @param cpu
     *            The CPU ID
     * @param ht
     *            The {@link HostThread} object that is running on this CPU
     */
    public void cacheTidOnCpu(Integer cpu, HostThread ht) {
        fCurrentTids.put(ht.getHost(), cpu, ht);
    }

    /**
     * Get the {@link OsWorker} object that is currently running on the CPU
     * of a host
     *
     * @param host
     *            The identifier of the trace/machine of the CPU
     * @param cpu
     *            The CPU ID on which the worker is running
     * @return The {@link OsWorker} running on the CPU
     */
    public @Nullable OsWorker getWorkerOnCpu(String host, Integer cpu) {
        HostThread ht = fCurrentTids.get(host, cpu);
        if (ht == null) {
            return null;
        }
        return findWorker(ht);
    }

    /**
     * Return the worker associated with this host TID
     *
     * @param ht
     *            The host thread associated with a worker
     * @return The {@link OsWorker} associated with a host thread
     */
    public @Nullable OsWorker findWorker(HostThread ht) {
        return fWorkerMap.get(ht);
    }

    /**
     * Add a new worker to the system
     *
     * @param worker
     *            The worker to add
     */
    public void addWorker(OsWorker worker) {
        fWorkerMap.put(worker.getHostThread(), worker);
    }

    /**
     * Get the list of workers on this system
     *
     * @return The list of workers on the system
     */
    public Collection<OsWorker> getWorkers() {
        return fWorkerMap.values();
    }

    /**
     * Pushes an interrupt context on the stack for a CPU on a host
     *
     * @param hostId
     *            The host ID of the trace/machine the interrupt context belongs
     *            to
     * @param cpu
     *            The CPU this interrupt happened on
     * @param interruptCtx
     *            The interrupt context to push on the stack
     */
    public void pushContextStack(String hostId, Integer cpu, OsInterruptContext interruptCtx) {
        Deque<OsInterruptContext> stack = fIntCtxStacks.get(hostId, cpu);
        if (stack == null) {
            stack = new ArrayDeque<>();
            fIntCtxStacks.put(hostId, cpu, stack);
        }
        stack.addFirst(interruptCtx);
    }

    /**
     * Peeks the top of the interrupt context stack for a CPU on a host, to see
     * what is the latest context.
     *
     * @param hostId
     *            The host ID of the trace/machine the interrupt context belongs
     *            to
     * @param cpu
     *            The CPU this interrupt happened on
     * @return The latest interrupt context on the CPU of the host
     */
    public OsInterruptContext peekContextStack(String hostId, Integer cpu) {
        Deque<@NonNull OsInterruptContext> stack = fIntCtxStacks.get(hostId, cpu);
        if (stack == null) {
            return OsInterruptContext.DEFAULT_CONTEXT;
        }
        if (stack.isEmpty()) {
            return OsInterruptContext.DEFAULT_CONTEXT;
        }
        return Objects.requireNonNull(stack.peekFirst());
    }

    /**
     * Removes the top of the interrupt context stack for a CPU on a host and
     * returns the result
     *
     * @param hostId
     *            The host ID of the trace/machine the interrupt context belongs
     *            to
     * @param cpu
     *            The CPU this interrupt happened on
     * @return The latest interrupt context on the CPU of the host
     */
    public @Nullable OsInterruptContext popContextStack(String hostId, Integer cpu) {
        Deque<OsInterruptContext> stack = fIntCtxStacks.get(hostId, cpu);
        if (stack == null) {
            return null;
        }
        if (stack.isEmpty()) {
            return null;
        }
        return stack.removeFirst();
    }

}
