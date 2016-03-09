/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

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
 */
public class LttngSystemModel {

    private final Table<String, Integer, HostThread> fCurrentTids = HashBasedTable.create();
    private final Table<String, Integer, Stack<LttngInterruptContext>> fIntCtxStacks = HashBasedTable.create();
    private final Map<HostThread, LttngWorker> fWorkerMap = new HashMap<>();

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
     * Get the {@link LttngWorker} object that is currently running on the CPU
     * of a host
     *
     * @param host
     *            The identifier of the trace/machine of the CPU
     * @param cpu
     *            The CPU ID on which the worker is running
     * @return The {@link LttngWorker} running on the CPU
     */
    public @Nullable LttngWorker getWorkerOnCpu(String host, Integer cpu) {
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
     * @return The {@link LttngWorker} associated with a host thread
     */
    public @Nullable LttngWorker findWorker(HostThread ht) {
        return fWorkerMap.get(ht);
    }

    /**
     * Add a new worker to the system
     *
     * @param worker
     *            The worker to add
     */
    public void addWorker(LttngWorker worker) {
        fWorkerMap.put(worker.getHostThread(), worker);
    }

    /**
     * Get the list of workers on this system
     *
     * @return The list of workers on the system
     */
    public Collection<LttngWorker> getWorkers() {
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
    public void pushContextStack(String hostId, Integer cpu, LttngInterruptContext interruptCtx) {
        Stack<LttngInterruptContext> stack = fIntCtxStacks.get(hostId, cpu);
        if (stack == null) {
            stack = new Stack<>();
            fIntCtxStacks.put(hostId, cpu, stack);
        }
        stack.push(interruptCtx);
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
    public LttngInterruptContext peekContextStack(String hostId, Integer cpu) {
        Stack<LttngInterruptContext> stack = fIntCtxStacks.get(hostId, cpu);
        if (stack == null) {
            return LttngInterruptContext.DEFAULT_CONTEXT;
        }
        if (stack.empty()) {
            return LttngInterruptContext.DEFAULT_CONTEXT;
        }
        LttngInterruptContext peek = stack.peek();
        return peek;
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
    public @Nullable LttngInterruptContext popContextStack(String hostId, Integer cpu) {
        Stack<LttngInterruptContext> stack = fIntCtxStacks.get(hostId, cpu);
        if (stack == null) {
            return null;
        }
        if (stack.empty()) {
            return null;
        }
        return stack.pop();
    }

}
