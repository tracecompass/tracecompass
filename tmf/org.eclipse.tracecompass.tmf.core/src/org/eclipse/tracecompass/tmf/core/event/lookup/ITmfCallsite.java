/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
     * Returns the line number of the call site.
     *
     * @return The line number, or 'null' if unavailable
     * @since 2.1
     */
    @Nullable Long getLineNo();
}
