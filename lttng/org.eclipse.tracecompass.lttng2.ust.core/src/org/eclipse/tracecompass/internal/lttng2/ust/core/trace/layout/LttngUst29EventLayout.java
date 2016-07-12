/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.core.trace.layout;

/**
 * Updated event definitions for LTTng-UST 2.9.
 *
 * @author Alexandre Montplaisir
 */
@SuppressWarnings("nls")
public class LttngUst29EventLayout extends LttngUst28EventLayout {

    /**
     * Constructor
     */
    protected LttngUst29EventLayout() {}

    private static final LttngUst29EventLayout INSTANCE = new LttngUst29EventLayout();

    /**
     * Get a singleton instance.
     *
     * @return The instance
     */
    public static LttngUst29EventLayout getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------
    // Library load/unload events
    // ------------------------------------------------------------------------

    /*
     * With LTTng 2.9, it is now recommended to use the load/unload events
     * instead of the dlopen/dlclose ones to track libraries loading and
     * unloading into the process space. The library now does the reference
     * counting for us. The event fields are exactly the same.
     *
     * See https://bugs.lttng.org/issues/1035 and related commits.
     */

    @Override
    public String eventDlOpen() {
        return "lttng_ust_lib:load";
    }

    @Override
    public String eventDlClose() {
        return "lttng_ust_lib:unload";
    }

    @Override
    public String eventDlBuildId() {
        return "lttng_ust_lib:build_id";
    }

    @Override
    public String eventDlDebugLink() {
        return "lttng_ust_lib:debug_link";
    }

}
