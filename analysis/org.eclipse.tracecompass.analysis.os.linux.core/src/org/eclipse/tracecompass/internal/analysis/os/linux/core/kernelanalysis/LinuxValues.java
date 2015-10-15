/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis;

/**
 * Definitions of values used in the Linux kernel code.
 *
 * Instead of using "magic numbers" in state providers, the definitions should
 * be added here first.
 *
 * @author Alexandre Montplaisir
 */
@SuppressWarnings("javadoc")
public interface LinuxValues {

    /**
     * Process states found in scheduler events.
     *
     * From include/linux/sched.h
     *
     * <pre>
     * #define TASK_RUNNING 0
     * #define TASK_INTERRUPTIBLE 1
     * #define TASK_UNINTERRUPTIBLE 2
     * #define __TASK_STOPPED 4
     * #define __TASK_TRACED 8
     * #define EXIT_DEAD 16
     * #define EXIT_ZOMBIE 32
     * #define EXIT_TRACE (EXIT_ZOMBIE | EXIT_DEAD)
     * #define TASK_DEAD 64
     * #define TASK_WAKEKILL 128
     * #define TASK_WAKING 256
     * #define TASK_PARKED 512
     * #define TASK_STATE_MAX 1024
     * </pre>
     */
    /**
     * The task is running normally, can be interrupted, in a syscall or user
     * mode.
     */
    int TASK_STATE_RUNNING = 0;

    int TASK_INTERRUPTIBLE = 1;

    int TASK_UNINTERRUPTIBLE = 2;

    /**
     * The task is dead, that means the PID can be re-used.
     */
    int TASK_DEAD = 64;

    /**
     * This is the maximum value + 1 that the task state can be. TASK_STATE_MAX
     * - 1 is useful to mask the task state.
     */
    int TASK_STATE_MAX = 1024;

    /**
     * Process statuses, used in LTTng statedump events.
     *
     * This is LTTng-specific, but the statedump are handled at this level, so
     * it makes sense to add those definitions here.
     *
     * Taken from lttng-module's lttng-statedump-impl.c:
     *
     * <pre>
     * enum lttng_process_status {
     *     LTTNG_UNNAMED = 0,
     *     LTTNG_WAIT_FORK = 1,
     *     LTTNG_WAIT_CPU = 2,
     *     LTTNG_EXIT = 3,
     *     LTTNG_ZOMBIE = 4,
     *     LTTNG_WAIT = 5,
     *     LTTNG_RUN = 6,
     *     LTTNG_DEAD = 7,
     * };
     * </pre>
     */

    /** Task is initially preempted */
    int STATEDUMP_PROCESS_STATUS_WAIT_CPU = 2;

    /** Task is initially blocked */
    int STATEDUMP_PROCESS_STATUS_WAIT = 5;
}
