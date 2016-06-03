/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.model;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * This class represents a virtual CPU, which is a CPU running on a guest. It
 * associates the guest CPU ID to a virtual machine of the model.
 *
 * @author Geneviève Bastien
 */
public final class VirtualCPU {

    private static final Table<VirtualMachine, Long, @Nullable VirtualCPU> VIRTUAL_CPU_TABLE = HashBasedTable.create();

    private final VirtualMachine fVm;
    private final Long fCpuId;

    /**
     * Return the virtual CPU for to the virtual machine and requested CPU ID
     *
     * @param vm
     *            The virtual machine
     * @param cpu
     *            the CPU number
     * @return the virtual CPU
     */
    public static synchronized VirtualCPU getVirtualCPU(VirtualMachine vm, Long cpu) {
        VirtualCPU ht = VIRTUAL_CPU_TABLE.get(vm, cpu);
        if (ht == null) {
            ht = new VirtualCPU(vm, cpu);
            VIRTUAL_CPU_TABLE.put(vm, cpu, ht);
        }
        return ht;
    }

    private VirtualCPU(VirtualMachine vm, Long cpu) {
        fVm = vm;
        fCpuId = cpu;
    }

    /**
     * Get the CPU ID of this virtual CPU
     *
     * @return The zero-based CPU ID
     */
    public Long getCpuId() {
        return fCpuId;
    }

    /**
     * Get the virtual machine object this virtual CPU belongs to
     *
     * @return The guest Virtual Machine
     */
    public VirtualMachine getVm() {
        return fVm;
    }

    @Override
    public String toString() {
        return "VirtualCPU: [" + fVm + ',' + fCpuId + ']'; //$NON-NLS-1$
    }

}