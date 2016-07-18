/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.lookup;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The generic call site structure in TMF. A call site has:
 * <ul>
 * <li> a file name
 * <li> a function name (optional)
 * <li> a line number
 * </ul>
 *
 * @author Bernd Hufmann
 *
 * @see TmfCallsite
 */
public interface ITmfCallsite {

    /**
     * Returns the file name of the call site.
     *
     * @return the file name
     */
    @NonNull String getFileName();

    /**
     * Returns the function name of the call site.
     *
     * @return the function name or null
     * @deprecated Should not be part of this interface anymore.
     */
    @Deprecated
    @Nullable String getFunctionName();

    /**
     * Returns the line number of the call site.
     *
     * @return the line number
     * @deprecated Use {@link #getLineNo()} instead, which can return null.
     */
    @Deprecated
    long getLineNumber();

    /**
     * Returns the line number of the call site.
     *
     * @return The line number, or 'null' if unavailable
     * @since 2.1
     */
    default @Nullable Long getLineNo() {
        /* TODO Change to abstract method once getLineNumber() is removed */
        return getLineNumber();
    }
}
