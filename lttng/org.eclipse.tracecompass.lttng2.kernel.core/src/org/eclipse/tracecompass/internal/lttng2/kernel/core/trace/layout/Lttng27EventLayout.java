/*******************************************************************************
 * Copyright (c) 2015 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Sebastien Lorrain - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout;

/**
 * This file defines all the known event and field names for LTTng kernel
 * traces, for versions of lttng-modules 2.7 and above.
 *
 * @author Sebastien Lorrain
 */
@SuppressWarnings("javadoc")
public class Lttng27EventLayout extends Lttng26EventLayout {

    /**
     * Constructor
     */
    protected Lttng27EventLayout() {}

    public static final Lttng27EventLayout INSTANCE = new Lttng27EventLayout();

    // ------------------------------------------------------------------------
    // New event definition in LTTng 2.7
    // ------------------------------------------------------------------------

    @Override
    public String eventHRTimerStart() {
        return "timer_hrtimer_start"; //$NON-NLS-1$
    }

    @Override
    public String eventHRTimerCancel() {
        return "timer_hrtimer_cancel"; //$NON-NLS-1$
    }

    @Override
    public String eventHRTimerExpireEntry() {
        return "timer_hrtimer_expire_entry"; //$NON-NLS-1$
    }

    @Override
    public String eventHRTimerExpireExit() {
        return "timer_hrtimer_expire_exit"; //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // New field definitions in LTTng 2.7
    // ------------------------------------------------------------------------

    public String fieldParentNSInum() {
        return "parent_ns_inum"; //$NON-NLS-1$
    }

    public String fieldChildNSInum() {
        return "child_ns_inum"; //$NON-NLS-1$
    }

    public String fieldChildVTids() {
        return "vtids"; //$NON-NLS-1$
    }

    public String fieldNSInum() {
        return "ns_inum"; //$NON-NLS-1$
    }

    public String fieldVTid() {
        return "vtid"; //$NON-NLS-1$
    }

    public String fieldPPid() {
        return "ppid"; //$NON-NLS-1$
    }

    public String fieldNSLevel() {
        return "ns_level"; //$NON-NLS-1$
    }

}
