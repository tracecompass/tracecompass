/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson, Ecole Polytechnique de Montreal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.trace;

/**
 * General exception that is thrown when there is a problem somewhere with the
 * CTF trace reader.
 *
 * @version 1.0
 * @author Alexandre Montplaisir
 */
public class CTFReaderException extends Exception {

    private static final long serialVersionUID = 2065258365219777672L;

    /**
     * Default constructor with no message.
     */
    public CTFReaderException() {
        super();
    }

    /**
     * Constructor with an attached message.
     *
     * @param message
     *            The message attached to this exception
     */
    public CTFReaderException(String message) {
        super(message);
    }

    /**
     * Re-throw an exception into this type.
     *
     * @param e
     *            The previous Exception we caught
     */
    public CTFReaderException(Exception e) {
        super(e);
    }

    /**
     * Constructor with an attached message and re-throw an exception into this type.
     *
     * @param message
     *            The message attached to this exception
     * @param exception
     *            The previous Exception caught
     * @since 2.0
     */
    public CTFReaderException(String message, Throwable exception) {
        super(message, exception);
    }

}
