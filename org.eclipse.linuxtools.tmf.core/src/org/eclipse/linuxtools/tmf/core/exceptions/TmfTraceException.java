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
 * <b><u>TmfTraceException</u></b>
 * <p>
 * TMF trace related exception
 */
public class TmfTraceException extends Exception {

    /**
     * The exception version ID
     */
    private static final long serialVersionUID = -6829650938285722133L;

    /**
     * @param errMsg the error message
     */
    public TmfTraceException(String errMsg) {
        super(errMsg);
    }

}
