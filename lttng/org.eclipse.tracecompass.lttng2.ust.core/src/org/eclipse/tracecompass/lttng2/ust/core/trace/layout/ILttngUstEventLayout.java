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

package org.eclipse.tracecompass.lttng2.ust.core.trace.layout;

import org.eclipse.tracecompass.internal.lttng2.ust.core.trace.layout.LttngUst20EventLayout;

/**
 * This interface defines concepts exposed by liblttng-ust libraries and traces.
 * Actual implementations can differ between different versions of the tracer.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
@SuppressWarnings("javadoc")
public interface ILttngUstEventLayout {

    /** The standard layout */
    ILttngUstEventLayout DEFAULT_LAYOUT = LttngUst20EventLayout.getInstance();

    // ------------------------------------------------------------------------
    // Event names
    // ------------------------------------------------------------------------

    /* liblttng-ust-libc-wrapper events */
    String eventLibcMalloc();
    String eventLibcCalloc();
    String eventLibcRealloc();
    String eventLibcFree();
    String eventLibcMemalign();
    String eventLibcPosixMemalign();

    /* liblttng-ust-dl events */
    String eventDlOpen();
    String eventDlClose();

    /* liblttng-ust-cyg-profile(-fast) events */
    String eventCygProfileFuncEntry();
    String eventCygProfileFastFuncEntry();
    String eventCygProfileFuncExit();
    String eventCygProfileFastFuncExit();

    // ------------------------------------------------------------------------
    // Field names
    // ------------------------------------------------------------------------

    String fieldPtr();
    String fieldNmemb();
    String fieldSize();
    String fieldOutPtr();
    String fieldInPtr();

    String fieldAddr();

    // ------------------------------------------------------------------------
    // Context field names
    // ------------------------------------------------------------------------

    String contextVpid();
    String contextVtid();
    String contextProcname();
    String contextIp();
}
