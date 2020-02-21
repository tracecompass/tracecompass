/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.core.trace.layout;

import org.eclipse.tracecompass.lttng2.ust.core.trace.layout.ILttngUstEventLayout;

/**
 * Definitions can be used by other tracers if they want to output ctf format.
 *
 * @author Abderrahmane Benbachir
 */
@SuppressWarnings("nls")
public class DefaultUstEventLayout implements ILttngUstEventLayout {

    /**
     * Constructor
     */
    protected DefaultUstEventLayout() {}

    private static final DefaultUstEventLayout INSTANCE = new DefaultUstEventLayout();

    /**
     * Get a singleton instance.
     *
     * @return The instance
     */
    public static DefaultUstEventLayout getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------
    // Event names used for memory events
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
    // Event names used for function entry/exit
    // ------------------------------------------------------------------------

    @Override
    public String eventCygProfileFuncEntry() {
        return "func_entry";
    }

    @Override
    public String eventCygProfileFastFuncEntry() {
        return "fast:func_entry";
    }

    @Override
    public String eventCygProfileFuncExit() {
        return "func_exit";
    }

    @Override
    public String eventCygProfileFastFuncExit() {
        return "fast:func_exit";
    }

    // ------------------------------------------------------------------------
    // Event names used to track dynamic linking loader
    // ------------------------------------------------------------------------

    @Override
    public String eventDlOpen() {
        return "ust_dl:dlopen";
    }

    @Override
    public String eventDlClose() {
        return "ust_dl:dlclose";
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
    // Field names
    // Note: 'Context' word only kept for implementing the ILttngUstEventLayout
    // interface
    // ------------------------------------------------------------------------

    @Override
    public String contextVpid() {
        return "vpid";
    }

    @Override
    public String contextVtid() {
        return "vtid";
    }

    @Override
    public String contextProcname() {
        return "procname";
    }

    @Override
    public String contextIp() {
        return "ip";
    }
}
