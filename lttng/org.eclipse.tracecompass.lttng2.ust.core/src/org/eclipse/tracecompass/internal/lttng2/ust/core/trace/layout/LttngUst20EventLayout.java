/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.core.trace.layout;

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

    private static final LttngUst20EventLayout INSTANCE = new LttngUst20EventLayout();

    /**
     * Get a singleton instance.
     *
     * @return The instance
     */
    public static LttngUst20EventLayout getInstance() {
        return INSTANCE;
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

    // ------------------------------------------------------------------------
    // Context field names
    // Note: The CTF parser exposes contexts as fields called "context._<name>"
    // ------------------------------------------------------------------------

    @Override
    public String contextVtid() {
        return "context._vtid";
    }

    @Override
    public String contextProcname() {
        return "context._procname";
    }
}
