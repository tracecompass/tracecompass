/*******************************************************************************
 * Copyright (c) 2015 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.core;

/**
 * Strings related to UST traces and convenience libraries.
 *
 * @author Matthew Khouzam
 * @author Geneviève Bastien
 * @noimplement This interface only contains static definitions.
 */
@SuppressWarnings({ "nls", "javadoc" })
public interface LttngUstEventStrings {

    /* UST_libc event names */
    String MALLOC = "ust_libc:malloc";
    String CALLOC = "ust_libc:calloc";
    String REALLOC = "ust_libc:realloc";
    String FREE = "ust_libc:free";
    String MEMALIGN = "ust_libc:memalign";
    String POSIX_MEMALIGN = "ust_libc:posix_memalign";

    /* Possible contexts */
    String CONTEXT_VTID = "context._vtid";
    String CONTEXT_PROCNAME = "context._procname";

    /* Event fields */
    String FIELD_PTR = "ptr";
    String FIELD_NMEMB = "nmemb";
    String FIELD_SIZE = "size";
    String FIELD_OUTPTR = "out_ptr";
    String FIELD_INPTR = "in_ptr";

}
