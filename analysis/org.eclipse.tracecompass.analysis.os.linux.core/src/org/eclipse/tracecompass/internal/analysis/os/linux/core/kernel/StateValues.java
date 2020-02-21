/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel;

import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;

/**
 * State values that are used in the kernel event handler. It's much better to
 * use integer values whenever possible, since those take much less space in the
 * history file.
 *
 * Here is the standard Linux Process model. Each state in this state machine
 * corresponds to a status in the state system.
 *
 * <pre>
 * @startuml
 *
 * state Preempted {
 *    state "wait for cpu" as wcpu
 *    state "wait for fork"
 *    state "wait for blocked" as wblock
 *    wblock -> wcpu : sched_switch
 * }
 *
 * state Running {
 *   User --> Kernel : syscall
 *   User --> Kernel : interrupt
 *   Kernel --> User : return
 *   Kernel --> Kernel : interrupt
 * }
 *
 * [*] --> Created : fork()
 *
 * Created -> Preempted
 *
 * Kernel --> zombie : exit()
 *
 * Kernel --> Sleep : sleep
 * Sleep --> Preempted : wakeup
 * Kernel --> Preempted : preempt
 * Preempted --> User : return
 *
 * @enduml
 * </pre>
 *
 * @author Alexandre Montplaisir
 */
public interface StateValues {

    /**
     * Unknown state.
     */
    int PROCESS_STATUS_UNKNOWN = 0;
    /**
     * Wait blocked, corresponds to the 'Sleep' state
     */
    int PROCESS_STATUS_WAIT_BLOCKED = 1;
    /**
     * Run Usermode, corresponds to the 'User' state
     */
    int PROCESS_STATUS_RUN_USERMODE = 2;
    /**
     * Run syscall, corresponds to the 'Kernel' state
     */
    int PROCESS_STATUS_RUN_SYSCALL = 3;
    /**
     * Interrupted, not in the state machine, it's part of the CPU context
     */
    int PROCESS_STATUS_INTERRUPTED = 4;
    /**
     * Waiting for CPU, corresponds to the 'Preempted' state
     */
    int PROCESS_STATUS_WAIT_FOR_CPU = 5;
    /**
     * Unknown wait state, not part of the state machine
     */
    int PROCESS_STATUS_WAIT_UNKNOWN = 6;
    /** Waiting for CPU after being forked -> a statedump state */
    int PROCESS_STATUS_WAIT_FORK = 7;

    /**
     * Unknown state {@link #PROCESS_STATUS_UNKNOWN}
     */
    ITmfStateValue PROCESS_STATUS_UNKNOWN_VALUE = TmfStateValue.newValueInt(PROCESS_STATUS_UNKNOWN);
    /**
     * Wait for unknown reason state value
     * {@link #PROCESS_STATUS_WAIT_UNKNOWN}
     */
    ITmfStateValue PROCESS_STATUS_WAIT_UNKNOWN_VALUE = TmfStateValue.newValueInt(PROCESS_STATUS_WAIT_UNKNOWN);
    /**
     * Wait blocked state value {@link #PROCESS_STATUS_WAIT_BLOCKED}
     */
    ITmfStateValue PROCESS_STATUS_WAIT_BLOCKED_VALUE = TmfStateValue.newValueInt(PROCESS_STATUS_WAIT_BLOCKED);
    /**
     * Run in usermode state value {@link #PROCESS_STATUS_RUN_USERMODE}
     */
    ITmfStateValue PROCESS_STATUS_RUN_USERMODE_VALUE = TmfStateValue.newValueInt(PROCESS_STATUS_RUN_USERMODE);
    /**
     * Run in kernel mode state value {@link #PROCESS_STATUS_RUN_SYSCALL}
     */
    ITmfStateValue PROCESS_STATUS_RUN_SYSCALL_VALUE = TmfStateValue.newValueInt(PROCESS_STATUS_RUN_SYSCALL);
    /**
     * Interrupted state value {@link #PROCESS_STATUS_INTERRUPTED}
     */
    ITmfStateValue PROCESS_STATUS_INTERRUPTED_VALUE = TmfStateValue.newValueInt(PROCESS_STATUS_INTERRUPTED);
    /**
     * Wait for CPU state value {@link #PROCESS_STATUS_WAIT_FOR_CPU}
     */
    ITmfStateValue PROCESS_STATUS_WAIT_FOR_CPU_VALUE = TmfStateValue.newValueInt(PROCESS_STATUS_WAIT_FOR_CPU);
    /** Wait for CPU after a fork state value {@link #PROCESS_STATUS_WAIT_FORK} */
    ITmfStateValue PROCESS_STATUS_WAIT_FORK_VALUE = TmfStateValue.newValueInt(PROCESS_STATUS_WAIT_FORK);

}
