/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event.lookup;


/**
 * The generic call site structure in TMF. A call site has:
 * <ul>
 * <li> a file name
 * <li> a function name (optional)
 * <li> a line number
 * </ul>
 *
 * @author Bernd Hufmann
 * @since 2.0
 *
 * @see TmfCallsite
 */
public interface ITmfCallsite {
    /**
     * Returns the file name of the call site.
     * @return the file name
     */
    public String getFileName();

    /**
     * Returns the function name of the call site.
     * @return the function name or null
     */
    public String getFunctionName();

    /**
     * Returns the line number of the call site.
     * @return the line number
     */
    public long getLineNumber();
}
