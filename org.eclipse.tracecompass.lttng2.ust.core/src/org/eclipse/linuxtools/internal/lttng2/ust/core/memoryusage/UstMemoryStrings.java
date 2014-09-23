/*******************************************************************************
 * Copyright (c) 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam, Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.ust.core.memoryusage;

/**
 * Strings for the memory usage state system using the LTTng UST libc
 * instrumentation
 *
 * @author Matthew Khouzam
 * @author Geneviève Bastien
 */
@SuppressWarnings({ "nls", "javadoc" })
public interface UstMemoryStrings {

    /** Memory state system attribute name */
    static final String UST_MEMORY_MEMORY_ATTRIBUTE = "Memory"; //$NON-NLS-1$
    /** Procname state system attribute name */
    static final String UST_MEMORY_PROCNAME_ATTRIBUTE = "Procname"; //$NON-NLS-1$
    /** Name of the attribute to store memory usage of events with no context */
    static final String OTHERS = "Others";

    /* UST_libc event names */
    static final String MALLOC = "ust_libc:malloc";
    static final String CALLOC = "ust_libc:calloc";
    static final String REALLOC = "ust_libc:realloc";
    static final String FREE = "ust_libc:free";
    static final String MEMALIGN = "ust_libc:memalign";
    static final String POSIX_MEMALIGN = "ust_libc:posix_memalign";

    /* Possible contexts */
    static final String CONTEXT_VTID = "context._vtid";
    static final String CONTEXT_PROCNAME = "context._procname";

    /* Event fields */
    static final String FIELD_PTR = "ptr";
    static final String FIELD_NMEMB = "nmemb";
    static final String FIELD_SIZE = "size";
    static final String FIELD_OUTPTR = "out_ptr";
    static final String FIELD_INPTR = "in_ptr";

}
