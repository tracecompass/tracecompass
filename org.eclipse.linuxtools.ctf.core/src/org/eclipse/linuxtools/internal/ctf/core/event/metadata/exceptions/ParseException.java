/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.ctf.core.event.metadata.exceptions;


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
     * @param message (to be sent to logs
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
}
