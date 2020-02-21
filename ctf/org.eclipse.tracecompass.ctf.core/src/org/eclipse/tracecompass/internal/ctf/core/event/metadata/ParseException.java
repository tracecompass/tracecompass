/*******************************************************************************
 * Copyright (c) 2011, 2015 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.event.metadata;


/**
 * <b><u>ParseException</u></b>
 */
public class ParseException extends Exception {

    private static final long serialVersionUID = 7901917601459652080L;

    /**
     * Empty constructor
     */
    public ParseException() {
        super();
    }

    /**
     * Constructor
     *
     * @param message to be sent to logs
     */
    public ParseException(String message) {
        super(message);
    }

    /**
     * Copy constructor
     * @param e the exception to throw
     */
    public ParseException(Exception e) {
        super(e);
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this exception's detail message.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public ParseException(String message, Exception cause) {
        super(message, cause);
    }
}
