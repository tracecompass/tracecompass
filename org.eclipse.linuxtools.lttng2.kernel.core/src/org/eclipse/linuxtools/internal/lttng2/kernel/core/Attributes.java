/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
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
@SuppressWarnings({"nls", "javadoc"})
public interface Attributes {

    /* First-level attributes */
    static final String CPUS = "CPUs";
    static final String THREADS = "Threads";
    static final String RESOURCES = "Resources";

    /* Sub-attributes of the CPU nodes */
    static final String CURRENT_THREAD = "Current_thread";
    static final String STATUS = "Status";

    /* Sub-attributes of the Thread nodes */
    static final String PPID = "PPID";
    //static final String STATUS = "Status"
    static final String EXEC_NAME = "Exec_name";
    static final String SYSTEM_CALL = "System_call";

    /* Attributes under "Resources" */
    static final String IRQS = "IRQs";
    static final String SOFT_IRQS = "Soft_IRQs";

    /* Misc stuff */
    static final String UNKNOWN = "Unknown";
}
