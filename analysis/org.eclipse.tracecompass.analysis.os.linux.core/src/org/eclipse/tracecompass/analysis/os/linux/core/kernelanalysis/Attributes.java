/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis;

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
    String CPUS = "CPUs";
    String THREADS = "Threads";

    /* Sub-attributes of the CPU nodes */
    String CURRENT_THREAD = "Current_thread";
    String STATUS = "Status";
    String SOFT_IRQS = "Soft_IRQs";
    String IRQS = "IRQs";

    /* Sub-attributes of the Thread nodes */
    String PPID = "PPID";
    //static final String STATUS = "Status"
    String EXEC_NAME = "Exec_name";

    /** @since 1.0 */
    String PRIO = "Prio";
    String SYSTEM_CALL = "System_call";

    /* Misc stuff */
    String UNKNOWN = "Unknown";
}
