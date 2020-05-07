/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.core.trace.layout;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.lttng2.ust.core.trace.layout.ILttngUstEventLayout;

/**
 * Definitions used in LTTng-UST for versions 2.0 up to 2.6.
 *
 * @author Alexandre Montplaisir
 */
@SuppressWarnings("nls")
public class LttngUst20EventLayout implements ILttngUstEventLayout {

    /**
     * Constructor
     */
    protected LttngUst20EventLayout() {}

    private static @Nullable LttngUst20EventLayout INSTANCE = null;

    /**
     * Get a singleton instance.
     *
     * @return The instance
     */
    public static synchronized LttngUst20EventLayout getInstance() {
        LttngUst20EventLayout instance = INSTANCE;
        if (instance == null) {
            instance = new LttngUst20EventLayout();
            INSTANCE = instance;
        }
        return instance;
    }

    // ------------------------------------------------------------------------
    // Event names used in liblttng-ust-libc-wrapper
    // ------------------------------------------------------------------------

    @Override
    public String eventLibcMalloc() {
        return "ust_libc:malloc";
    }

    @Override
    public String eventLibcCalloc() {
        return "ust_libc:calloc";
    }

    @Override
    public String eventLibcRealloc() {
        return "ust_libc:realloc";
    }

    @Override
    public String eventLibcFree() {
        return "ust_libc:free";
    }

    @Override
    public String eventLibcMemalign() {
        return "ust_libc:memalign";
    }

    @Override
    public String eventLibcPosixMemalign() {
        return "ust_libc:posix_memalign";
    }

    // ------------------------------------------------------------------------
    // Event names used in liblttng-cyg-profile
    // ------------------------------------------------------------------------

    @Override
    public String eventCygProfileFuncEntry() {
        return "lttng_ust_cyg_profile:func_entry";
    }

    @Override
    public String eventCygProfileFastFuncEntry() {
        return "lttng_ust_cyg_profile_fast:func_entry";
    }

    @Override
    public String eventCygProfileFuncExit() {
        return "lttng_ust_cyg_profile:func_exit";
    }

    @Override
    public String eventCygProfileFastFuncExit() {
        return "lttng_ust_cyg_profile_fast:func_exit";
    }

    // ------------------------------------------------------------------------
    // Event names used in liblttng-ust-dl
    // ------------------------------------------------------------------------

    @Override
    public String eventDlOpen() {
        return "lttng_ust_dl:dlopen";
    }

    @Override
    public String eventDlClose() {
        return "lttng_ust_dl:dlclose";
    }

    // ------------------------------------------------------------------------
    // Field names
    // ------------------------------------------------------------------------

    @Override
    public String fieldPtr() {
        return "ptr";
    }

    @Override
    public String fieldNmemb() {
        return "nmemb";
    }

    @Override
    public String fieldSize() {
        return "size";
    }

    @Override
    public String fieldOutPtr() {
        return "out_ptr";
    }

    @Override
    public String fieldInPtr() {
        return "in_ptr";
    }

    @Override
    public String fieldAddr() {
        return "addr";
    }

    // ------------------------------------------------------------------------
    // Context field names
    // Note: The CTF parser exposes contexts as fields called "context._<name>"
    // ------------------------------------------------------------------------

    @Override
    public String contextVpid() {
        return "context._vpid";
    }

    @Override
    public String contextVtid() {
        return "context._vtid";
    }

    @Override
    public String contextProcname() {
        return "context._procname";
    }

    @Override
    public String contextIp() {
        return "context._ip";
    }
}
