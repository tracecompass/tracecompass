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

/**
 * This class represents a machine, host or guest, in a virtual machine model. A
 * machine is identified by a trace's host ID.
 *
 * @author Geneviève Bastien
 */
public final class VirtualMachine {

    private static enum MachineType {
        HOST,
        GUEST
    }

    private final long fVmUid;
    private final String fHostId;
    private final MachineType fType;

    /**
     * Create a new host machine. A host is a physical machine that may contain
     * virtual guest machines.
     *
     * @param hostId
     *            The host ID of the trace(s) this machine represents
     * @return A {@link VirtualMachine} of type host
     */
    public static VirtualMachine newHostMachine(String hostId) {
        return new VirtualMachine(MachineType.HOST, hostId, -1);
    }

    /**
     * Create a new guest machine. A guest is a virtual machine with virtual
     * CPUs running on a host.
     *
     * @param uid
     *            Some unique identifier of this guest machine that can be used
     *            in both the guest and the host to match both machines.
     * @param hostId
     *            The host ID of the trace(s) this machine represents
     * @return A {@link VirtualMachine} of type guest.
     */
    public static VirtualMachine newGuestMachine(long uid, String hostId) {
        return new VirtualMachine(MachineType.GUEST, hostId, uid);
    }

    private VirtualMachine(MachineType type, String hostId, long uid) {
        fType = type;
        fVmUid = uid;
        fHostId = hostId;
    }

    /**
     * Return whether this machine is a guest or a host
     *
     * @return {@code true} if the machine is a guest, or {@code false} if it is
     *         a host
     */
    public boolean isGuest() {
        return fType == MachineType.GUEST;
    }

    /**
     * Get the unique identifier that is used between the host and the guest to
     * identify this machine.
     *
     * @return The Virtual Machine unique ID.
     */
    public long getVmUid() {
        return fVmUid;
    }

    /**
     * Get the host ID of this machine
     *
     * @return The host ID of this machine
     */
    public String getHostId() {
        return fHostId;
    }

    @Override
    public String toString() {
        return "VirtualMachine: " + fHostId; //$NON-NLS-1$
    }

}