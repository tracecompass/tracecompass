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

/**
 * Updated event definitions for LTTng-UST 2.7.
 *
 * @author Alexandre Montplaisir
 */
@SuppressWarnings("nls")
public class LttngUst27EventLayout extends LttngUst20EventLayout {

    /**
     * Constructor
     */
    protected LttngUst27EventLayout() {}

    private static final LttngUst27EventLayout INSTANCE = new LttngUst27EventLayout();

    /**
     * Get a singleton instance.
     *
     * @return The instance
     */
    public static synchronized LttngUst27EventLayout getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------
    // Event names used in liblttng-ust-libc-wrapper
    // They are now prefixed with "lttng_*"
    // ------------------------------------------------------------------------

    @Override
    public String eventLibcMalloc() {
        return "lttng_ust_libc:malloc";
    }

    @Override
    public String eventLibcCalloc() {
        return "lttng_ust_libc:calloc";
    }

    @Override
    public String eventLibcRealloc() {
        return "lttng_ust_libc:realloc";
    }

    @Override
    public String eventLibcFree() {
        return "lttng_ust_libc:free";
    }

    @Override
    public String eventLibcMemalign() {
        return "lttng_ust_libc:memalign";
    }

    @Override
    public String eventLibcPosixMemalign() {
        return "lttng_ust_libc:posix_memalign";
    }
}
