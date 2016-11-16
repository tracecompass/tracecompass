/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.kernel;

import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;

/**
 * State values that are used in the kernel event handler. It's much better to
 * use integer values whenever possible, since those take much less space in the
 * history file.
 *
 * @author Alexandre Montplaisir
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 2.0
 */
@SuppressWarnings("javadoc")
public interface StateValues {

    /* Process status */
    @Deprecated
    int PROCESS_STATUS_UNKNOWN = 0;
    @Deprecated
    int PROCESS_STATUS_WAIT_BLOCKED = 1;
    @Deprecated
    int PROCESS_STATUS_RUN_USERMODE = 2;
    @Deprecated
    int PROCESS_STATUS_RUN_SYSCALL = 3;
    @Deprecated
    int PROCESS_STATUS_INTERRUPTED = 4;
    @Deprecated
    int PROCESS_STATUS_WAIT_FOR_CPU = 5;
    @Deprecated
    int PROCESS_STATUS_WAIT_UNKNOWN = 6;

    @Deprecated
    ITmfStateValue PROCESS_STATUS_UNKNOWN_VALUE = TmfStateValue.newValueInt(PROCESS_STATUS_UNKNOWN);
    @Deprecated
    ITmfStateValue PROCESS_STATUS_WAIT_UNKNOWN_VALUE = TmfStateValue.newValueInt(PROCESS_STATUS_WAIT_UNKNOWN);
    @Deprecated
    ITmfStateValue PROCESS_STATUS_WAIT_BLOCKED_VALUE = TmfStateValue.newValueInt(PROCESS_STATUS_WAIT_BLOCKED);
    @Deprecated
    ITmfStateValue PROCESS_STATUS_RUN_USERMODE_VALUE = TmfStateValue.newValueInt(PROCESS_STATUS_RUN_USERMODE);
    @Deprecated
    ITmfStateValue PROCESS_STATUS_RUN_SYSCALL_VALUE = TmfStateValue.newValueInt(PROCESS_STATUS_RUN_SYSCALL);
    @Deprecated
    ITmfStateValue PROCESS_STATUS_INTERRUPTED_VALUE = TmfStateValue.newValueInt(PROCESS_STATUS_INTERRUPTED);
    @Deprecated
    ITmfStateValue PROCESS_STATUS_WAIT_FOR_CPU_VALUE = TmfStateValue.newValueInt(PROCESS_STATUS_WAIT_FOR_CPU);

    /* CPU Status */
    int CPU_STATUS_IDLE = 0;
    /**
     * Soft IRQ raised, could happen in the CPU attribute but should not since
     * this means that the CPU went idle when a softirq was raised.
     */
    int CPU_STATUS_SOFT_IRQ_RAISED = (1 << 0);
    int CPU_STATUS_RUN_USERMODE = (1 << 1);
    int CPU_STATUS_RUN_SYSCALL = (1 << 2);
    int CPU_STATUS_SOFTIRQ = (1 << 3);
    int CPU_STATUS_IRQ = (1 << 4);

    ITmfStateValue CPU_STATUS_IDLE_VALUE = TmfStateValue.newValueInt(CPU_STATUS_IDLE);
    ITmfStateValue CPU_STATUS_RUN_USERMODE_VALUE = TmfStateValue.newValueInt(CPU_STATUS_RUN_USERMODE);
    ITmfStateValue CPU_STATUS_RUN_SYSCALL_VALUE = TmfStateValue.newValueInt(CPU_STATUS_RUN_SYSCALL);
    ITmfStateValue CPU_STATUS_IRQ_VALUE = TmfStateValue.newValueInt(CPU_STATUS_IRQ);
    ITmfStateValue CPU_STATUS_SOFTIRQ_VALUE = TmfStateValue.newValueInt(CPU_STATUS_SOFTIRQ);

    /** Soft IRQ is raised, CPU is in user mode */
    ITmfStateValue SOFT_IRQ_RAISED_VALUE = TmfStateValue.newValueInt(CPU_STATUS_SOFT_IRQ_RAISED);

    /** If the softirq is running and another is raised at the same time. */
    ITmfStateValue SOFT_IRQ_RAISED_RUNNING_VALUE = TmfStateValue.newValueInt(CPU_STATUS_SOFT_IRQ_RAISED | CPU_STATUS_SOFTIRQ);
}
