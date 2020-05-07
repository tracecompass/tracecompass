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

/**
 * Updated event definitions for LTTng-UST 2.8.
 *
 * @author Alexandre Montplaisir
 */
@SuppressWarnings({"javadoc", "nls"})
public class LttngUst28EventLayout extends LttngUst27EventLayout {

    /**
     * Constructor
     */
    protected LttngUst28EventLayout() {}

    private static @Nullable LttngUst28EventLayout INSTANCE = null;

    /**
     * Get a singleton instance.
     *
     * @return The instance
     */
    public static synchronized LttngUst28EventLayout getInstance() {
        LttngUst28EventLayout instance = INSTANCE;
        if (instance == null) {
            instance = new LttngUst28EventLayout();
            INSTANCE = instance;
        }
        return instance;
    }

    // ------------------------------------------------------------------------
    // UST Statedump events
    // ------------------------------------------------------------------------

    public String eventStatedumpStart() {
        return "lttng_ust_statedump:start";
    }

    public String eventStatedumpEnd() {
        return "lttng_ust_statedump:end";
    }

    public String eventStatedumpBinInfo() {
        return "lttng_ust_statedump:bin_info";
    }

    public String eventStateDumpBuildId() {
        return "lttng_ust_statedump:build_id";
    }

    public String eventStateDumpDebugLink() {
        return "lttng_ust_statedump:debug_link";
    }

    // ------------------------------------------------------------------------
    // Additional liblttng-ust-dl events
    // ------------------------------------------------------------------------

    public String eventDlBuildId() {
        return "lttng_ust_dl:build_id";
    }

    public String eventDlDebugLink() {
        return "lttng_ust_dl:debug_link";
    }

    // ------------------------------------------------------------------------
    // Field names used by statedump and liblttng-ust-dl events
    // (only supported for 2.8+ traces)
    // ------------------------------------------------------------------------

    public String fieldBaddr() {
        return "baddr";
    }

    public String fieldMemsz() {
        return "memsz";
    }

    public String fieldPath() {
        return "path";
    }

    public String fieldHasBuildId() {
        return "has_build_id";
    }

    public String fieldHasDebugLink() {
        return "has_debug_link";
    }

    public String fieldBuildId() {
        return "build_id";
    }

    public String fieldDebugLinkFilename() {
        return "filename";
    }

    public String fieldIsPic() {
        return "is_pic";
    }
}
