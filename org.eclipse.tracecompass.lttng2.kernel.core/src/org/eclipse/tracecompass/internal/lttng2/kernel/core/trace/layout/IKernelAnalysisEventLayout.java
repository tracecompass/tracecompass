/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout;

import java.util.Collection;

/**
 * Interface to define "concepts" present in the Linux kernel (represented by
 * its tracepoints), that can then be exposed by different tracers under
 * different names.
 *
 * @author Alexandre Montplaisir
 */
// The methods are named after the TRACE_EVENT's, should be straightforward
@SuppressWarnings("javadoc")
public interface IKernelAnalysisEventLayout {

    // ------------------------------------------------------------------------
    // Common definitions
    // ------------------------------------------------------------------------

    /**
     * Whenever a process appears for the first time in a trace, we assume it
     * starts inside this system call. (The syscall prefix is defined by the
     * implementer of this interface.)
     *
     * TODO Change to a default method with Java 8?
     */
    String INITIAL_SYSCALL_NAME = "clone"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Event names
    // ------------------------------------------------------------------------

    String eventIrqHandlerEntry();
    String eventIrqHandlerExit();
    String eventSoftIrqEntry();
    String eventSoftIrqExit();
    String eventSoftIrqRaise();
    String eventSchedSwitch();
    Collection<String> eventsSchedWakeup();
    String eventSchedProcessFork();
    String eventSchedProcessExit();
    String eventSchedProcessFree();
    String eventStatedumpProcessState();
    String eventSyscallEntryPrefix();
    String eventCompatSyscallEntryPrefix();
    String eventSyscallExitPrefix();

    // ------------------------------------------------------------------------
    // Event field names
    // ------------------------------------------------------------------------

    String fieldIrq();
    String fieldVec();
    String fieldTid();
    String fieldPrevTid();
    String fieldPrevState();
    String fieldNextComm();
    String fieldNextTid();
    String fieldChildComm();
    String fieldParentTid();
    String fieldChildTid();
}
