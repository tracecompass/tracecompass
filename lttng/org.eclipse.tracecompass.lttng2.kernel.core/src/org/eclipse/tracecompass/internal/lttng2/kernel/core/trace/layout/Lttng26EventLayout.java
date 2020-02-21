/*******************************************************************************
 * Copyright (c) 2014 Ericsson
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

package org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout;

/**
 * This file defines all the known event and field names for LTTng kernel
 * traces, for versions of lttng-modules 2.6 and above.
 *
 * @author Alexandre Montplaisir
 */
public class Lttng26EventLayout extends LttngEventLayout {

    /**
     * Constructor
     */
    protected Lttng26EventLayout() {}

    private static final Lttng26EventLayout INSTANCE = new Lttng26EventLayout();

    public static Lttng26EventLayout getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------
    // New event names in these versions
    // ------------------------------------------------------------------------

    @Override
    public String eventSyscallEntryPrefix() {
        return "syscall_entry_"; //$NON-NLS-1$
    }

    @Override
    public String eventCompatSyscallEntryPrefix() {
        return "compat_syscall_entry_"; //$NON-NLS-1$
    }

    @Override
    public String eventSyscallExitPrefix() {
        return "syscall_exit_"; //$NON-NLS-1$
    }

    @Override
    public String eventCompatSyscallExitPrefix() {
        return "compat_syscall_exit_"; //$NON-NLS-1$
    }
}
