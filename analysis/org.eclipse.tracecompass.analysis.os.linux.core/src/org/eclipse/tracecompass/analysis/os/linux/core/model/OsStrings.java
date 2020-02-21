/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.model;

import java.util.Objects;

import org.eclipse.tracecompass.tmf.core.TmfStrings;

/**
 * Get externalized strings to identify various operating system concepts. These
 * strings can be used in views and analyses to have a common term to identify
 * often used concepts, or as keys for searches and filters by the user.
 *
 * These strings are meant to be used and viewed by the user. Internally, they
 * may be translated into fixed non-externalized strings for actual
 * manipulation. Example: views will display TID in english, SvQQ in klingon,
 * searches for a thread ID would be written TID == 123 in english, SvQQ == 123
 * in klingon or eventually thread.tid in non-externalized (but maybe less
 * discoverable) concepts.
 *
 * @author Geneviève Bastien
 * @since 4.0
 */
public final class OsStrings {

    private OsStrings()  {
        // Do nothing
    }

    /**
     * Get the string for the thread ID
     *
     * @return The externalized label for thread ID
     */
    public static String tid() {
        return Objects.requireNonNull(org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.Messages.AspectName_Tid);
    }

    /**
     * Get the string for the process ID
     *
     * @return The externalized label for process ID
     */
    public static String pid() {
        return Objects.requireNonNull(org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.Messages.AspectName_Pid);
    }

    /**
     * Get the string for the parent's thread ID
     *
     * @return The externalized label for parent's thread ID
     * @since 4.2
     */
    public static String ptid() {
        return Objects.requireNonNull(org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.Messages.AspectName_Ptid);
    }

    /**
     * Get the string for the cpu string
     *
     * @return The externalized label for CPU
     * @deprecated use {@link TmfStrings#cpu}
     */
    @Deprecated
    public static String cpu() {
        return TmfStrings.cpu();
    }

    /**
     * Get the externalized string for the executable name of a thread
     *
     * @return The externalized label for exec name
     * @since 4.1
     */
    public static String execName() {
        return Objects.requireNonNull(org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.Messages.AspectName_ExecName);
    }

}
