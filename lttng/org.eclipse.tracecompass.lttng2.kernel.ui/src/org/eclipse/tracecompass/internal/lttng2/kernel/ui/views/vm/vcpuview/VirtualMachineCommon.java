/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.ui.views.vm.vcpuview;

/**
 * Provides some common elements for all virtual machine views
 *
 * @author Mohamad Gebai
 */
public final class VirtualMachineCommon {

    /** Type of resources/entries */
    public static enum Type {
        /** Entries for VMs */
        VM,
        /** Entries for VCPUs */
        VCPU,
        /** Entries for Threads */
        THREAD,
        /** Null resources (filler rows, etc.) */
        NULL
    }
}
