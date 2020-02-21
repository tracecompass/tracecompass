/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core;

/**
 * General exception that is thrown when there is a problem somewhere with the
 * CTF trace reader.
 *
 * @author Alexandre Montplaisir
 * @since 1.0
 */
public class CTFException extends Exception {

    private static final long serialVersionUID = 2065258365219777672L;

    /**
     * Default constructor with no message.
     */
    public CTFException() {
        super();
    }

    /**
     * Constructor with an attached message.
     *
     * @param message
     *            The message attached to this exception
     */
    public CTFException(String message) {
        super(message);
    }

    /**
     * Re-throw an exception into this type.
     *
     * @param e
     *            The previous Exception we caught
     */
    public CTFException(Exception e) {
        super(e);
    }

    /**
     * Constructor with an attached message and re-throw an exception into this type.
     *
     * @param message
     *            The message attached to this exception
     * @param exception
     *            The previous Exception caught
     */
    public CTFException(String message, Throwable exception) {
        super(message, exception);
    }

}
