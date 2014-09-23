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

package org.eclipse.linuxtools.internal.pcap.core.packet;

/**
 * Exception that is thrown when Packet is erroneous. This is different than an
 * invalid packet. An invalid packet contains bad fields (such as bad checksum)
 * and does not throw exceptions while an erroneous packet is a packet that is
 * impossible to obtain. For instance, an erroneous packet can be smaller than
 * the minimum required size. Erroneous packets throw BadPacketExceptions.
 *
 * @author Vincent Perot
 */
public class BadPacketException extends Exception {

    private static final long serialVersionUID = 7071588720009577619L;

    /**
     * Default constructor with no message.
     */
    public BadPacketException() {
        super();
    }

    /**
     * Constructor with an attached message.
     *
     * @param message
     *            The message attached to this exception
     */
    public BadPacketException(String message) {
        super(message);
    }

    /**
     * Re-throw an exception into this type.
     *
     * @param e
     *            The previous Exception we caught
     */
    public BadPacketException(Exception e) {
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
    public BadPacketException(String message, Throwable exception) {
        super(message, exception);
    }

}
