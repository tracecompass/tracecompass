/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam- Initial API and implementation
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.cli.core.parser;

/**
 * Command line exceptions.
 *
 * @author Matthew Khouzam
 */
public class CliParserException extends Exception {

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
    public CliParserException(String message) {
        super(message);
    }

}
