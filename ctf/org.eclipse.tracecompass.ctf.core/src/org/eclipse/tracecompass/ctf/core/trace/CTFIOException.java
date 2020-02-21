/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.ctf.core.trace;

import org.eclipse.tracecompass.ctf.core.CTFException;

/**
 * An exception just for trace readers and writers
 *
 * @since 1.0
 */
public class CTFIOException extends CTFException {

    /**
     * Unique ID
     */
    private static final long serialVersionUID = -2216400542574921838L;

    /**
     * Constructor with an attached message.
     *
     * @param message
     *            The message attached to this exception
     */
    public CTFIOException(String message) {
        super(message);
    }

    /**
     * Constructor with an attached message.
     *
     * @param e
     *            The exception attached to this exception
     */
    public CTFIOException(Exception e) {
        super(e);
    }

    /**
     * Constructor with an attached message and exception.
     *
     * @param message
     *            The message attached to this exception
     * @param e
     *            The encapsulated exception
     * @since 1.0
     */
    public CTFIOException(String message, Exception e) {
        super(message, e);
    }

}
