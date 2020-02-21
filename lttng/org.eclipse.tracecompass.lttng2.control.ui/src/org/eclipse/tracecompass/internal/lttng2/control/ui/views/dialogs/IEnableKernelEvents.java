/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs;

import java.util.List;

/**
 * <p>
 * Interface for providing information about kernel events to be enabled.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface IEnableKernelEvents {

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return a flag whether the tracepoints shall be configured.
     */
    boolean isTracepoints();

    /**
     * @return a flag indicating all tracepoints and syscall shall be enabled or not
     */
    boolean isAllEvents();

    /**
     * @return a flag indicating whether all tracepoints shall be enabled or not.
     */
    boolean isAllTracePoints();

    /**
     * @return a flag whether the syscalls shall be configured.
     */
    boolean isSyscalls();

    /**
     * @return a flag indicating whether syscalls shall be enabled or not.
     */
    boolean isAllSyscalls();

    /**
     * @return a list of event names to be enabled.
     */
    List<String> getEventNames();

    /**
     * @return a flag whether the dynamic probe shall be configured.
     */
    boolean isDynamicProbe();

    /**
     * @return event name of the dynamic probe (or null if no dynamic probe).
     */
    String getProbeEventName();

    /**
     * @return the dynamic probe (or null if no dynamic probe).
     */
    String getProbeName();

    /**
     * @return a flag whether the dynamic function entry/return probe shall be configured.
     */
    boolean isDynamicFunctionProbe();

    /**
     * @return event name of the dynamic function entry/exit probe (or null if no dynamic probe).
     */
    String getFunctionEventName();

    /**
     * @return the dynamic function entry/exit probe (or null if no dynamic probe).
     */
    String getFunction();

    /**
     * @return a filter expression
     */
    String getFilterExpression();
}