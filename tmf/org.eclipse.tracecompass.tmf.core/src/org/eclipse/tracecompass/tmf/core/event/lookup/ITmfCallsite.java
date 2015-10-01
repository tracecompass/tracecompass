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
     */
    String getFunctionName();

    /**
     * Returns the line number of the call site.
     *
     * @return the line number
     */
    long getLineNumber();
}
