/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.core;

/**
 * This file defines all the known event and field names for LTTng 2.0 kernel
 * traces.
 *
 * Once again, these should not be externalized, since they need to match
 * exactly what the tracer outputs. If you want to localize them in a view, you
 * should do a mapping in the viewer itself.
 *
 * @author alexmont
 *
 */
@SuppressWarnings({"javadoc", "nls"})
public abstract class LttngStrings {

    /* Event names */
    public static final String EXIT_SYSCALL = "exit_syscall";
    public static final String IRQ_HANDLER_ENTRY = "irq_handler_entry";
    public static final String IRQ_HANDLER_EXIT = "irq_handler_exit";
    public static final String SOFTIRQ_ENTRY = "softirq_entry";
    public static final String SOFTIRQ_EXIT = "softirq_exit";
    public static final String SOFTIRQ_RAISE = "softirq_raise";
    public static final String SCHED_SWITCH = "sched_switch";
    public static final String SCHED_PROCESS_FORK = "sched_process_fork";
    public static final String SCHED_PROCESS_EXIT = "sched_process_exit";
    public static final String SCHED_PROCESS_FREE = "sched_process_free";

    public static final String SYSCALL_PREFIX = "sys_";
    public static final String COMPAT_SYSCALL_PREFIX = "compat_sys_";

    /* Field names */
    public static final String IRQ = "irq";
    public static final String COMM = "comm";
    public static final String TID = "tid";
    public static final String VEC = "vec";
    public static final String PREV_COMM = "prev_comm";
    public static final String PREV_TID = "prev_tid";
    public static final String PREV_STATE = "prev_state";
    public static final String NEXT_COMM = "next_comm";
    public static final String NEXT_TID = "next_tid";
    public static final String PARENT_TID = "parent_tid";
    public static final String CHILD_COMM = "child_comm";
    public static final String CHILD_TID = "child_tid";
}
