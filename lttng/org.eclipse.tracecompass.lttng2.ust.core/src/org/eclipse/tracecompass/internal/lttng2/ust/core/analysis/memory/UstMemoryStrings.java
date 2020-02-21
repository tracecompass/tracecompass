/*******************************************************************************
 * Copyright (c) 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam, Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.memory;

/**
 * Strings for the memory usage state system using the LTTng UST libc
 * instrumentation
 *
 * @author Matthew Khouzam
 * @author Geneviève Bastien
 */
@SuppressWarnings("nls")
public interface UstMemoryStrings {

    /** Memory state system attribute name */
    String UST_MEMORY_MEMORY_ATTRIBUTE = "Memory";

    /** Procname state system attribute name */
    String UST_MEMORY_PROCNAME_ATTRIBUTE = "Procname";

    /** Name of the attribute to store memory usage of events with no context */
    String OTHERS = "Others";

}