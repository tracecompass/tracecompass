/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam- Initial API and implementation
 **********************************************************************/

package org.eclipse.linuxtools.internal.tracing.rcp.ui.cli;

/**
 * Command line exceptions.
 *
 * @author Matthew Khouzam
 */
public class TracingRCPCliException extends Exception {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -844846299720475123L;

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized, and may subsequently be initialized by a call to
     * {@link #initCause}.
     *
     * @param message
     *            the detail message. The detail message is saved for later
     *            retrieval by the {@link #getMessage()} method.
     */
    public TracingRCPCliException(String message) {
        super(message);
    }

}
