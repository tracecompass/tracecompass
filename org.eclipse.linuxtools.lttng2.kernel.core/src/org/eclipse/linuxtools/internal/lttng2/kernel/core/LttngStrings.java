/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
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
 */
@SuppressWarnings({"javadoc", "nls"})
public interface LttngStrings {

    /* Event names */
    static final String EXIT_SYSCALL = "exit_syscall";
    static final String IRQ_HANDLER_ENTRY = "irq_handler_entry";
    static final String IRQ_HANDLER_EXIT = "irq_handler_exit";
    static final String SOFTIRQ_ENTRY = "softirq_entry";
    static final String SOFTIRQ_EXIT = "softirq_exit";
    static final String SOFTIRQ_RAISE = "softirq_raise";
    static final String SCHED_SWITCH = "sched_switch";
    static final String SCHED_WAKEUP = "sched_wakeup";
    static final String SCHED_WAKEUP_NEW = "sched_wakeup_new";
    static final String SCHED_PROCESS_FORK = "sched_process_fork";
    static final String SCHED_PROCESS_EXIT = "sched_process_exit";
    static final String SCHED_PROCESS_FREE = "sched_process_free";
    static final String STATEDUMP_PROCESS_STATE = "lttng_statedump_process_state";

    /* System call names */
    static final String SYSCALL_PREFIX = "sys_";
    static final String COMPAT_SYSCALL_PREFIX = "compat_sys_";
    static final String SYS_CLONE = "sys_clone";

    /* Field names */
    static final String IRQ = "irq";
    static final String COMM = "comm";
    static final String NAME = "name";
    static final String PID = "pid";
    static final String TID = "tid";
    static final String PPID = "ppid";
    static final String STATUS = "status";
    static final String VEC = "vec";
    static final String PREV_COMM = "prev_comm";
    static final String PREV_TID = "prev_tid";
    static final String PREV_STATE = "prev_state";
    static final String NEXT_COMM = "next_comm";
    static final String NEXT_TID = "next_tid";
    static final String PARENT_TID = "parent_tid";
    static final String CHILD_COMM = "child_comm";
    static final String CHILD_TID = "child_tid";
}
