/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.pcap.core.trace;

/**
 * Exception that is thrown when the Pcap file is not valid.
 *
 * @author Vincent Perot
 */
public class BadPcapFileException extends Exception {

    private static final long serialVersionUID = 8228512814116052260L;

    /**
     * Default constructor with no message.
     */
    public BadPcapFileException() {
        super();
    }

    /**
     * Constructor with an attached message.
     *
     * @param message
     *            The message attached to this exception
     */
    public BadPcapFileException(String message) {
        super(message);
    }

    /**
     * Re-throw an exception into this type.
     *
     * @param e
     *            The previous Exception we caught
     */
    public BadPcapFileException(Exception e) {
        super(e);
    }

    /**
     * Constructor with an attached message and re-throw an exception into this
     * type.
     *
     * @param message
     *            The message attached to this exception
     * @param exception
     *            The previous Exception caught
     */
    public BadPcapFileException(String message, Throwable exception) {
        super(message, exception);
    }

}
