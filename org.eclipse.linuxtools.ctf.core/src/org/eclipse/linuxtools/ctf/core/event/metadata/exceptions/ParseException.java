/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event.metadata.exceptions;

/**
 * <b><u>ParseException</u></b>
 */
public class ParseException extends Exception {

    private static final long serialVersionUID = 7901917601459652080L;

    public ParseException() {
        super();
    }

    /**
     * Constructor
     *
     * @param message
     */
    public ParseException(String message) {
        super(message);
    }

}
