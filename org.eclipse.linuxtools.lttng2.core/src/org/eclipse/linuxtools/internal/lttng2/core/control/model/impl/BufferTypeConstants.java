/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Simon Delisle - Initial API and implementation
 **********************************************************************/

package org.eclipse.linuxtools.internal.lttng2.core.control.model.impl;

/**
 * Constants for buffer type.
 *
 * @author Simon Delisle
 */
public interface BufferTypeConstants {
    // ------------------------------------------------------------------------
    // Buffer type
    // ------------------------------------------------------------------------
    /**
     * Buffer type : per UID
     */
    static final String BUFFER_PER_UID = "per UID"; //$NON-NLS-1$
    /**
     * Buffer type : per PID
     */
    static final String BUFFER_PER_PID = "per PID"; //$NON-NLS-1$
    /**
     * Buffer type : shared
     */
    static final String BUFFER_SHARED = "shared"; //$NON-NLS-1$
    /**
     * If the LTTng version doesn't show the buffer type
     */
    static final String BUFFER_TYPE_UNKNOWN = "information not unavailable"; //$NON-NLS-1$

}
