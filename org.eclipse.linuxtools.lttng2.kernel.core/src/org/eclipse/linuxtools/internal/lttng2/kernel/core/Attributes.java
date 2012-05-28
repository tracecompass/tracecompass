/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.core;

/**
 * This file defines all the attribute names used in the handler. Both the
 * construction and query steps should use them.
 * 
 * These should not be externalized! The values here are used as-is in the
 * history file on disk, so they should be kept the same to keep the file format
 * compatible. If a view shows attribute names directly, the localization should
 * be done on the viewer side.
 * 
 * @author alexmont
 * 
 */
@SuppressWarnings("nls")
public abstract class Attributes {

    /* First-level attributes */
    public static final String CPUS = "CPUs";
    public static final String THREADS = "Threads";
    public static final String RESOURCES = "Resources";

    /* Sub-attributes of the CPU nodes */
    public static final String CURRENT_THREAD = "Current_thread";
    public static final String STATUS = "Status";

    /* Sub-attributes of the Thread nodes */
    public static final String PPID = "PPID";
    //public static final String STATUS = "Status"
    public static final String EXEC_NAME = "Exec_name";
    public static final String SYSTEM_CALL = "System_call";

    /* Attributes under "Resources" */
    public static final String IRQS = "IRQs";
    public static final String SOFT_IRQS = "Soft_IRQs";

    /* 
     * Statistics sub-nodes
     * (Written all out, because "Stats" is easy to confuse with "Status")
     */
    public static final String STATISTICS = "Stats";
    public static final String EVENT_TYPES = "Event_types";

    /* Misc stuff */
    public static final String UNKNOWN = "Unknown";
}
