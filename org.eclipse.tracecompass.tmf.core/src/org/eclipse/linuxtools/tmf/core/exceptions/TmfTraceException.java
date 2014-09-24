/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.exceptions;

/**
 * TMF trace related exception
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfTraceException extends Exception {

    /**
     * The exception version ID
     */
    private static final long serialVersionUID = -6829650938285722133L;

    /**
     * Constructor
     *
     * @param errMsg the error message
     */
    public TmfTraceException(String errMsg) {
        super(errMsg);
    }

    /**
     * Constructor
     *
     * @param errMsg the error message
     * @param cause the error cause (<code>null</code> is permitted which means no cause is available)
     */
    public TmfTraceException(String errMsg, Throwable cause) {
        super(errMsg, cause);
    }
}
