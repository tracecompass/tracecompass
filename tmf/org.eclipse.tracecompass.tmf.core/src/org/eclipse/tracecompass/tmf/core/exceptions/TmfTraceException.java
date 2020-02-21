/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.exceptions;

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
