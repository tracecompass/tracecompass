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
 * State values that are used in the kernel event handler. It's much better to
 * use integer values whenever possible, since those take much less space in the
 * history file.
 *
 * @author alexmont
 *
 */
@SuppressWarnings("javadoc")
public class StateValues {

    /* CPU Status */
    public static final int CPU_STATUS_IDLE = 0;
    public static final int CPU_STATUS_RUN_USERMODE = 1;
    public static final int CPU_STATUS_RUN_SYSCALL = 2;
    public static final int CPU_STATUS_IRQ = 3;
    public static final int CPU_STATUS_SOFTIRQ = 4;

    /* Process status */
    public static final int PROCESS_STATUS_WAIT = 1;
    public static final int PROCESS_STATUS_RUN_USERMODE = 2;
    public static final int PROCESS_STATUS_RUN_SYSCALL = 3;
    public static final int PROCESS_STATUS_INTERRUPTED = 4;

    /* SoftIRQ-specific stuff. -1: null/disabled, >= 0: running on that CPU */
    public static final int SOFT_IRQ_RAISED = -2;
}
